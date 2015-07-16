package nl.naturalis.purl.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import nl.naturalis.nda.client.NBAResourceException;

/**
 * 
 * @author Ayco Holleman
 * @created Jul 9, 2015
 *
 */
public abstract class AbstractPurl implements Purl {

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

	protected final String localId;
	protected final MediaType[] accept;

	private boolean debug;


	/**
	 * Create a PURL handler for the specified local scope id and the specified
	 * media types in the Accept header. The local scope id is part of the PURL
	 * (usually the last or second-last part of the URL). It is a
	 * Naturalis-specific ID for the object, for example a bar code.
	 */
	public AbstractPurl(String localId, MediaType[] accept)
	{
		this.localId = localId;
		this.accept = accept;
	}


	public void setDebug(boolean debug)
	{
		this.debug = debug;
	}


	@Override
	public final Response handle()
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