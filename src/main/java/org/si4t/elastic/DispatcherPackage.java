package org.si4t.elastic;

/**
 * DispatcherPackage.
 * 
 * @author Marko Milic
 */
public class DispatcherPackage
{
	private DispatcherAction action;

	private ElasticSearchClientRequest clientRequest;

	private DocumentBatch documentBatch;

	public DispatcherPackage(DispatcherAction action, ElasticSearchClientRequest request, DocumentBatch documentBatch)
	{
		super();
		this.setAction(action);
		this.setClientRequest(request);
		this.setDocumentBatch(documentBatch);
	}

	public DispatcherAction getAction() {
		return action;
	}

	public void setAction(DispatcherAction action) {
		this.action = action;
	}

	public ElasticSearchClientRequest getClientRequest() {
		return clientRequest;
	}

	public void setClientRequest(ElasticSearchClientRequest clientRequest) {
		this.clientRequest = clientRequest;
	}

	public DocumentBatch getDocumentBatch() {
		return documentBatch;
	}

	public void setDocumentBatch(DocumentBatch documentBatch) {
		this.documentBatch = documentBatch;
	}
}
