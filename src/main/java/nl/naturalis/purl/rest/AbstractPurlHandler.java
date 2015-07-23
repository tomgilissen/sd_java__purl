package nl.naturalis.purl.rest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import nl.naturalis.nda.client.NBAResourceException;

import static nl.naturalis.purl.rest.ResourceUtil.*;

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
		this.request = request;
		this.uriInfo = uriInfo;
		this.objectID = uriInfo.getPathParameters().getFirst("objectID");
		this.accept = ContentNegotiator.getRequestedMediaTypes(request);
		String val = uriInfo.getQueryParameters().getFirst("__debug");
		if (val != null && (val.length() == 0 || val.toLowerCase().equals("true"))) {
			this.debug = true;
		}
		else {
			this.debug = false;
		}
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
			else {
				return serverError(e.getServerInfoAsString());
			}
		}
		catch (Throwable t) {
			if (debug) {
				return serverErrorDebug(getStackTrace(t));
			}
			else {
				return serverError(getStackTrace(t));
			}
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