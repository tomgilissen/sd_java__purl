package nl.naturalis.purl;

public class PURLException extends Exception {

	private static final long serialVersionUID = -5682097016562077983L;


	public PURLException(String message)
	{
		super(message);
	}


	public PURLException(Throwable cause)
	{
		super(cause);
	}


	public PURLException(String message, Throwable cause)
	{
		super(message, cause);
	}

}
