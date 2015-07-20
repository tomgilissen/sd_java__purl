package nl.naturalis.purl.rest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import nl.naturalis.nda.domain.MultiMediaObject;
import nl.naturalis.purl.util.Registry;

public class MultimediaPurlHandler extends AbstractPurlHandler {

	private MultiMediaObject multimedia;


	public MultimediaPurlHandler(HttpServletRequest request, UriInfo uriInfo)
	{
		super(request, uriInfo);
	}


	@Override
	protected Response doHandle() throws Exception
	{
		multimedia = Registry.getInstance().getMultiMediaClient().find(objectID);
		if (multimedia == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		ContentNegotiator negotiator = ContentNegotiatorFactory.getInstance().forMultimedia(accept);
		return null;
	}

	//private

}
