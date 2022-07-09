package org.si4t.elastic;

/**
 * ElasticSearchClientRequest.
 * 
 * @author Marko Milic
 */
public class ElasticSearchClientRequest {

	private String endpointUrl;

	private String user;

	private String password;

	private String indexName;

	public ElasticSearchClientRequest(String endpointUrl, String user, String password, String indexName)
	{
		this.endpointUrl = endpointUrl;
		this.user = user;
		this.password = password;
		this.indexName = indexName;
	}

	public String getEndpointUrl() { return endpointUrl; }
	public String getUserName() { return user; }
	public String getPassword() { return password; }
	public String getIndexName() {
		return indexName;
	}
}
