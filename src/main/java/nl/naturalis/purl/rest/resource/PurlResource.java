package nl.naturalis.purl.rest.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.domainobject.util.debug.BeanPrinter;

@Path("/(naturalis|voff|floron)")
public class PurlResource {

	@GET
	@Path("/{objecttype}/{id}")
	public Response handle(UriInfo request)
	{
		MultivaluedMap<String,String> params = request.getPathParameters();
		BeanPrinter.out(params);
		return null;
	}

}
