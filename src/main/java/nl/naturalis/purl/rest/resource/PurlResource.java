package nl.naturalis.purl.rest.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.domainobject.util.debug.BeanPrinter;

@Path("/")
public class PurlResource {

	@GET
	@Path("/{institute: (naturalis|floron|voff)}/{objecttype}/{id}")
	@Produces("text/plain;charset=UTF-8")
	public String handle(@Context UriInfo request)
	{
		MultivaluedMap<String,String> params = request.getPathParameters();
		BeanPrinter.out(params);
		return request.getBaseUri().toString();
	}

}
