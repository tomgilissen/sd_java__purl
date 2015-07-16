package nl.naturalis.purl.rest.resource;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/")
public class PurlResource {

	@Context
	private UriInfo uriInfo;

	@Context
	private HttpServletRequest request;

	public PurlResource()
	{
		super();
	}


	@GET
	@Path("/naturalis/specimen/{UnitID}")
	public Response handleNaturalisSpecimenPurl(@PathParam("UnitID") String id)
	{
		return null;
	}



}
