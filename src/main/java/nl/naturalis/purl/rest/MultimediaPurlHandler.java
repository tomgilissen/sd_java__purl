package nl.naturalis.purl.rest;

import static nl.naturalis.purl.rest.ResourceUtil.notFound;
import static nl.naturalis.purl.rest.ResourceUtil.notAcceptableDebug;

import java.net.URI;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import nl.naturalis.nda.client.NBAResourceException;
import nl.naturalis.nda.domain.MultiMediaObject;
import nl.naturalis.nda.domain.ObjectType;
import nl.naturalis.nda.domain.ServiceAccessPoint;
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


	/**
	 * @see AbstractPurlHandler#doHandle()
	 */
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
			return Response.notAcceptable(negotiator.getAlternatives(multimedia)).build();
		}
		return Response.temporaryRedirect(getLocation(mediaType)).build();
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
		url.append(ResourceUtil.urlEncode("multimedia/get-multimedia/?unitID="));
		url.append(ResourceUtil.urlEncode(objectID));
		return URI.create(url.toString());
	}


	private URI getNbaUri()
	{
		StringBuilder url = new StringBuilder(128);
		url.append(Registry.getInstance().getNbaBaseUrl());
		url.append("/multimedia/find/");
		url.append(ResourceUtil.urlEncode(objectID));
		return URI.create(url.toString());
	}


	private URI getMedialibUri(MediaType mediaType) throws NBAResourceException
	{
		Set<ServiceAccessPoint.Variant> variants = nbaResult.getServiceAccessPoints().keySet();
		for (ServiceAccessPoint.Variant variant : variants) {
			ServiceAccessPoint sap = nbaResult.getServiceAccessPoints().get(variant);
			MediaType sapMediaType = MediaType.valueOf(sap.getFormat());
			if (sapMediaType.equals(mediaType)) {
				return sap.getAccessUri();
			}
		}
		assert (false);
		return null;
	}

}
