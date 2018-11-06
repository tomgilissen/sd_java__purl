package nl.naturalis.purl.rest;

import static nl.naturalis.nba.api.model.SourceSystem.BRAHMS;
import static nl.naturalis.nba.api.model.SourceSystem.CRS;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.naturalis.nba.api.model.Specimen;
import nl.naturalis.nba.utils.ConfigObject;
import nl.naturalis.purl.PurlException;
import nl.naturalis.purl.Registry;

/**
 * A {@link PurlHandler} capable of handling PURLs for specimen objects.
 * 
 * @author Ayco Holleman
 */
public class NaturalisPurlHandler extends AbstractSpecimenPurlHandler {

	private static final Logger logger = LogManager.getLogger(NaturalisPurlHandler.class);

	public NaturalisPurlHandler(String objectID, HttpServletRequest request, UriInfo uriInfo) {
		super(objectID, request, uriInfo);
	}

	@Override
	protected Response doHandle() throws Exception {
		Specimen specimen = getSpecimen();
		if (specimen == null) {
			logger.info("Responding with 404 (Not Found) for unitID \"{}\"", objectID);
			return notFound("specimen", objectID);
		}
		if (!sourceSystemOK(specimen)) {
			logger.info("Responding with 404 (Not Found) for unitID \"{}\" (belongs to another source system: \"{}\")",
					objectID, specimen.getSourceSystem().getCode());
			return notFound("specimen", objectID);
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
		return specimen.getSourceSystem() == CRS || specimen.getSourceSystem() == BRAHMS;
	}

	private URI getLocation(MediaType mediaType, Specimen specimen) throws PurlException {
		if (mediaType.isCompatible(MediaType.TEXT_HTML_TYPE)) {
			return getBioportalUri();
		}
		if (mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
			return getNbaUri();
		}
		Optional<URI> o = findUriForMediaType(mediaType, specimen);
		return o.isPresent() ? o.get() : null;
	}

	private URI getBioportalUri() throws PurlException {
		ConfigObject cfg = Registry.getInstance().getConfig();
		String uriTemplate = cfg.required("bioportal.specimen.url");
		if (!uriTemplate.contains("${unitID}")) {
			throw new PurlException(
					"Missing placeholder \"${unitID}\" in Bioportal URL template (check purl.properties)");
		}
		try {
			return new URI(uriTemplate.replace("${unitID}", objectID));
		} catch (URISyntaxException e) {
			throw new PurlException(e);
		}
	}



}
