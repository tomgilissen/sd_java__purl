package nl.naturalis.purl.rest;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.naturalis.nba.api.model.Specimen;
import nl.naturalis.nba.utils.ConfigObject;
import nl.naturalis.purl.PurlException;
import nl.naturalis.purl.Registry;

import static nl.naturalis.nba.api.model.SourceSystem.XC;
import static nl.naturalis.purl.rest.ContentNegotiatorUtil.findUriForMediaType;
import static nl.naturalis.purl.rest.ContentNegotiatorUtil.getAvailableMultiMediaTypes;
import static nl.naturalis.purl.rest.ContentNegotiatorUtil.getRequestedMediaTypes;
import static nl.naturalis.purl.rest.ResourceUtil.load;
import static nl.naturalis.purl.rest.ResourceUtil.notAcceptable;
import static nl.naturalis.purl.rest.ResourceUtil.notFound;
import static nl.naturalis.purl.rest.ResourceUtil.redirect;
import static nl.naturalis.purl.rest.ResourceUtil.redirectDebug;

/**
 * A {@link PurlHandler} capable of handling PURLs for specimen objects.
 * 
 * @author Ayco Holleman
 */
public class XenoCantoPurlHandler extends AbstractSpecimenPurlHandler {

  private static final Logger logger = LogManager.getLogger(XenoCantoPurlHandler.class);

  public XenoCantoPurlHandler(String objectID, HttpServletRequest request, UriInfo uriInfo) {
    super(objectID, request, uriInfo);
  }

  @Override
  protected Response doHandle() throws Exception {
    Specimen specimen = getSpecimen();
    if (specimen == null) {
      logger.info("Responding with 404 (Not Found) for unitID \"{}\"", objectID);
      return notFound("observation", objectID);
    }
    if (!sourceSystemOK(specimen)) {
      logger.info("Responding with 404 (Not Found) for unitID \"{}\" (belongs to another source system: \"{}\")",
          objectID, specimen.getSourceSystem().getCode());
      return notFound("observation", objectID);
    }
    for (MediaType mediaType : getRequestedMediaTypes(request)) {
      URI location = getLocation(mediaType, specimen);
      if (location != null) {
        if (Registry.getInstance().getConfig().isTrue("noredirect")) {
          return load(location, mediaType);
        }
        if (debug) {
          return redirectDebug(location);
        }
        return redirect(location);
      }
    }
    Set<MediaType> available = new LinkedHashSet<>();
    available.add(MediaType.TEXT_HTML_TYPE);
    available.add(MediaType.APPLICATION_JSON_TYPE);
    available.addAll(getAvailableMultiMediaTypes(specimen));
    MediaType[] mts = available.toArray(new MediaType[available.size()]);
    logger.info("Responding with 406 (Not Acceptable) for unitID \"{}\"", objectID);
    List<Variant> variants = Variant.mediaTypes(mts).build();
    return notAcceptable(variants);
  }

  private static boolean sourceSystemOK(Specimen specimen) {
    return specimen.getSourceSystem() == XC;
  }

  private URI getLocation(MediaType mediaType, Specimen specimen) throws PurlException {
    if (mediaType.isCompatible(MediaType.TEXT_HTML_TYPE)) {
      return getXenoCantoUri(specimen);
    }
    if (mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
      return getNbaUri();
    }
    Optional<URI> o = findUriForMediaType(mediaType, specimen);
    return o.isPresent() ? o.get() : null;
  }

  private static URI getXenoCantoUri(Specimen specimen) throws PurlException {
    ConfigObject cfg = Registry.getInstance().getConfig();
    String uriTemplate = cfg.required("xenocanto.observation.url");
    if (!uriTemplate.contains("${sourceSystemId}")) {
      throw new PurlException(
          "Missing placeholder \"${sourceSystemId}\" in Xeno-canto URL template (check purl.properties)");
    }
    return URI.create(uriTemplate.replace("${sourceSystemId}", specimen.getSourceSystemId()));
  }

}
