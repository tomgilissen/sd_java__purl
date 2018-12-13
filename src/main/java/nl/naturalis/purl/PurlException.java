package nl.naturalis.purl;

/**
 * Base class for runtime exceptions in the PURL application.
 * 
 * @author Ayco Holleman
 *
 */
public class PurlException extends RuntimeException {

  public PurlException(String message) {
    super(message);
  }

  public PurlException(Throwable cause) {
    super(cause);
  }

  public PurlException(String message, Throwable cause) {
    super(message, cause);
  }

}
