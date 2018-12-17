package nl.naturalis.purl;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;

import org.apache.http.client.utils.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.naturalis.nba.api.model.Specimen;
import nl.naturalis.nba.utils.StringUtil;
import nl.naturalis.purl.rdf.RdfWriter;

import static nl.naturalis.purl.ContentNegotiationUtil.MEDIATYPE_RDF_XML;
import static nl.naturalis.purl.ContentNegotiationUtil.getAvailableMultiMediaTypes;
import static nl.naturalis.purl.ContentNegotiationUtil.getRequestedMediaTypes;
import static nl.naturalis.purl.rest.ResourceUtil.notAcceptable;
import static nl.naturalis.purl.rest.ResourceUtil.notFound;
import static nl.naturalis.purl.rest.ResourceUtil.plainTextResponse;
import static nl.naturalis.purl.rest.ResourceUtil.redirect;
import static nl.naturalis.purl.rest.ResourceUtil.redirectDebug;

/**
 * Abstract base class for PURLs referring to specimens c.q. observations.
 */
public abstract class AbstractSpecimenPurlHandler extends AbstractPurlHandler {

  private static final Logger logger = LogManager.getLogger(AbstractSpecimenPurlHandler.class);

  public AbstractSpecimenPurlHandler(String objectID, HttpServletRequest request, UriInfo uriInfo) {
    super(objectID, request, uriInfo);
  }

  @Override
  protected Response doHandle() throws PurlException {
    Specimen specimen = NbaUtil.getSpecimen(objectId);
    if (specimen == null) {
      logger.info("Responding with 404 (Not Found) for unitID \"{}\"", objectId);
      return notFound("specimen", objectId);
    }
    if (!sourceSystemOK(specimen)) {
      logger.info("Responding with 404 (Not Found) for unitID \"{}\". Wrong source system: \"{}\"", objectId,
          specimen.getSourceSystem().getCode());
      return notFound("specimen", objectId);
    }
    List<MediaType> requested = getRequestedMediaTypes(request);
    if (requested.size() == 0) {
      return getRdfResponse(specimen);
    }
    for (MediaType mediaType : requested) {
      if (mediaType.isCompatible(MEDIATYPE_RDF_XML)) {
        return getRdfResponse(specimen);
      }
      Optional<URI> uri = negotiate(mediaType, specimen);
      if (uri.isPresent()) {
        if (debug) {
          return redirectDebug(uri.get());
        }
        return redirect(uri.get());
      }
    }
    List<MediaType> available = getAvailableMediaTypes(specimen);
    List<Variant> variants = Variant.mediaTypes(available.toArray(new MediaType[available.size()])).build();
    logger.info("Responding with 406 (Not Acceptable) for unitID \"{}\"", objectId);
    return notAcceptable(variants);
  }

  /**
   * Test whether the PURL being handled is compatible with the specimen retrieved using the unitID element in the PURL. For example,
   * http://data.biodiversitydata.nl/naturalis/specimen/XC12345 is a Naturalis PURL containing a Xeno-canto unitID. This needs to be
   * rejected (i.e. result in a 404 response).
   * 
   * @param specimen
   * @return
   */
  protected abstract boolean sourceSystemOK(Specimen specimen);

  /**
   * Find a suitable forward-to location for the media type requested by the client. Implements the content negotiation mechanism.
   * 
   * @param mediaType
   * @param specimen
   * @return
   */
  protected Optional<URI> negotiate(MediaType mediaType, Specimen specimen) {
    if (mediaType.isCompatible(MediaType.TEXT_HTML_TYPE)) {
      return getHtmlLandingPage(specimen);
    }
    if (mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
      return Optional.of(getNbaUri());
    }
    return findMatchInSpecimenDocument(mediaType, specimen);
  }

  /**
   * Provide the forward-to location if the client requested HTML.
   * 
   * @param specimen
   * @return
   */
  protected abstract Optional<URI> getHtmlLandingPage(Specimen specimen);

  /**
   * Search the provided specimen document for a URI matching the requested media type.
   * 
   * @param mediaType
   * @param specimen
   * @return
   */
  protected Optional<URI> findMatchInSpecimenDocument(MediaType mediaType, Specimen specimen) {
    return ContentNegotiationUtil.findMatchingMultiMediaUri(mediaType, specimen);
  }

  /**
   * Provide a list of media types that can be served. By default a list of RDF/XML, HTML, JSON and all multimedia media types found in the
   * specimen document is returned, but subclasses can override this if required.
   * 
   * @param specimen
   * @return
   */
  protected List<MediaType> getAvailableMediaTypes(Specimen specimen) {
    List<MediaType> available = new ArrayList<>();
    available.add(MEDIATYPE_RDF_XML);
    available.add(MediaType.TEXT_HTML_TYPE);
    available.add(MediaType.APPLICATION_JSON_TYPE);
    available.addAll(getAvailableMultiMediaTypes(specimen));
    return available;
  }

  private URI getNbaUri() {
    String baseUrl = Registry.getInstance().getNbaBaseUrl();
    URIBuilder ub;
    try {
      ub = new URIBuilder(baseUrl);
    } catch (URISyntaxException e) {
      throw new PurlConfigException("Invalid value for nba.baseurl (check purl.properties)");
    }
    String rootPath = ub.getPath();
    StringBuilder fullPath = new StringBuilder(50);
    if (rootPath != null) {
      fullPath.append(StringUtil.rtrim(rootPath, '/'));
    }
    fullPath.append("/specimen/findByUnitID/");
    fullPath.append(objectId);
    ub.setPath(fullPath.toString());
    try {
      return ub.build();
    } catch (URISyntaxException e) {
      throw new PurlConfigException(e);
    }
  }

  private Response getRdfResponse(Specimen specimen) {
    if (debug) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
      new RdfWriter().write(specimen, baos);
      try {
        return plainTextResponse(baos.toString("UTF-8"));
      } catch (UnsupportedEncodingException e) { // Won't happen
        throw new AssertionError();
      }
    }
    StreamingOutput stream = (output) -> {
      new RdfWriter().write(specimen, output);
    };
    return Response.ok(stream).type(MEDIATYPE_RDF_XML).build();
  }

}
