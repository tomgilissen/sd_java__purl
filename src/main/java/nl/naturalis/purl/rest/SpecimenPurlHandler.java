package nl.naturalis.purl.rest;

import static nl.naturalis.purl.rest.ResourceUtil.JPEG;
import static nl.naturalis.purl.rest.ResourceUtil.notAcceptable;
import static nl.naturalis.purl.rest.ResourceUtil.notAcceptableDebug;
import static nl.naturalis.purl.rest.ResourceUtil.notFound;
import static nl.naturalis.purl.rest.ResourceUtil.redirect;
import static nl.naturalis.purl.rest.ResourceUtil.redirectDebug;
import static nl.naturalis.purl.rest.ResourceUtil.urlEncode;

import java.net.URI;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import nl.naturalis.nda.client.NBAResourceException;
import nl.naturalis.nda.client.SpecimenClient;
import nl.naturalis.nda.domain.MultiMediaObject;
import nl.naturalis.nda.domain.ObjectType;
import nl.naturalis.nda.domain.ServiceAccessPoint;
import nl.naturalis.purl.Registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link PurlHandler} capable of handling PURLs for specimen objects.
 * 
 * @author Ayco Holleman
 * @created Jul 9, 2015
 *
 */
public class SpecimenPurlHandler extends AbstractPurlHandler {

	private static final Logger logger = LoggerFactory.getLogger(SpecimenPurlHandler.class);

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
		if (!Registry.getInstance().getSpecimenClient().exists(objectID)) {
			return notFound(ObjectType.SPECIMEN, objectID);
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


	private URI getLocation(MediaType mediaType) throws NBAResourceException
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
		url.append("/specimen/find/");
		url.append(urlEncode(objectID));
		return URI.create(url.toString());
	}


	private URI getMedialibUri(MediaType requested) throws NBAResourceException
	{
		MultiMediaObject[] multimedia = getMultiMedia();
		if (multimedia != null) {
			Set<ServiceAccessPoint.Variant> variants;
			for (MultiMediaObject mmo : multimedia) {
				if (mmo.getServiceAccessPoints() != null) {
					variants = mmo.getServiceAccessPoints().keySet();
					for (ServiceAccessPoint.Variant variant : variants) {
						ServiceAccessPoint sap = mmo.getServiceAccessPoints().get(variant);
						// TODO: HACK. Media type not always set. Solve in import!
						String format = sap.getFormat() == null ? JPEG : sap.getFormat();
						MediaType provided = MediaType.valueOf(format);
						if (provided.isCompatible(requested)) {
							return sap.getAccessUri();
						}
					}
				}
			}
		}
		/*
		 * Because the content negotiator has iterated through the exact same
		 * multimedia objects to see which media types are available for the
		 * requested object, and if one of them matches what the client wants,
		 * we should never get here.
		 */
		assert (false);
		return null;
	}


	private MultiMediaObject[] getMultiMedia() throws NBAResourceException
	{
		if (multimedia == null) {
			SpecimenClient client = Registry.getInstance().getSpecimenClient();
			logger.info("Retrieving multimedia for specimen with UnitID " + objectID);
			multimedia = client.getMultiMedia(objectID);
			logger.info("Number of multimedia found: " + multimedia.length);
			if (logger.isDebugEnabled()) {
				logger.debug(ResourceUtil.dump(multimedia));
			}
		}
		return multimedia;
	}

}
