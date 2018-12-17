package nl.naturalis.purl.waarneming;

import java.net.URI;
import java.net.URISyntaxException;
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
import nl.naturalis.purl.PurlConfigException;
import nl.naturalis.purl.Registry;

import static nl.naturalis.nba.api.model.SourceSystem.OBS;
import static nl.naturalis.purl.Messages.MISSING_PLACEHOLDER;

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
    String uriTemplate = cfg.required("waarneming.observation.url");
    if (!uriTemplate.contains("${sourceSystemId}")) {
      throw new PurlConfigException(String.format(MISSING_PLACEHOLDER, "sourceSystemId", uriTemplate));
    }
    try {
      URI uri = new URI(uriTemplate.replace("${sourceSystemId}", specimen.getSourceSystemId()));
      return Optional.of(uri);
    } catch (URISyntaxException e) {
      throw new PurlConfigException(e);
    }
  }

  /*
   * NOTE: waarneming.nl has blanked out the multimedia URIs in the specimen document, so the implementation in AbstractSpecimenPurlHandler
   * won't work. But the URIs can still be found in the MultiMediaObject index.
   */
  @Override
  protected Optional<URI> findMatchInSpecimenDocument(MediaType mediaType, Specimen specimen) {
    MultiMediaObject[] multimedia = NbaUtil.getMultiMedia(specimen);
    return ContentNegotiationUtil.findMatchingMultiMediaUri(mediaType, multimedia);
  }

}
