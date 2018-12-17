package nl.naturalis.purl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.naturalis.nba.api.model.MultiMediaObject;
import nl.naturalis.nba.api.model.ServiceAccessPoint;
import nl.naturalis.nba.api.model.Specimen;

/**
 * Utility class dealing with matching media types requested via the HTTP Accept header to actually available media types.
 * 
 * @author Ayco Holleman
 *
 */
public class ContentNegotiationUtil {

  /**
   * Defined as {@code application/json;charset=UTF-8}. When sending a JSON response back to the client, use this media type in stead of the
   * prefab MediaType.APPLICATION_JSON. We seem to have reports that without the charset parameter, some browsers or browser versions don't
   * interpret the response as expected.
   */
  public static final MediaType MEDIATYPE_JSON = new MediaType("application", "json", "UTF-8");
  /**
   * Defined as {@code application/rdf+xml}.
   */
  public static final MediaType MEDIATYPE_RDF_XML = new MediaType("application", "rdf+xml");
  /**
   * image/jpeg
   */
  private static final MediaType MEDIATYPE_JPEG = new MediaType("image", "jpeg");

  private static final Logger logger = LogManager.getLogger(ContentNegotiationUtil.class);

  /**
   * Retrieve Accept headers from the HTTP request and convert them to an array of {@code MediaType} instances. Note that clients can supply
   * multiple Accept headers, but they can also supply one Accept header with a comma-separated list of media types, or they could do both.
   * For debug purposes you can mimic Accept headers by adding an "__accept" query parameter to the PURL. If you do this, the actual Accept
   * headers (if any) will be ignored.
   * 
   * @param request
   * @return
   */
  public static List<MediaType> getRequestedMediaTypes(HttpServletRequest request) {
    String acceptParam = request.getParameter("__accept");
    if (acceptParam != null) {
      return getRequestedMediaTypesDebug(acceptParam);
    }
    List<MediaType> types = new ArrayList<>();
    Enumeration<String> acceptHeaders = request.getHeaders("Accept");
    while (acceptHeaders.hasMoreElements()) {
      String acceptHeader = acceptHeaders.nextElement();
      String[] mediaTypes = acceptHeader.split(",");
      for (String one : mediaTypes) {
        try {
          types.add(MediaType.valueOf(one));
        } catch (IllegalArgumentException e) {
          if (mediaTypes.length == 1) {
            logger.warn("Invalid Accept header in request: \"{}\" (ignored)", one);
          } else {
            logger.warn("Invalid media type in Accept header: \"{}\" (ignored)", one);
          }
        }
      }
    }
    return types;
  }

  /**
   * Get the available multimedia media types from the provided MultiMediaObject documents.
   */
  public static Set<MediaType> getAvailableMultiMediaTypes(MultiMediaObject[] multimedia) {
    Set<MediaType> mediaTypes = new LinkedHashSet<>();
    for (MultiMediaObject mmo : multimedia) {
      if (mmo.getServiceAccessPoints() != null) {
        for (ServiceAccessPoint sap : mmo.getServiceAccessPoints()) {
          // HACK. Media type not always set. Solve in import!
          MediaType mt = sap.getFormat() == null ? MEDIATYPE_JPEG : MediaType.valueOf(sap.getFormat());
          mediaTypes.add(mt);
        }
      }
    }
    return mediaTypes;
  }

  /**
   * Get the available multimedia media types from the provided Specimen document.
   */
  public static Set<MediaType> getAvailableMultiMediaTypes(Specimen specimen) {
    Set<MediaType> mediaTypes = new LinkedHashSet<>();
    if (specimen.getAssociatedMultiMediaUris() != null) {
      for (ServiceAccessPoint sap : specimen.getAssociatedMultiMediaUris()) {
        // HACK. Media type not always set. Solve in import!
        MediaType mt = sap.getFormat() == null ? MEDIATYPE_JPEG : MediaType.valueOf(sap.getFormat());
        mediaTypes.add(mt);
      }
    }
    return mediaTypes;
  }

  /**
   * Searches the provided multimedia documents for a multimedia URI that matches the provided media type.
   */
  public static Optional<URI> findMatchingMultiMediaUri(MediaType requested, MultiMediaObject[] multimedia) {
    for (MultiMediaObject mmo : multimedia) {
      if (mmo.getServiceAccessPoints() != null) {
        for (ServiceAccessPoint sap : mmo.getServiceAccessPoints()) {
          MediaType mt = sap.getFormat() == null ? MEDIATYPE_JPEG : MediaType.valueOf(sap.getFormat());
          if (requested.isCompatible(mt)) {
            return Optional.ofNullable(sap.getAccessUri());
          }
        }
      }
    }
    return Optional.empty();
  }

  /**
   * Searches the provided specimen document for a multimedia URI that matches the provided media type.
   * 
   * @param requested
   * @param specimen
   * @return
   */
  public static Optional<URI> findMatchingMultiMediaUri(MediaType requested, Specimen specimen) {
    if (specimen.getAssociatedMultiMediaUris() != null) {
      for (ServiceAccessPoint sap : specimen.getAssociatedMultiMediaUris()) {
        MediaType mt = sap.getFormat() == null ? MEDIATYPE_JPEG : MediaType.valueOf(sap.getFormat());
        if (requested.isCompatible(mt)) {
          return Optional.ofNullable(sap.getAccessUri());
        }
      }
    }
    return Optional.empty();
  }

  private static List<MediaType> getRequestedMediaTypesDebug(String requestParam) {
    String[] chunks = requestParam.split(",");
    List<MediaType> types = new ArrayList<>(chunks.length);
    for (String chunk : chunks) {
      try {
        types.add(MediaType.valueOf(chunk));
      } catch (IllegalArgumentException e) {
        logger.warn("Invalid media type in __accept parameter: \"{}\" (ignored)", chunk);
      }
    }
    return types;
  }

}
