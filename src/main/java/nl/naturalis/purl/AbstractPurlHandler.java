package nl.naturalis.purl;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.naturalis.nba.client.ServerException;
import nl.naturalis.nba.utils.StringUtil;
import nl.naturalis.purl.rest.PurlHandler;
import nl.naturalis.purl.rest.ResourceUtil;

import static nl.naturalis.purl.rest.ResourceUtil.serverErrorDebug;

/**
 * Abstract base class for classes capable of handling PURL requests. Implements the one method specified by the {@link PurlHandler}
 * interface by delegating everything except exception handling to concrete subclasses via the abstract template method (
 * {@link #doHandle()}). Subclasses should generally not try to handle {@code Exception}s themselves, but throw them out of the
 * {@code doHandle()} method, so they will be handled in a uniform way.
 * 
 * @author Ayco Holleman
 * 
 */
public abstract class AbstractPurlHandler implements PurlHandler {

  private static final Logger logger = LogManager.getLogger(AbstractPurlHandler.class);

  protected final HttpServletRequest request;
  protected final UriInfo uriInfo;

  /**
   * The local scope identifier extracted from the PURL. Given the current URL templates, that's the very last part of the PURL. For
   * specimens, the local scope identifier can be the unitID or the sourceSystemId of the specimen.
   */
  protected final String objectId;
  /**
   * Whether or not we should provide debug information, wrapped in a {@link ResourceUtil#serverErrorDebug(String) debug response}.
   */
  protected final boolean debug;

  /**
   * Create a {@code PurlHandler} for the specified PURL request.
   * 
   * @param request
   * @param uriInfo
   */
  public AbstractPurlHandler(String objectID, HttpServletRequest request, UriInfo uriInfo) {
    logger.info("Receiving request for " + uriInfo.getPath());
    this.objectId = objectID;
    this.request = request;
    this.uriInfo = uriInfo;
    /*
     * If the __debug parameter is in the request URL, the HTTP status code will always be 200, even if an error occurs. This ensures that
     * browsers will treat the response (which includes a plain/text error message and the HTTP status code that would normally be returned)
     * as though nothing special has happened. This behaviour is most likely unnecessary for anything but the oldest browsers.
     */
    if (uriInfo.getQueryParameters().containsKey("__debug")) {
      String val = uriInfo.getQueryParameters().getFirst("__debug");
      this.debug = StringUtil.isTrue(val, true);
    } else {
      this.debug = false;
    }
  }

  @Override
  public final Response handlePurl() {
    try {
      Response response = doHandle();
      return response;
    } catch (ServerException e) {
      if (debug) {
        return serverErrorDebug(e.getServerInfoAsString());
      }
      return ResourceUtil.serverError(e.getServerInfoAsString());
    } catch (Throwable t) {
      if (debug) {
        return serverErrorDebug(getStackTrace(t));
      }
      return ResourceUtil.serverError(getStackTrace(t));
    }
  }

  /**
   * Template method to be implemented by concrete subclasses.
   * 
   * @return The HTTP response to be sent back to the client.
   * @throws Exception
   */
  protected abstract Response doHandle() throws Exception;

  private static String getStackTrace(Throwable t) {
    while (t.getCause() != null) {
      t = t.getCause();
    }
    StringBuilder sb = new StringBuilder(6000);
    sb.append(t.toString());
    for (StackTraceElement e : t.getStackTrace()) {
      sb.append("\nat ");
      sb.append(e.getClassName()).append('.').append(e.getMethodName());
      sb.append('(')
          .append(e.getFileName())
          .append(':')
          .append(e.getLineNumber())
          .append(')');
    }
    return sb.toString();
  }

}
