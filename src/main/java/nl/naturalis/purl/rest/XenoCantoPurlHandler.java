package nl.naturalis.purl.rest;

import static nl.naturalis.nba.api.model.SourceSystem.XC;
import static nl.naturalis.purl.rest.ContentNegotiatorUtil.findUriForMediaType;
import static nl.naturalis.purl.rest.ContentNegotiatorUtil.getAvailableMultiMediaTypes;
import static nl.naturalis.purl.rest.ContentNegotiatorUtil.getRequestedMediaTypes;
import static nl.naturalis.purl.rest.ResourceUtil.load;
import static nl.naturalis.purl.rest.ResourceUtil.notAcceptable;
import static nl.naturalis.purl.rest.ResourceUtil.notFound;
import static nl.naturalis.purl.rest.ResourceUtil.redirect;
import static nl.naturalis.purl.rest.ResourceUtil.redirectDebug;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;

import org.apache.http.client.utils.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.naturalis.nba.api.InvalidQueryException;
import nl.naturalis.nba.api.QueryCondition;
import nl.naturalis.nba.api.QueryResult;
import nl.naturalis.nba.api.QueryResultItem;
import nl.naturalis.nba.api.QuerySpec;
import nl.naturalis.nba.api.model.MultiMediaObject;
import nl.naturalis.nba.api.model.Specimen;
import nl.naturalis.nba.client.MultiMediaObjectClient;
import nl.naturalis.nba.client.SpecimenClient;
import nl.naturalis.nba.utils.ConfigObject;
import nl.naturalis.nba.utils.StringUtil;
import nl.naturalis.purl.PurlException;
import nl.naturalis.purl.Registry;

/**
 * A {@link PurlHandler} capable of handling PURLs for specimen objects.
 * 
 * @author Ayco Holleman
 */
public class XenoCantoPurlHandler extends AbstractPurlHandler {

	private static final Logger logger = LogManager.getLogger(XenoCantoPurlHandler.class);

	public XenoCantoPurlHandler(String objectID, HttpServletRequest request, UriInfo uriInfo) {
		super(objectID, request, uriInfo);
	}

	/**
	 * @see AbstractPurlHandler#doHandle()
	 */
	@Override
	protected Response doHandle() throws Exception {
		Specimen specimen = getSpecimen();
		if (specimen == null) {
			logger.info("Responding with 404 (Not Found) for unitID \"{}\"", objectID);
			return notFound("Specimen", objectID);
		}
		if (!sourceSystemOK(specimen)) {
			logger.info("Responding with 404 (Not Found) for unitID \"{}\" (belongs to another source system: \"{}\")",
					objectID, specimen.getSourceSystem().getCode());
			return notFound("Specimen", objectID);
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

	protected boolean sourceSystemOK(Specimen specimen) {
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

	private URI getXenoCantoUri(Specimen specimen) throws PurlException {
		ConfigObject cfg = Registry.getInstance().getConfig();
		String uriTemplate = cfg.required("xenocanto.observation.url");
		if (!uriTemplate.contains("${sourceSystemId}")) {
			throw new PurlException(
					"Missing placeholder \"${sourceSystemId}\" in Xeno-canto URL template (check purl.properties)");
		}
		try {
			return new URI(uriTemplate.replace("${sourceSystemId}", specimen.getSourceSystemId()));
		} catch (URISyntaxException e) {
			throw new PurlException(e);
		}
	}

	private URI getNbaUri() throws PurlException {
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

	private Specimen getSpecimen() throws PurlException {
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

	@SuppressWarnings("unused")
	private MultiMediaObject[] getMultiMedia(Specimen specimen) throws PurlException {
		logger.info("Retrieving multimedia for specimen with UnitID " + objectID);
		MultiMediaObjectClient client = Registry.getInstance().getMultiMediaClient();
		String field = "associatedSpecimenReference";
		String value = specimen.getId();
		QueryCondition condition = new QueryCondition(field, "=", value);
		QuerySpec query = new QuerySpec();
		query.setConstantScore(true);
		query.addCondition(condition);
		QueryResult<MultiMediaObject> result;
		try {
			result = client.query(query);
		} catch (InvalidQueryException e) {
			throw new PurlException(e);
		}
		MultiMediaObject[] multimedia = new MultiMediaObject[result.size()];
		int i = 0;
		for (QueryResultItem<MultiMediaObject> qri : result) {
			multimedia[i++] = qri.getItem();
		}
		logger.info("Number of multimedia found: " + multimedia.length);
		return multimedia;
	}

}
