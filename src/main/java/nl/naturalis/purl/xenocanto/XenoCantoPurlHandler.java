package nl.naturalis.purl.xenocanto;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.naturalis.nba.api.model.Specimen;
import nl.naturalis.nba.utils.ConfigObject;
import nl.naturalis.purl.AbstractSpecimenPurlHandler;
import nl.naturalis.purl.PurlConfigException;
import nl.naturalis.purl.Registry;

import static nl.naturalis.nba.api.model.SourceSystem.XC;
import static nl.naturalis.purl.Messages.MISSING_PLACEHOLDER;

/**
 * Handles PURLs conforming to the Xeno Canto URL template.
 * 
 * @author Ayco Holleman
 */
public class XenoCantoPurlHandler extends AbstractSpecimenPurlHandler {

  @SuppressWarnings("unused")
  private static final Logger logger = LogManager.getLogger(XenoCantoPurlHandler.class);

  public XenoCantoPurlHandler(String objectID, HttpServletRequest request, UriInfo uriInfo) {
    super(objectID, request, uriInfo);
  }

  @Override
  protected boolean sourceSystemOK(Specimen specimen) {
    return specimen.getSourceSystem() == XC;
  }

  @Override
  protected Optional<URI> getHtmlLandingPage(Specimen specimen) {
    ConfigObject cfg = Registry.getInstance().getConfig();
    String uriTemplate = cfg.required("xenocanto.observation.url");
    if (!uriTemplate.contains("${unitID}")) {
      throw new PurlConfigException(String.format(MISSING_PLACEHOLDER, "unitID", uriTemplate));
    }
    try {
      URI uri = new URI(uriTemplate.replace("${unitID}", specimen.getUnitID()));
      return Optional.of(uri);
    } catch (URISyntaxException e) {
      throw new PurlConfigException(e);
    }
  }

}
