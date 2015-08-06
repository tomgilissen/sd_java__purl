package nl.naturalis.purl.rest.resource;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import nl.naturalis.purl.rest.PurlHandler;
import nl.naturalis.purl.rest.ResourceUtil;
import nl.naturalis.purl.rest.SpecimenPurlHandler;

import org.domainobject.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(PurlResource.class);

	@Context
	private HttpServletRequest request;

	@Context
	private UriInfo uriInfo;


	/**
	 * Show some welcome content at the root.
	 * 
	 * @return
	 */
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String welcome()
	{
		String html = StringUtil.fromInputStream(getClass().getResourceAsStream("welcome.html"));
		String baseUri = uriInfo.getBaseUri().toString();
		if (!baseUri.endsWith("/")) {
			baseUri = baseUri + "/";
		}
		html = html.replaceAll("@BASEURL@", baseUri);
		return html;
	}


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
	@SuppressWarnings("static-method")
	public Response handleNaturalisMultimediaPurl()
	{
		return ResourceUtil.serverError("Under construction");
//		PurlHandler handler = new MultimediaPurlHandler(request, uriInfo);
//		return handler.handlePurl();
	}

}
