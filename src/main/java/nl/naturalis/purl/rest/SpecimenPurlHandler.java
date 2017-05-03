package nl.naturalis.purl.rest;

import static nl.naturalis.purl.rest.ResourceUtil.JPEG;
import static nl.naturalis.purl.rest.ResourceUtil.notAcceptable;
import static nl.naturalis.purl.rest.ResourceUtil.notAcceptableDebug;
import static nl.naturalis.purl.rest.ResourceUtil.notFound;
import static nl.naturalis.purl.rest.ResourceUtil.redirect;
import static nl.naturalis.purl.rest.ResourceUtil.redirectDebug;
import static nl.naturalis.purl.rest.ResourceUtil.urlEncode;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.naturalis.nba.api.InvalidQueryException;
import nl.naturalis.nba.api.QueryCondition;
import nl.naturalis.nba.api.QueryResult;
import nl.naturalis.nba.api.QueryResultItem;
import nl.naturalis.nba.api.QuerySpec;
import nl.naturalis.nba.api.model.MultiMediaObject;
import nl.naturalis.nba.api.model.ServiceAccessPoint;
import nl.naturalis.nba.api.model.Specimen;
import nl.naturalis.nba.client.MultiMediaObjectClient;
import nl.naturalis.nba.client.ServerException;
import nl.naturalis.nba.client.SpecimenClient;
import nl.naturalis.purl.PurlException;
import nl.naturalis.purl.Registry;

/**
 * A {@link PurlHandler} capable of handling PURLs for specimen objects.
 * 
 * @author Ayco Holleman
 * @created Jul 9, 2015
 *
 */
public class SpecimenPurlHandler extends AbstractPurlHandler {

	private static final Logger logger = LoggerFactory.getLogger(SpecimenPurlHandler.class);

	private static final Specimen DUMMY_SPECIMEN = new Specimen();

	private Specimen specimen = DUMMY_SPECIMEN;
	private MultiMediaObject[] multimedia;

	public SpecimenPurlHandler(HttpServletRequest request, UriInfo uriInfo)
	{
		super(request, uriInfo);
	}

	/**
	 * @see AbstractPurlHandler#doHandle()
	 */
	@Override
	protected Response doHandle() throws Exception
	{

		if (getSpecimen() == null) {
			return notFound("Specimen", objectID);
		}
		ContentNegotiator negotiator = ContentNegotiatorFactory.getInstance().forSpecimens(accept);
		MediaType mediaType;
		if (negotiator.clientAcceptsRepositoryMediaType()) {
			mediaType = negotiator.negotiate(getMultiMedia());
		}
		else {
			mediaType = negotiator.negotiate();
		}
		if (mediaType == null) {
			if (debug) {
				return notAcceptableDebug(negotiator.getAlternatives(getMultiMedia()));
			}
			return notAcceptable(negotiator.getAlternatives(getMultiMedia()));
		}
		if (debug) {
			return redirectDebug(getLocation(mediaType));
		}
		return redirect(getLocation(mediaType));
	}

	private URI getLocation(MediaType mediaType) throws ServerException
	{
		if (mediaType.isCompatible(MediaType.TEXT_HTML_TYPE)) {
			return getBioportalUri();
		}
		if (mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
			return getNbaUri();
		}
		return getMedialibUri(mediaType);
	}

	private URI getBioportalUri()
	{
		StringBuilder url = new StringBuilder(128);
		url.append(Registry.getInstance().getBioportalBaseUrl());
		url.append("/nba/result?nba_request=");
		url.append(urlEncode("specimen/get-specimen/?unitID="));
		url.append(urlEncode(objectID));
		return URI.create(url.toString());
	}

	private URI getNbaUri()
	{
		StringBuilder url = new StringBuilder(128);
		url.append(Registry.getInstance().getNbaBaseUrl());
		url.append("/specimen/findByUnitID/");
		url.append(urlEncode(objectID));
		return URI.create(url.toString());
	}

	private URI getMedialibUri(MediaType requested) throws ServerException
	{
		MultiMediaObject[] multimedia = getMultiMedia();
		if (multimedia != null) {
			for (MultiMediaObject mmo : multimedia) {
				if (mmo.getServiceAccessPoints() != null) {
					for (ServiceAccessPoint sap : mmo.getServiceAccessPoints()) {
						// TODO: HACK. Media type not always set.
						String format = sap.getFormat() == null ? JPEG : sap.getFormat();
						MediaType available = MediaType.valueOf(format);
						if (available.isCompatible(requested)) {
							return sap.getAccessUri();
						}
					}
				}
			}
		}
		/*
		 * Because the content negotiator has iterated over the exact same
		 * multimedia objects to see which media types are available for the
		 * requested object, and to check if one of them matches what the client
		 * wants, we should never get here.
		 */
		assert (false);
		return null;
	}

	private Specimen getSpecimen() throws PurlException
	{
		if (specimen == DUMMY_SPECIMEN) {
			logger.info("Retrieving specimen with UnitID " + objectID);
			SpecimenClient client = Registry.getInstance().getSpecimenClient();
			Specimen[] specimens = client.findByUnitID(objectID);
			if (specimens.length == 0) {
				specimen = null;
			}
			else if (specimens.length > 1) {
				throw new PurlException("Duplicate unitID: " + objectID);
			}
			else {
				specimen = specimens[0];
			}
		}
		return specimen;
	}

	private MultiMediaObject[] getMultiMedia()
	{
		if (multimedia == null) {
			logger.info("Retrieving multimedia for specimen with UnitID " + objectID);
			MultiMediaObjectClient client = Registry.getInstance().getMultiMediaClient();
			String field = "associatedSpecimenReference";
			String value = specimen.getId();
			QueryCondition qc = new QueryCondition(field, "=", value);
			QuerySpec qs = new QuerySpec();
			qs.setConstantScore(true);
			qs.addCondition(qc);
			QueryResult<MultiMediaObject> result;
			try {
				result = client.query(qs);
			}
			catch (InvalidQueryException e) {
				assert (false);
				return null;
			}
			multimedia = new MultiMediaObject[result.size()];
			int i = 0;
			for (QueryResultItem<MultiMediaObject> qri : result) {
				multimedia[i++] = qri.getItem();
			}
			logger.info("Number of multimedia found: " + multimedia.length);
		}
		return multimedia;
	}

}
