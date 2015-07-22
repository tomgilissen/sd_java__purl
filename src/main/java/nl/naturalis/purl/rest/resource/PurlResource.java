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

/**
 * Central class of the PURL service. Provides endpoints for PURLs, but
 * delegates all the heavy lifting to specialized {@link PurlHandler}s.
 * 
 * @author Ayco Holleman
 * @created Jul 22, 2015
 *
 */
@Path("/")
public class PurlResource {

	@Context
	private HttpServletRequest request;

	@Context
	private UriInfo uriInfo;


	/**
	 * Endpoint for specimen PURLs.
	 * 
	 * @return
	 */
	@GET
	@Path("/naturalis/specimen/{objectID}")
	public Response handleNaturalisSpecimenPurl()
	{
		PurlHandler handler = new SpecimenPurlHandler(request, uriInfo);
		return handler.handlePurl();
	}


	/**
	 * Endpoint for multimedia PURLs.
	 * 
	 * @return
	 */
	@GET
	@Path("/naturalis/multimedia/{objectID}")
	public Response handleNaturalisMultimediaPurl()
	{
		PurlHandler handler = new MultimediaPurlHandler(request, uriInfo);
		return handler.handlePurl();
	}

}
