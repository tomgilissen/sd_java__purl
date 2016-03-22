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

import nl.naturalis.nba.api.model.MultiMediaObject;
import nl.naturalis.nba.api.model.ObjectType;
import nl.naturalis.nba.api.model.ServiceAccessPoint;
import nl.naturalis.purl.Registry;

/**
 * A {@link PurlHandler} capable of handling PURLs for multimedia objects.
 * 
 * @author Ayco Holleman
 * @created Jul 22, 2015
 *
 */
public class MultimediaPurlHandler extends AbstractPurlHandler {

	private MultiMediaObject nbaResult;


	public MultimediaPurlHandler(HttpServletRequest request, UriInfo uriInfo)
	{
		super(request, uriInfo);
	}


	@Override
	protected Response doHandle() throws Exception
	{
		nbaResult = Registry.getInstance().getMultiMediaClient().find(objectID);
		if (nbaResult == null) {
			return notFound(ObjectType.MULTIMEDIA, objectID);
		}
		ContentNegotiator negotiator = ContentNegotiatorFactory.getInstance().forMultimedia(accept);
		MediaType mediaType;
		if (negotiator.clientAcceptsRepositoryMediaType()) {
			mediaType = negotiator.negotiate(new MultiMediaObject[] { nbaResult });
		}
		else {
			mediaType = negotiator.negotiate();
		}
		if (mediaType == null) {
			MultiMediaObject[] multimedia = new MultiMediaObject[] { nbaResult };
			if (debug) {
				return notAcceptableDebug(negotiator.getAlternatives(multimedia));
			}
			return notAcceptable(negotiator.getAlternatives(multimedia));
		}
		if (debug) {
			return redirectDebug(getLocation(mediaType));
		}
		return redirect(getLocation(mediaType));
	}


	private URI getLocation(MediaType mediaType)
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
		url.append(urlEncode("multimedia/get-multimedia-object-for-specimen-within-result-set/?unitID="));
		url.append(urlEncode(objectID));
		return URI.create(url.toString());
	}


	private URI getNbaUri()
	{
		StringBuilder url = new StringBuilder(128);
		url.append(Registry.getInstance().getNbaBaseUrl());
		url.append("/multimedia/find/");
		url.append(urlEncode(objectID));
		return URI.create(url.toString());
	}


	private URI getMedialibUri(MediaType requested)
	{
		Set<ServiceAccessPoint.Variant> variants = nbaResult.getServiceAccessPoints().keySet();
		for (ServiceAccessPoint.Variant variant : variants) {
			ServiceAccessPoint sap = nbaResult.getServiceAccessPoints().get(variant);
			// TODO: HACK. Media type not always set. Solve in import!
			String format = sap.getFormat() == null ? JPEG : sap.getFormat();
			MediaType provided = MediaType.valueOf(format);
			if (provided.isCompatible(requested)) {
				return sap.getAccessUri();
			}
		}
		assert (false);
		return null;
	}

}
