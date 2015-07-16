package nl.naturalis.purl.rest;

import java.net.URI;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import nl.naturalis.nda.client.MultiMediaClient;
import nl.naturalis.nda.client.NBAResourceException;
import nl.naturalis.nda.domain.MultiMediaObject;
import nl.naturalis.nda.domain.ServiceAccessPoint;
import nl.naturalis.purl.util.AppInfo;

/**
 * 
 * @author Ayco Holleman
 * @created Jul 9, 2015
 *
 */
public class SpecimenPurl extends AbstractPurl {

	public SpecimenPurl(String objectId, MediaType[] mediaTypes)
	{
		super(objectId, mediaTypes);
	}

	private MultiMediaObject[] multimedia;


	protected Response doHandle() throws Exception
	{
		if (!AppInfo.instance().getSpecimenClient().exists(localId)) {
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
			MultiMediaClient client = AppInfo.instance().getMultiMediaClient();
			multimedia = client.getMultiMediaForSpecimen(localId);
		}
		return multimedia;
	}


	private URI getBioportalUrl()
	{
		String baseUrl = AppInfo.instance().getConfig().required("bioportal.baseurl");
		StringBuilder url = new StringBuilder(128);
		url.append(baseUrl);
		url.append("/nba/result?nba_request=");
		url.append(urlEncode("specimen/get-specimen/?unitID="));
		url.append(urlEncode(localId));
		return URI.create(url.toString());
	}


	private URI getNbaUrl()
	{
		String baseUrl = AppInfo.instance().getConfig().required("nba.baseurl");
		StringBuilder url = new StringBuilder(128);
		url.append(baseUrl);
		url.append("/specimen/find/");
		url.append(urlEncode(localId));
		return URI.create(url.toString());
	}

}
