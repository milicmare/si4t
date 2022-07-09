package org.si4t.elastic;

public class ElasticSearchException extends Exception
{
    /**
	 * 
	 *  * @author Marko Milic
	 */
	private static final long serialVersionUID = 1L;

	// Parameterless Constructor
    public ElasticSearchException() {}

    // Constructor that accepts a message
    public ElasticSearchException(String message)
    {
       super(message);
    }
    
    // Constructor that accepts a message
    public ElasticSearchException(String message, Exception e)
    {
       super(message, e);
    }
}