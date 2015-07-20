package nl.naturalis.purl.rest;

import java.net.URI;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import nl.naturalis.nda.client.MultiMediaClient;
import nl.naturalis.nda.client.NBAResourceException;
import nl.naturalis.nda.domain.MultiMediaObject;
import nl.naturalis.nda.domain.ServiceAccessPoint;
import nl.naturalis.purl.util.Registry;

/**
 * 
 * @author Ayco Holleman
 * @created Jul 9, 2015
 *
 */
public class SpecimenPurlHandler extends AbstractPurlHandler {

	private MultiMediaObject[] multimedia;


	public SpecimenPurlHandler(HttpServletRequest request, UriInfo uriInfo)
	{
		super(request, uriInfo);
	}


	protected Response doHandle() throws Exception
	{
		if (!Registry.getInstance().getSpecimenClient().exists(objectID)) {
			return Response.status(Status.NOT_FOUND).build();
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
			return Response.notAcceptable(negotiator.getAlternatives(getMultiMedia())).build();
		}
		if (mediaType.equals(MediaType.TEXT_HTML_TYPE)) {
			return Response.temporaryRedirect(getBioportalUrl()).build();
		}
		else if (mediaType.equals(MediaType.APPLICATION_JSON_TYPE)) {
			return Response.temporaryRedirect(getNbaUrl()).build();
		}
		else {
			Response response = redirectToMedialib(mediaType);
			assert (response != null);
			return response;
		}
	}


	private Response redirectToMedialib(MediaType mediaType) throws NBAResourceException
	{
		MultiMediaObject[] multimedia = getMultiMedia();
		if (multimedia != null) {
			Set<ServiceAccessPoint.Variant> variants;
			for (MultiMediaObject mmo : multimedia) {
				if (mmo.getServiceAccessPoints() != null) {
					variants = mmo.getServiceAccessPoints().keySet();
					for (ServiceAccessPoint.Variant variant : variants) {
						ServiceAccessPoint sap = mmo.getServiceAccessPoints().get(variant);
						MediaType sapMediaType = MediaType.valueOf(sap.getFormat());
						if (sapMediaType.equals(mediaType)) {
							return Response.temporaryRedirect(sap.getAccessUri()).build();
						}
					}
				}
			}
		}
		return null;
	}


	private MultiMediaObject[] getMultiMedia() throws NBAResourceException
	{
		if (multimedia == null) {
			MultiMediaClient client = Registry.getInstance().getMultiMediaClient();
			multimedia = client.getMultiMediaForSpecimen(objectID);
		}
		return multimedia;
	}


	private URI getBioportalUrl()
	{
		String baseUrl = Registry.getInstance().getConfig().required("bioportal.baseurl");
		StringBuilder url = new StringBuilder(128);
		url.append(baseUrl);
		url.append("/nba/result?nba_request=");
		url.append(urlEncode("specimen/get-specimen/?unitID="));
		url.append(urlEncode(objectID));
		return URI.create(url.toString());
	}


	private URI getNbaUrl()
	{
		String baseUrl = Registry.getInstance().getConfig().required("nba.baseurl");
		StringBuilder url = new StringBuilder(128);
		url.append(baseUrl);
		url.append("/specimen/find/");
		url.append(urlEncode(objectID));
		return URI.create(url.toString());
	}

}
