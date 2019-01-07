package nl.naturalis.purl.xenocanto;

import java.net.URI;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.naturalis.nba.api.model.Specimen;
import nl.naturalis.nba.utils.ConfigObject;
import nl.naturalis.purl.AbstractSpecimenPurlHandler;
import nl.naturalis.purl.PurlUtil;
import nl.naturalis.purl.Registry;

import static nl.naturalis.nba.api.model.SourceSystem.XC;

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
    String urlTemplate = cfg.required("xenocanto.observation.url");
    return Optional.of(PurlUtil.createUrl(urlTemplate, "sourceSystemId", specimen.getSourceSystemId()));
  }

}
