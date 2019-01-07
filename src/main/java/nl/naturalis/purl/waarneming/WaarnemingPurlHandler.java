package nl.naturalis.purl.waarneming;

import java.net.URI;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.naturalis.nba.api.model.MultiMediaObject;
import nl.naturalis.nba.api.model.Specimen;
import nl.naturalis.nba.utils.ConfigObject;
import nl.naturalis.purl.AbstractSpecimenPurlHandler;
import nl.naturalis.purl.ContentNegotiationUtil;
import nl.naturalis.purl.NbaUtil;
import nl.naturalis.purl.PurlUtil;
import nl.naturalis.purl.Registry;

import static nl.naturalis.nba.api.model.SourceSystem.OBS;

/**
 * Handles PURLs conforming to the Waarneming.nl URL template.
 * 
 * @author Ayco Holleman
 */
public class WaarnemingPurlHandler extends AbstractSpecimenPurlHandler {

  @SuppressWarnings("unused")
  private static final Logger logger = LogManager.getLogger(WaarnemingPurlHandler.class);

  public WaarnemingPurlHandler(String objectID, HttpServletRequest request, UriInfo uriInfo) {
    super(objectID, request, uriInfo);
  }

  @Override
  protected boolean sourceSystemOK(Specimen specimen) {
    return specimen.getSourceSystem() == OBS;
  }

  @Override
  protected Optional<URI> getHtmlLandingPage(Specimen specimen) {
    ConfigObject cfg = Registry.getInstance().getConfig();
    String urlTemplate = cfg.required("waarneming.observation.url");
    return Optional.of(PurlUtil.createUrl(urlTemplate, "sourceSystemId", specimen.getSourceSystemId()));
  }

  /*
   * NOTE: waarneming.nl has blanked out the multimedia URIs in the specimen document, so the implementation in AbstractSpecimenPurlHandler
   * won't work. The multimedia URIs can still be found in the MultiMediaObject index though.
   */
  @Override
  protected Optional<URI> findMultiMediaUriWithMediaType(MediaType mediaType, Specimen specimen) {
    MultiMediaObject[] multimedia = NbaUtil.getMultiMedia(specimen);
    return ContentNegotiationUtil.findMatchingMultiMediaUri(mediaType, multimedia);
  }

}
