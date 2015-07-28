package nl.naturalis.purl;

/**
 * Base class for checked exceptions of PURL application.
 * 
 * @author Ayco Holleman
 * @created Jul 22, 2015
 *
 */
public class PurlException extends Exception {

	private static final long serialVersionUID = -5682097016562077983L;


	public PurlException(String message)
	{
		super(message);
	}


	public PurlException(Throwable cause)
	{
		super(cause);
	}


	public PurlException(String message, Throwable cause)
	{
		super(message, cause);
	}

}
