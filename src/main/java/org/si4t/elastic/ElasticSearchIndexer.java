package org.si4t.elastic;

import com.tridion.configuration.Configuration;
import com.tridion.configuration.ConfigurationException;
import com.tridion.storage.si4t.BaseIndexData;
import com.tridion.storage.si4t.BinaryIndexData;
import com.tridion.storage.si4t.IndexingException;
import com.tridion.storage.si4t.SearchIndex;
import com.tridion.storage.si4t.SearchIndexData;
import com.tridion.storage.si4t.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ElasticSearchIndexer.
 * 
 * @author Marko Milic
 */
public class ElasticSearchIndexer implements SearchIndex {

	private static final Logger log = LoggerFactory.getLogger(ElasticSearchIndexer.class);
	private static String INDEXER_NODE = "Indexer";
	private static String DEFAULT_INDEX_BATCH_SIZE = "10";

	private Map<String, BaseIndexData> itemRemovals = new ConcurrentHashMap<String, BaseIndexData>();
	private Map<String, SearchIndexData> itemAdds = new ConcurrentHashMap<String, SearchIndexData>();
	private Map<String, BinaryIndexData> binaryAdds = new ConcurrentHashMap<String, BinaryIndexData>();
	private Map<String, SearchIndexData> itemUpdates = new ConcurrentHashMap<String, SearchIndexData>();

	private String documentEndpoint;

	private String user;

	private String password;

	private String indexName;

	private int indexBatchSize;

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tridion.storage.si4t.SearchIndex#configure(com.tridion
	 * .configuration.Configuration)
	 */

	@Override
	public void configure(Configuration configuration) throws ConfigurationException
	{
		log.debug("Configuration is: " + configuration.toString());

		Configuration indexerConfiguration = configuration.getChild(INDEXER_NODE);

		String documentEndpoint = indexerConfiguration.getAttribute("documentEndpoint");
		log.info("Setting Document Endpoint to: " + documentEndpoint);
		this.documentEndpoint = documentEndpoint;

		String user = indexerConfiguration.getAttribute("user");
		log.info("Setting user to: " + user);
		this.user = user;

		String password = indexerConfiguration.getAttribute("password");
		log.info("Setting Password to: " + password);
		this.password = password;

		String indexName = indexerConfiguration.getAttribute("indexName");
		log.info("Setting Index Name to: " + indexName);
		this.indexName = indexName;

		String indexBatchSize = indexerConfiguration.getAttribute("indexBatchSize", DEFAULT_INDEX_BATCH_SIZE);
		this.indexBatchSize = Integer.valueOf(indexBatchSize);
		log.info("Index batch size set to: " + this.indexBatchSize);

	}

	public void addItemToIndex(SearchIndexData data) throws IndexingException
	{
		if (Utils.StringIsNullOrEmpty(data.getUniqueIndexId()))
		{
			log.error("Addition failed. Unique ID is empty");
			return;
		}

		if (data.getFieldSize() == 0)
		{
			log.warn("To be indexed item has no data.");
			log.warn("Item is: " + data.toString());
		}

		if (!this.itemAdds.containsKey(data.getUniqueIndexId()))
		{
			this.itemAdds.put(data.getUniqueIndexId(), data);
		}
	}

	public void removeItemFromIndex(BaseIndexData data) throws IndexingException
	{
		if (Utils.StringIsNullOrEmpty(data.getUniqueIndexId()))
		{
			log.error("Removal addition failed. Unique ID empty");
			return;
		}
		this.itemRemovals.put(data.getUniqueIndexId(), data);
	}

	public void updateItemInIndex(SearchIndexData data) throws IndexingException
	{
		if (Utils.StringIsNullOrEmpty(data.getUniqueIndexId()))
		{
			log.error("Adding update item failed. Unique ID empty");
			return;
		}
		this.itemUpdates.put(data.getUniqueIndexId(), data);
	}

	public void addBinaryToIndex(BinaryIndexData data) throws IndexingException {
		// TODO Auto-generated method stub
	}

	public void removeBinaryFromIndex(BaseIndexData data) throws IndexingException {
		// TODO Auto-generated method stub
	}

	public void commit(String publicationId) throws IndexingException
	{
		try
		{
			this.commitAddContentToElasticSearch(this.itemAdds);
			//this.commitAddBinariesToElasticSearch();
			this.removeItemsFromElasticSearch(this.itemRemovals);
			this.processItemUpdates();
		}
		catch (IOException e)
		{
			logException(e);
			throw new IndexingException("IO Exception: " + e.getMessage());
		}
		catch (ParserConfigurationException e)
		{
			logException(e);
			throw new IndexingException("ParserConfigurationException: " + e.getMessage());
		}
		catch (SAXException e)
		{
			logException(e);
			throw new IndexingException("SAXException:" + e.getMessage());
		} catch (ElasticSearchException e) {
			logException(e);
			throw new IndexingException("SAXException:" + e.getMessage());
		}
		finally
		{
			log.info("Clearing out registers.");
			this.clearRegisters();
		}
	}

	private void commitAddBinariesToElasticSearch() throws IOException, ParserConfigurationException, SAXException
	{
		if (this.binaryAdds.size() > 0)
		{
			log.info("Adding binaries to Solr.");

			log.info
					(
						ElasticSearchIndexDispatcher.INSTANCE.
								addBinaries(binaryAdds,
										new ElasticSearchClientRequest(
												this.documentEndpoint, this.user, this.password, this.indexName)
								)
					);
		}
	}

	private void commitAddContentToElasticSearch(Map<String, SearchIndexData> itemsToAdd) throws IOException, ParserConfigurationException, SAXException, ElasticSearchException
	{
		if (itemsToAdd != null && itemsToAdd.size() > 0)
		{
			log.info("Adding " + itemsToAdd.size() + " documents in batches of " + indexBatchSize);

			List<DocumentBatch> groupedDocuments = new ArrayList<DocumentBatch>();

			int i = 0;
			DocumentBatch documentBatch = null;
			for (Entry<String, SearchIndexData> entry : itemsToAdd.entrySet())
			{
				if (i % indexBatchSize == 0)
				{
					documentBatch = new DocumentBatch();
					groupedDocuments.add(documentBatch);
				}
				SearchIndexData data = entry.getValue();
				documentBatch.getItems().add(constructInputDocument(data, log));
				i++;
			}
			log.trace(groupedDocuments.toString());
			this.dispatchAddContentToElasticSearch(groupedDocuments);
		}
	}

	private void removeItemsFromElasticSearch(Map<String, BaseIndexData> itemsToRemove) throws IOException, ParserConfigurationException, SAXException, ElasticSearchException
	{
		if (itemsToRemove != null && itemsToRemove.size() > 0)
		{
			log.info("Removing " + itemsToRemove.size() + " documents in batches of " + indexBatchSize);

			List<DocumentBatch> groupedDocuments = new ArrayList<DocumentBatch>();

			int i = 0;
			DocumentBatch documentBatch = null;
			for (Entry<String, BaseIndexData> entry : itemsToRemove.entrySet())
			{
				if (i % indexBatchSize == 0 || documentBatch == null)
				{
					documentBatch = new DocumentBatch();
					groupedDocuments.add(documentBatch);
				}

				documentBatch.getItems().add(
						new DocumentData( entry.getValue().getUniqueIndexId())
				);
				i++;
			}
			log.trace(groupedDocuments.toString());
			this.dispatchRemoveItemsFromCloudSearch(groupedDocuments);
		}
	}

	private static DocumentData constructInputDocument(SearchIndexData data, Logger log)
	{
		DocumentData document = new DocumentData(data.getUniqueIndexId());

		log.info("Adding document with ID: " + document.getId());

		Map<String, ArrayList<Object>> fieldList = data.getIndexFields();
		for (Entry<String, ArrayList<Object>> fieldEntry : fieldList.entrySet())
		{
			String fieldName = fieldEntry.getKey();
			ArrayList<Object> fieldValues =  fieldEntry.getValue();

			if (fieldValues.size() == 1)
			{
				document.getFields().put(fieldName, fieldValues.get(0));
			}
			else
			{
				document.getFields().put(fieldName, fieldValues);
			}
		}
		return document;
	}

	private void dispatchAddContentToElasticSearch(List<DocumentBatch> groupedDocuments) throws ParserConfigurationException, IOException, SAXException, ElasticSearchException
	{
		log.info("Dispatching documents in " + groupedDocuments.size() + " batches");

		int batchIndex = 1;
		for (DocumentBatch documentBatch : groupedDocuments)
		{
			int batchSize = documentBatch.getItems().size();
			if (batchSize > 0)
			{
				DispatcherPackage dispatcherPackage = new DispatcherPackage
						(
								DispatcherAction.PERSIST,
								new ElasticSearchClientRequest(
										this.documentEndpoint, this.user, this.password, this.indexName
								),
								documentBatch
						);
				String status = ElasticSearchIndexDispatcher.INSTANCE.addDocuments(dispatcherPackage);

				log.info("Adding " + batchSize + " documents of batch " + batchIndex + " had the following response: " + status);
			}
			batchIndex++;
		}
	}

	private void dispatchRemoveItemsFromCloudSearch(List<DocumentBatch> groupedDocuments) throws ParserConfigurationException, IOException, SAXException, ElasticSearchException
	{
		log.info("Dispatching documents in " + groupedDocuments.size() + " batches");

		int batchIndex = 1;
		for (DocumentBatch documentBatch : groupedDocuments)
		{
			int batchSize = documentBatch.getItems().size();
			if (batchSize > 0)
			{
				DispatcherPackage dispatcherPackage = new DispatcherPackage
						(
								DispatcherAction.PERSIST,
								new ElasticSearchClientRequest(
										this.documentEndpoint, this.user, this.password, this.indexName
								),
								documentBatch
						);
				String status = ElasticSearchIndexDispatcher.INSTANCE.removeFromElasticSearch(dispatcherPackage);

				log.info("Removing " + batchSize + " documents of batch " + batchIndex + " had the following response: " + status);
			}
			batchIndex++;
		}
	}

	private void processItemUpdates() throws ParserConfigurationException, IOException, SAXException, ElasticSearchException
	{
		this.commitAddContentToElasticSearch(this.itemUpdates);
	}

	private void logException(Exception e)
	{
		log.error(e.getMessage());
		log.error(Utils.stacktraceToString(e.getStackTrace()));
	}

	private void clearRegisters()
	{
		itemAdds.clear();
		binaryAdds.clear();
		itemRemovals.clear();
		itemUpdates.clear();
	}

	public void destroy()
	{
		ElasticSearchIndexDispatcher.INSTANCE.destroyServers();
	}

}