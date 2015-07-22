package nl.naturalis.purl.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import nl.naturalis.nda.client.NBAResourceException;

/**
 * Abstract base class for classes capable of handling PURL requests. Implements
 * the one method specified by the {@link PurlHandler} interface by delegating
 * everything to concrete subclasses via an abstract template method (
 * {@link #doHandle()}). What it does take care of, however, is exception
 * handling. Subclasses should generally not try to handle {@code Exception}s
 * themselves, but just throw them out of the {@code doHandle()} method.
 * 
 * @author Ayco Holleman
 * @created Jul 9, 2015
 *
 */
public abstract class AbstractPurlHandler implements PurlHandler {

	/**
	 * When sending a JSON response, use this media type in stead of the prefab
	 * MediaType.APPLICATION_JSON. We seem to have reports that without the
	 * charset parameter, some browsers or browser versions don't interpret the
	 * response as expected.
	 */
	protected static final String MEDIA_TYPE_JSON = "application/json;charset=UTF-8";


	protected static String urlEncode(String s)
	{
		try {
			return URLEncoder.encode(s, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			// Won't happen with UTF-8
			return null;
		}
	}

	protected final HttpServletRequest request;
	protected final UriInfo uriInfo;
	protected final String objectID;
	protected final MediaType[] accept;
	protected final boolean debug;


	public AbstractPurlHandler(HttpServletRequest request, UriInfo uriInfo)
	{
		this.request = request;
		this.uriInfo = uriInfo;
		this.objectID = uriInfo.getPathParameters().getFirst("UnitID");
		this.accept = ContentNegotiator.getRequestedMediaTypes(request);
		String val = uriInfo.getQueryParameters().getFirst("__debug");
		if (val != null && (val.length() == 0 || val.toLowerCase().equals("true")))
			this.debug = true;
		else
			this.debug = false;
	}


	@Override
	public final Response handlePurl()
	{
		try {
			Response response = doHandle();
			assert (response != null);
			return response;
		}
		catch (NBAResourceException e) {
			if (debug)
				return debugResponse(e.getServerInfoAsString());
			else
				return serverError(e.getServerInfoAsString());
		}
		catch (Throwable t) {
			if (debug)
				return debugResponse(getStackTrace(t));
			else
				return serverError(getStackTrace(t));
		}
	}


	protected abstract Response doHandle() throws Exception;


	private static Response serverError(String entity)
	{
		return Response.serverError().type(MediaType.TEXT_PLAIN).entity(entity).build();
	}


	private static Response debugResponse(String entity)
	{
		return Response.ok().type(MediaType.TEXT_PLAIN).entity(entity).build();
	}


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