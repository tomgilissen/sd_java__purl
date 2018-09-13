package nl.naturalis.purl.rest.resource;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.naturalis.nba.utils.StringUtil;
import nl.naturalis.purl.rest.NaturalisPurlHandler;
import nl.naturalis.purl.rest.PurlHandler;
import nl.naturalis.purl.rest.XenoCantoPurlHandler;

/**
 * Central class of the PURL service. Provides endpoints for PURLs, but
 * delegates all the heavy lifting to specialized {@link PurlHandler}s.
 * 
 * @author Ayco Holleman
 *
 */
@Path("/")
public class PurlResource {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(PurlResource.class);

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
	public String welcome() {
		String html = StringUtil.fromInputStream(getClass().getResourceAsStream("welcome.html"));
		return html;
	}

	/**
	 * Endpoint for Naturalis specimen PURLs.
	 * 
	 * @return
	 */
	@GET
	@Path("/naturalis/specimen/{objectID}")
	public Response handleNaturalisSpecimenPurl(@PathParam("objectID") String objectID) {
		PurlHandler handler = new NaturalisPurlHandler(objectID, request, uriInfo);
		return handler.handlePurl();
	}

	/**
	 * Endpoint for Xeno-canto observation PURLs.
	 * 
	 * @return
	 */
	@GET
	@Path("/xeno-canto/observation/{objectID}")
	public Response handleXenoCantoSpecimenPurl(@PathParam("objectID") String objectID) {
		XenoCantoPurlHandler handler = new XenoCantoPurlHandler(objectID, request, uriInfo);
		return handler.handlePurl();
	}

}
