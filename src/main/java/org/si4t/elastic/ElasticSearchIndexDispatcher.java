package org.si4t.elastic;

import com.tridion.storage.si4t.BinaryIndexData;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum ElasticSearchIndexDispatcher
{
	INSTANCE;

	private static final Logger log = LoggerFactory.getLogger(ElasticSearchIndexDispatcher.class);
	private static ConcurrentHashMap<String, OkHttpClient> _httpClients = new ConcurrentHashMap<>();

	private OkHttpClient getElasticSearchHTTPClient(ElasticSearchClientRequest clientRequest) throws ElasticSearchException
	{
		if (_httpClients.get(clientRequest.getEndpointUrl()) == null)
		{
			log.info("Obtaining Elastic Search Client [" + clientRequest.getEndpointUrl() + ": " + clientRequest.getEndpointUrl());
				this.createElasticSearchHTTPClient(clientRequest.getEndpointUrl(),clientRequest.getUserName(),clientRequest.getPassword());
		}
		return _httpClients.get(clientRequest.getEndpointUrl());
	}

	private void createElasticSearchHTTPClient(String endpointUrl, String user, String password) throws ElasticSearchException
	{
		log.info("Creating Elastic Search Client: " + endpointUrl);
		OkHttpClient client = new OkHttpClient().newBuilder()
				  .build();
		_httpClients.put(endpointUrl, client);
	}

	public String addDocuments(DispatcherPackage dispatcherPackage) throws ParserConfigurationException, IOException, SAXException, ElasticSearchException
	{
		ElasticSearchClientRequest clientRequest = dispatcherPackage.getClientRequest();
		OkHttpClient client = this.getElasticSearchHTTPClient(clientRequest);
		DocumentBatch documentBatch = dispatcherPackage.getDocumentBatch();
		if (documentBatch == null)
		{
			throw new NullPointerException("Document batch is null");
		}

		String url = clientRequest.getEndpointUrl() + "/" + clientRequest.getIndexName() + "/_bulk?pretty";
		log.info("URL constructed: " + url);
		ArrayList<DocumentData> documents = documentBatch.getItems();
		StringBuilder stringBuilder = new StringBuilder(1000);


		for (DocumentData documentData : documents)
		{
			log.info("Adding " + documentData.getId() + " document to the elastic search Indexer");

			String singleRequestData = String.format("{ \"index\" : {\"_id\" : \"%s\", \"_index\" : \"%s\"} }\r\n %s \r\n",
					documentData.getId(),
					clientRequest.getIndexName(),
					new Gson().toJson(documentData.getFields())
					);
			stringBuilder.append(singleRequestData);
		}

		log.info("Body constructed: " + stringBuilder.toString());
		
		MediaType mediaType = MediaType.parse("application/json");
		RequestBody body = RequestBody.create(mediaType, stringBuilder.toString());
		Request request = new Request.Builder()
		  .url(url)
		  .method("PUT", body)
		  .addHeader("Authorization", getBasicAuthenticationHeader(clientRequest.getUserName(),clientRequest.getPassword()))
		  .addHeader("Content-Type", "application/json")
		  .build();
		
		Response response = client.newCall(request).execute();	
		if (response.code() != 200)
		{
			throw new ElasticSearchException("Failed to add documents! Exception code was: "+ response.code());
		}		
		
		String responseBodyString = response.body().string();
		log.info("response body" + documents.size() + " document(s) had the following response: " + responseBodyString);
		
		ObjectMapper objectMapper = new ObjectMapper();
		ElasticJsonResponse statusMessage = objectMapper.readValue(responseBodyString, ElasticJsonResponse.class);
		
		if (statusMessage.errors == true)
		{
			throw new ElasticSearchException("Failed to add documents! Status was: "+ responseBodyString);
		}	
		
		response.body().close();
		return ("Adding " + documents.size() + " document(s) had the following response: " + responseBodyString);
	}

	private static final String getBasicAuthenticationHeader(String username, String password) {
		String valueToEncode = username + ":" + password;
		return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
	}

	public String addBinaries(Map<String, BinaryIndexData> binaryAdds, ElasticSearchClientRequest clientRequest) throws IOException, ParserConfigurationException, SAXException
	{
		//TODO:NOT IMPLEMENTED
		return "";
	}

	public String removeFromElasticSearch(DispatcherPackage dispatcherPackage) throws ParserConfigurationException, IOException, SAXException, ElasticSearchException
	{
		ElasticSearchClientRequest clientRequest = dispatcherPackage.getClientRequest();
		OkHttpClient client = this.getElasticSearchHTTPClient(clientRequest);
		DocumentBatch documentBatch = dispatcherPackage.getDocumentBatch();
		if (documentBatch == null)
		{
			throw new NullPointerException("Document batch is null");
		}
		String url = clientRequest.getEndpointUrl() + "/" + clientRequest.getIndexName() + "/_bulk?pretty";
		log.info("URL constructed: " + url);
		ArrayList<DocumentData> documents = documentBatch.getItems();
		StringBuilder stringBuilder = new StringBuilder(1000);

		for (DocumentData documentData : documents)
		{
			log.info("Adding " + documentData.getId() + " document to the elastic search Indexer");

			String singleRequestData = String.format("{ \"delete\" : { \"_index\" : \"%s\", \"_id\" : \"%s\" } }\r\n",
					clientRequest.getIndexName(),
					documentData.getId()
					);
			stringBuilder.append(singleRequestData);
		}

		log.info("Body constructed: " + stringBuilder.toString());
		
		MediaType mediaType = MediaType.parse("application/json");
		RequestBody body = RequestBody.create(mediaType, stringBuilder.toString());
		Request request = new Request.Builder()
		  .url(url)
		  .method("PUT", body)
		  .addHeader("Authorization", getBasicAuthenticationHeader(clientRequest.getUserName(),clientRequest.getPassword()))
		  .addHeader("Content-Type", "application/json")
		  .build();
		
		Response response = client.newCall(request).execute();	
		if (response.code() != 200)
		{
			throw new ElasticSearchException("Failed to delete documents! Exception code was: "+ response.code());
		}		
		String responseBodyString = response.body().string();
		log.info("response body" + documents.size() + " document(s) had the following response: " + responseBodyString);
		
		ObjectMapper objectMapper = new ObjectMapper();
		ElasticJsonResponse statusMessage = objectMapper.readValue(responseBodyString, ElasticJsonResponse.class);
		
		if (statusMessage.errors == true)
		{
			throw new ElasticSearchException("Failed to delete documents! Status was: "+ responseBodyString);
		}	
		
		response.body().close();
		
		return ("Deleting " + documents.size() + " document(s) had the following response: " + responseBodyString);
	}

	public void destroyServers()
	{
	}
}