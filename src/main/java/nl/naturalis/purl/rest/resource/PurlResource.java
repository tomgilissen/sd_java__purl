package nl.naturalis.purl.rest.resource;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import nl.naturalis.purl.rest.MultimediaPurlHandler;
import nl.naturalis.purl.rest.PurlHandler;
import nl.naturalis.purl.rest.SpecimenPurlHandler;

@Path("/")
public class PurlResource {

	@Context
	private HttpServletRequest request;

	@Context
	private UriInfo uriInfo;


	@GET
	@Path("/naturalis/specimen/{UnitID}")
	public Response handleNaturalisSpecimenPurl()
	{
		PurlHandler handler = new SpecimenPurlHandler(request, uriInfo);
		return handler.handlePurl();
	}

	@GET
	@Path("/naturalis/multimedia/{UnitID}")
	public Response handleNaturalisMultimediaPurl()
	{
		PurlHandler handler = new MultimediaPurlHandler(request, uriInfo);
		return handler.handlePurl();
	}

}
