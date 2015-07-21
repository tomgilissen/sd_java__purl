package nl.naturalis.purl.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import nl.naturalis.nda.client.NBAResourceException;

/**
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
	protected static final String JSON_MEDIA_TYPE = "application/json;charset=UTF-8";


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

	protected final String objectID;
	protected final MediaType[] accept;
	protected final boolean debug;


	public AbstractPurlHandler(HttpServletRequest request, UriInfo uriInfo)
	{
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
		StackTraceElement[] stackTrace = t.getStackTrace();
		StringBuilder sb = new StringBuilder(6000);
		sb.append(t.toString());
		for (int i = 0; i < stackTrace.length; ++i) {
			StackTraceElement e = stackTrace[i];
			sb.append("\nat ");
			sb.append(e.getClassName()).append('.').append(e.getMethodName());
			sb.append('(').append(e.getFileName()).append(':').append(e.getLineNumber()).append(')');
		}
		return sb.toString();
	}

}