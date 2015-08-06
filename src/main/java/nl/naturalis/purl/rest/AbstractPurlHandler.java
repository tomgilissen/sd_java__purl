package nl.naturalis.purl.rest;

import static nl.naturalis.purl.rest.ResourceUtil.serverError;
import static nl.naturalis.purl.rest.ResourceUtil.serverErrorDebug;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import nl.naturalis.nda.client.NBAResourceException;

import org.domainobject.util.ArrayUtil;
import org.domainobject.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for classes capable of handling PURL requests. Implements
 * the one method specified by the {@link PurlHandler} interface by delegating
 * everything to concrete subclasses via an abstract template method (
 * {@link #doHandle()}). What it does take care of, however, is error handling.
 * Subclasses should generally not try to handle {@code Exception}s themselves,
 * but just throw them out of the {@code doHandle()} method.
 * 
 * @author Ayco Holleman
 * @created Jul 9, 2015
 * 
 *
 */
public abstract class AbstractPurlHandler implements PurlHandler {

	private static final Logger logger = LoggerFactory.getLogger(AbstractPurlHandler.class);

	protected final HttpServletRequest request;
	protected final UriInfo uriInfo;
	/**
	 * The local scope identifier extracted from the PURL.
	 */
	protected final String objectID;
	/**
	 * The Accept headers in the HTTP request.
	 */
	protected final MediaType[] accept;
	/**
	 * Whether or not we should provide debug information, wrapped in a
	 * {@link ResourceUtil#serverErrorDebug(String) debug response}.
	 */
	protected final boolean debug;


	/**
	 * Create a {@code PurlHandler} for the specified PURL request.
	 * 
	 * @param request
	 * @param uriInfo
	 */
	public AbstractPurlHandler(HttpServletRequest request, UriInfo uriInfo)
	{
		if(logger.isDebugEnabled()) {
			logger.debug("Creating " + getClass() + " for " + uriInfo.getPath());
		}
		this.request = request;
		this.uriInfo = uriInfo;
		this.objectID = uriInfo.getPathParameters().getFirst("objectID");
		this.accept = ContentNegotiator.getRequestedMediaTypes(request);
		if(logger.isDebugEnabled()) {
			logger.debug("Accepted media types: " + ArrayUtil.implode(accept));
		}
		String val = uriInfo.getQueryParameters().getFirst("__debug");
		this.debug = StringUtil.isTrue(val);
	}


	/**
	 * Implements {@link PurlHandler#handlePurl()}.
	 */
	@Override
	public final Response handlePurl()
	{
		try {
			Response response = doHandle();
			assert (response != null);
			return response;
		}
		catch (NBAResourceException e) {
			if (debug) {
				return serverErrorDebug(e.getServerInfoAsString());
			}
			return serverError(e.getServerInfoAsString());
		}
		catch (Throwable t) {
			if (debug) {
				return serverErrorDebug(getStackTrace(t));
			}
			return serverError(getStackTrace(t));
		}
	}


	/**
	 * Template method to be implemented by concrete subclasses. Subclasses are
	 * allowed and encouraged to throw any exception that should lead to a
	 * server error directly out of the {@code doHandle} method.
	 * 
	 * @return The HTTP response to be sent back to the client.
	 * @throws Exception
	 */
	protected abstract Response doHandle() throws Exception;


	private static String getStackTrace(Throwable t)
	{
		while (t.getCause() != null) {
			t = t.getCause();
		}
		StringBuilder sb = new StringBuilder(6000);
		sb.append(t.toString());
		for (StackTraceElement e : t.getStackTrace()) {
			sb.append("\nat ");
			sb.append(e.getClassName()).append('.').append(e.getMethodName());
			sb.append('(').append(e.getFileName()).append(':').append(e.getLineNumber()).append(')');
		}
		return sb.toString();
	}

}