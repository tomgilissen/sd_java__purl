package nl.naturalis.purl;

/**
 * @author Ayco Holleman
 *
 */
public class ApplicationInitializationException extends RuntimeException {

	private static final long serialVersionUID = -5149672072255517864L;


	/**
	 * @param message
	 */
	public ApplicationInitializationException(String message)
	{
		super(message);
	}


	/**
	 * @param cause
	 */
	public ApplicationInitializationException(Throwable cause)
	{
		super(cause);
	}


	/**
	 * @param message
	 * @param cause
	 */
	public ApplicationInitializationException(String message, Throwable cause)
	{
		super(message, cause);
	}

}
