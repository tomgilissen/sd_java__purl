package nl.naturalis.purl.rest;

import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriInfo;

import org.apache.http.client.utils.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.naturalis.nba.api.model.Specimen;
import nl.naturalis.nba.client.SpecimenClient;
import nl.naturalis.nba.utils.StringUtil;
import nl.naturalis.purl.PurlException;
import nl.naturalis.purl.Registry;

public abstract class AbstractSpecimenPurlHandler extends AbstractPurlHandler {

	private static final Logger logger = LogManager.getLogger(AbstractSpecimenPurlHandler.class);
	
	public AbstractSpecimenPurlHandler(String objectID, HttpServletRequest request, UriInfo uriInfo) {
		super(objectID, request, uriInfo);
	}	

	protected Specimen getSpecimen() throws PurlException {
		logger.info("Retrieving specimen with UnitID " + objectID);
		SpecimenClient client = Registry.getInstance().getSpecimenClient();
		Specimen[] specimens = client.findByUnitID(objectID);
		if (specimens.length == 0) {
			return null;
		}
		if (specimens.length > 1) {
			throw new PurlException("Duplicate unitID: " + objectID);
		}
		return specimens[0];
	}

	protected URI getNbaUri() throws PurlException {
		String baseUrl = Registry.getInstance().getNbaBaseUrl();
		URIBuilder ub;
		try {
			ub = new URIBuilder(baseUrl);
		} catch (URISyntaxException e) {
			throw new PurlException("Invalid value for nba.baseurl (check purl.properties)");
		}
		String rootPath = ub.getPath();
		StringBuilder fullPath = new StringBuilder(50);
		if (rootPath != null) {
			fullPath.append(StringUtil.rtrim(rootPath, '/'));
		}
		fullPath.append("/specimen/findByUnitID/");
		fullPath.append(objectID);
		ub.setPath(fullPath.toString());
		try {
			return ub.build();
		} catch (URISyntaxException e) {
			throw new PurlException(e);
		}
	}
}
