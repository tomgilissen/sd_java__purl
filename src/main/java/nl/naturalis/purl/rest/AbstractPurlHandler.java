package nl.naturalis.purl.rest;

import java.io.UnsupportedEncodingException;
import java.net.URI;
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


	protected static String[] getStackTrace(Throwable t)
	{
		while (t.getCause() != null) {
			t = t.getCause();
		}
		StackTraceElement[] stackTrace = t.getStackTrace();
		String[] trace = new String[stackTrace.length];
		for (int i = 0; i < stackTrace.length; ++i) {
			StackTraceElement e = stackTrace[i];
			StringBuilder sb = new StringBuilder(128);
			sb.append("at ");
			sb.append(e.getClassName()).append('.').append(e.getMethodName());
			sb.append('(').append(e.getFileName()).append(':').append(e.getLineNumber()).append(')');
			trace[i] = sb.toString();
		}
		return trace;
	}


	protected final String objectID;
	protected final MediaType[] accept;
	protected final boolean debug;


	public AbstractPurlHandler(HttpServletRequest request, UriInfo uriInfo)
	{
		this.objectID = uriInfo.getPathParameters().getFirst("UnitID");
		this.accept = ContentNegotiator.getRequestedMediaTypes(request);
		String val = uriInfo.getQueryParameters().getFirst("__debug");
		if (val == null || val.length() == 0 || val.toLowerCase().equals("true"))
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
				return Response.ok().entity(e.getServerInfoAsString()).build();
			else
				return Response.serverError().entity(e.getServerInfoAsString()).build();
		}
		catch (Throwable t) {
			if (debug)
				return Response.ok().entity(getStackTrace(t)).build();
			else
				return Response.serverError().entity(getStackTrace(t)).build();
		}
	}


	protected abstract Response doHandle() throws Exception;

}