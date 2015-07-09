package nl.naturalis.purl.rest;

import static nl.naturalis.purl.rest.ContentNegotiator.HTML;
import static nl.naturalis.purl.rest.ContentNegotiator.JPEG;
import static nl.naturalis.purl.rest.ContentNegotiator.JSON;

import java.net.URI;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import nl.naturalis.nda.client.NBAResourceException;
import nl.naturalis.nda.client.SpecimenClient;
import nl.naturalis.nda.domain.Specimen;
import nl.naturalis.purl.util.AppInfo;

/**
 * 
 * @author Ayco Holleman
 * @created Jul 9, 2015
 *
 */
public class SpecimenPurl extends AbstractPurl {

	public SpecimenPurl(String id, MediaType mediaType)
	{
		super(id, mediaType);
	}


	@Override
	public Response redirect() throws NBAResourceException
	{
		if (accept == HTML) {
			return redirectToBioportal();
		}
		if (accept == JSON) {
			return redirectToNba();
		}
		else if (accept == JPEG) {
		}
		return null;
	}


	private Response redirectToBioportal() throws NBAResourceException
	{
		SpecimenClient client = AppInfo.instance().getSpecimenClient();
		Specimen specimen = client.find(objectId);
		return null;
	}


	private Response redirectToNba()
	{
		String url = AppInfo.instance().getConfig().required("nl.naturalis.purl.baseurl.nba");
		url += "/specimen/find/" + objectId;
		return Response.temporaryRedirect(URI.create(url)).build();
	}


	private Response redirectToMedialib()
	{
		return null;
	}


	private String getBioportalUrl() {
		String bioportalBaseUrl = AppInfo.instance().getConfig().required("nl.naturalis.purl.baseurl.bioportal");
		StringBuilder url = new StringBuilder(255);
		//url.
		String nbaBaseUrl = AppInfo.instance().getConfig().required("nl.naturalis.purl.baseurl.nba");
		
		return null;
	}
}
