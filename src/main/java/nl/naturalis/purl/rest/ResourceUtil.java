package nl.naturalis.purl.rest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import nl.naturalis.nba.utils.debug.BeanPrinter;
import nl.naturalis.nba.utils.http.SimpleHttpGet;
import nl.naturalis.purl.Messages;

import static nl.naturalis.purl.Messages.INTERNAL_SERVER_ERROR;
import static nl.naturalis.purl.Messages.NOT_ACCEPTABLE;
import static nl.naturalis.purl.Messages.NOT_FOUND;
import static nl.naturalis.purl.Messages.SEE_OTHER;

/**
 * Utility class providing REST-related functionality.
 * 
 * @author Ayco Holleman
 */
public class ResourceUtil {

  private ResourceUtil() {}

  /**
   * Equivalent to {@code URLEncoder.encode(s, "UTF-8")}.
   * 
   * @param raw The {@code String} to encode
   * @return
   */
  public static String utf8Encode(String raw) {
    try {
      return URLEncoder.encode(raw, "UTF-8");
    } catch (UnsupportedEncodingException e) {
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
  public static Response redirect(URI location) {
    return Response.seeOther(location).build();
  }

  /**
   * Retrieves and serves up the content at the specified location.
   * 
   * @param location
   * @param mediaType
   * @return
   */
  public static Response load(URI location, MediaType mediaType) {
    SimpleHttpGet request = new SimpleHttpGet();
    request.setBaseUrl(location);
    byte[] source = request.execute().getResponseBody();
    return Response.ok().entity(source).type(mediaType).build();
  }

  /**
   * Shows the location to which the PURL server would redirect, but does not actually redirect.
   * 
   * @param location
   * @return
   */
  public static Response redirectDebug(URI location) {
    String message = SEE_OTHER + location;
    return plainTextResponse(message);
  }

  /**
   * Generate an HTTP response with status 500 (INTERNAL SERVER ERROR) and the specified message in the response body. The content type of
   * the response body is set to text/plain.
   * 
   * @param message
   * @return
   */
  public static Response serverError(String message) {
    message = INTERNAL_SERVER_ERROR + message;
    return ResourceUtil.plainTextResponse(500, message);
  }

  /**
   * Report a server error, but set the actual HTTP status to 200 (OK). By setting the HTTP status to 200, we are guaranteed that the
   * browser treats and displays the response body in the most typical fashion. The content type of the response body is set to text/plain.
   * 
   * @param message
   * @return
   */
  public static Response serverErrorDebug(String message) {
    message = INTERNAL_SERVER_ERROR + message;
    return plainTextResponse(message);
  }

  /**
   * Generate a 404 (NOT FOUND) response with the specified message in the response body.
   * 
   * @param message
   * @return
   */
  public static Response notFound(String objectType, String objectID) {
    String message = NOT_FOUND + String.format("No %s exists with ID \"%s\"", objectType, objectID);
    return plainTextResponse(404, message);
  }

  /**
   * Generate a 406 (NOT ACCEPTABLE) response with the specified list of acceptable alternative media types both in the response header and
   * the response body.
   * 
   * @param variants
   * @return
   */
  public static Response notAcceptable(List<Variant> variants) {
    StringBuilder sb = new StringBuilder(200);
    sb.append(NOT_ACCEPTABLE);
    sb.append("Acceptable media types for this object: ");
    if (variants == null || variants.size() == 0) {
      sb.append(" none!");
    } else {
      sb.append(getVariantsAsString(variants));
    }
    return Response.notAcceptable(variants)
        .entity(sb.toString())
        .type(MediaType.TEXT_PLAIN)
        .build();
  }

  /**
   * Generate a debug variant of an HTTP 406 (NOT ACCEPTABLE) response. The HTTP status actually returned is 200 (OK) and the list of
   * alternative mediatypes is written to the response body.
   * 
   * @param variants
   * @return
   */
  public static Response notAcceptableDebug(List<Variant> variants) {
    StringBuilder sb = new StringBuilder(200);
    sb.append(Messages.NOT_ACCEPTABLE);
    sb.append("\nAcceptable media types for this object: ");
    if (variants == null || variants.size() == 0) {
      sb.append(" none!");
    } else {
      sb.append(getVariantsAsString(variants));
    }
    return plainTextResponse(sb.toString());
  }

  /**
   * Generate a 200 (OK) response with the specified message in the response body and a Content-Type header of text/plain.
   * 
   * @param message
   * @return
   */
  public static Response plainTextResponse(String message) {
    return Response.ok(message, MediaType.TEXT_PLAIN).build();
  }

  /**
   * Generate a response with the specified HTTP status code, the specified message in the reponse body and a Content-Type header of
   * text/plain.
   * 
   * @param status
   * @param message
   * @return
   */
  public static Response plainTextResponse(int status, String message) {
    return Response.status(status).entity(message).type(MediaType.TEXT_PLAIN).build();
  }

  /**
   * Give a print-out of the specified object.
   * 
   * @param bean
   * @return
   */
  public static String dump(Object bean) {
    StringWriter sw = new StringWriter(4096);
    BeanPrinter bp = new BeanPrinter(new PrintWriter(sw));
    bp.dump(bean);
    return sw.toString();
  }

  private static String getVariantsAsString(List<Variant> variants) {
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
