package nl.naturalis.purl.naturalis;

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

import static nl.naturalis.nba.api.model.SourceSystem.BRAHMS;
import static nl.naturalis.nba.api.model.SourceSystem.CRS;

/**
 * Handles PURLs conforming to the Naturalis URL template.
 * 
 * 
 * @author Ayco Holleman
 */
public class NaturalisPurlHandler extends AbstractSpecimenPurlHandler {

  @SuppressWarnings("unused")
  private static final Logger logger = LogManager.getLogger(NaturalisPurlHandler.class);

  public NaturalisPurlHandler(String objectID, HttpServletRequest request, UriInfo uriInfo) {
    super(objectID, request, uriInfo);
  }

  @Override
  protected boolean sourceSystemOK(Specimen specimen) {
    return specimen.getSourceSystem() == CRS || specimen.getSourceSystem() == BRAHMS;
  }

  @Override
  protected Optional<URI> getHtmlLandingPage(Specimen specimen) {
    ConfigObject cfg = Registry.getInstance().getConfig();
    String urlTemplate = cfg.required("bioportal.specimen.url");
    return Optional.of(PurlUtil.createUrl(urlTemplate, "unitID", specimen.getUnitID()));
  }

}
