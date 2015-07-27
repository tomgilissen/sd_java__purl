package nl.naturalis.purl.rest;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import nl.naturalis.nda.domain.ObjectType;

/**
 * Utility class providing useful REST-related functionality.
 * 
 * @author Ayco Holleman
 * @created Jul 22, 2015
 *
 */
public class ResourceUtil {

	/**
	 * Defined as {@code application/json;charset=UTF-8}. When sending a JSON
	 * response back to the client, use this media type in stead of the prefab
	 * MediaType.APPLICATION_JSON. We seem to have reports that without the
	 * charset parameter, some browsers or browser versions don't interpret the
	 * response as expected.
	 */
	public static final String MEDIA_TYPE_JSON = "application/json;charset=UTF-8";


	private ResourceUtil()
	{
	}


	/**
	 * Equivalent to {@code URLEncoder.encode(s, "UTF-8")}.
	 * 
	 * @param raw
	 *            The {@code String} to encode
	 * @return
	 */
	public static String urlEncode(String raw)
	{
		try {
			return URLEncoder.encode(raw, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			// Won't happen with UTF-8
			return null;
		}
	}


	/**
	 * Performs a 303 (See Other) redirect to the specified location
	 * 
	 * @param location
	 * @return
	 */
	public static Response redirect(URI location)
	{
		return Response.seeOther(location).build();
	}


	/**
	 * Shows the location to which the PURL server would redirect, but does not
	 * actually redirect.
	 * 
	 * @param location
	 * @return
	 */
	public static Response redirectDebug(URI location)
	{
		String message = "303 (SEE OTHER)\n" + location;
		return plainTextResponse(message);
	}


	/**
	 * Generate an HTTP response with status 500 (INTERNAL SERVER ERROR) and the
	 * specified message in the response body. The content type of the response
	 * body is set to text/plain.
	 * 
	 * @param message
	 * @return
	 */
	public static Response serverError(String message)
	{
		message = "500 (INTERNAL SERVER ERROR)\n" + message;
		return plainTextResponse(500, message);
	}


	/**
	 * Report a server error, but set the actual HTTP status to 200 (OK). By
	 * setting the HTTP status to 200, we are guaranteed that the browser treats
	 * and displays the response body in the most typical fashion. The content
	 * type of the response body is set to text/plain.
	 * 
	 * @param message
	 * @return
	 */
	public static Response serverErrorDebug(String message)
	{
		message = "500 (INTERNAL SERVER ERROR)\n" + message;
		return plainTextResponse(message);
	}


	/**
	 * Generate a 404 (NOT FOUND) response with the specified message in the
	 * response body.
	 * 
	 * @param message
	 * @return
	 */
	public static Response notFound(ObjectType objectType, String objectID)
	{
		String message = String.format("404 (NOT FOUND)\nNo %s exists with ID %s", objectType, objectID);
		return plainTextResponse(404, message);
	}


	/**
	 * Generate a 406 (NOT ACCEPTABLE) response with the specified list of
	 * acceptable alternative media types both in the response header and the
	 * response body.
	 * 
	 * @param variants
	 * @return
	 */
	public static Response notAcceptable(List<Variant> variants)
	{
		StringBuilder sb = new StringBuilder(200);
		sb.append("406 (NOT ACCEPTABLE)\nNone of the requested media types can be served.");
		sb.append("\nAcceptable media types for this object: ");
		if (variants == null || variants.size() == 0) {
			sb.append(" none!");
		}
		else {
			sb.append(getVariantsAsString(variants));
		}
		return Response.notAcceptable(variants).entity(sb.toString()).type(MediaType.TEXT_PLAIN).build();
	}


	/**
	 * Generate a debug variant of an HTTP 406 (NOT ACCEPTABLE) response. The
	 * HTTP status actually returned is 200 (OK) and the list of alternative
	 * mediatypes is written to the response body.
	 * 
	 * @param variants
	 * @return
	 */
	public static Response notAcceptableDebug(List<Variant> variants)
	{
		StringBuilder sb = new StringBuilder(200);
		sb.append("406 (NOT ACCEPTABLE)\nNone of the requested media types can be served.");
		sb.append("\nAcceptable media types for this object: ");
		if (variants == null || variants.size() == 0) {
			sb.append(" none!");
		}
		else {
			sb.append(getVariantsAsString(variants));
		}
		return plainTextResponse(sb.toString());
	}


	/**
	 * Generate a 200 (OK) response with the specified message in the response
	 * body and a Content-Type header of text/plain.
	 * 
	 * @param message
	 * @return
	 */
	public static Response plainTextResponse(String message)
	{
		return Response.ok(message, MediaType.TEXT_PLAIN).build();
	}


	/**
	 * Generate a response with the specified HTTP status code, the specified
	 * message in the reponse body and a Content-Type header of text/plain.
	 * 
	 * @param status
	 * @param message
	 * @return
	 */
	public static Response plainTextResponse(int status, String message)
	{
		return Response.status(status).entity(message).type(MediaType.TEXT_PLAIN).build();
	}


	private static String getVariantsAsString(List<Variant> variants)
	{
		StringBuilder sb = new StringBuilder(64);
		for (int i = 0; i < variants.size(); ++i) {
			if (i != 0) {
				sb.append(',');
			}
			sb.append(variants.get(i).getMediaType().toString());
		}
		return sb.toString();
	}

}
