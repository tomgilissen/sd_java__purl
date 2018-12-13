package nl.naturalis.purl.rest;

import java.util.Properties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

/**
 * REST resource providing version-related information about the PURL service.
 * 
 * @author Ayco Holleman
 * @created Jul 22, 2015
 *
 */
@Path("/version")
public class VersionResource {

	/**
	 * Show version-related information in plain text format.
	 * 
	 * @param uriInfo
	 * @return
	 */
	@GET
	@Path("/")
	@Produces("text/plain;charset=UTF-8")
	public String show(@Context UriInfo uriInfo)
	{
		// Suppress "method can be declared static" warning
		getClass();
		return "TODO: version-related data";
	}


	/**
	 * Show version-related information in json format.
	 * 
	 * @param uriInfo
	 * @return
	 */
	@GET
	@Path("/json")
	@Produces(MediaType.APPLICATION_JSON)
	public Properties json(@Context UriInfo uriInfo)
	{
		try {
			Properties props = new Properties();
			props.load(getClass().getResourceAsStream("/version.properties"));
			return props;
		}
		catch (Throwable t) {
			//throw ResourceUtil.handleError(uriInfo, t);
			return null;
		}
	}

}
