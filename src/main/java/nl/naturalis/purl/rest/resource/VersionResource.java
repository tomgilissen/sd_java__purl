package nl.naturalis.purl.rest.resource;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;


@Path("/version")
public class VersionResource {

	@GET
	@Path("/")
	@Produces("text/plain;charset=UTF-8")
	public String show(@Context UriInfo uriInfo)
	{
		return "TODO: version-related data";
//		try {
//			Properties props = new Properties();
//			props.load(getClass().getResourceAsStream("/version.properties"));
//			StringWriter sw = new StringWriter(128);
//			PrintWriter pw = new PrintWriter(sw);
//			pw.println("Netherlands Biodiversity API (NBA)");
//			pw.println();
//			String version = props.getProperty("git.tag");
//			pw.println("Version: " + version);
//			pw.println("Build date: " + props.getProperty("built"));
//			pw.println("Git branch: " + props.getProperty("git.branch"));
//			pw.println("Git commit: " + props.getProperty("git.commit"));
//			return sw.toString();
//		}
//		catch (Throwable t) {
//			//throw ResourceUtil.handleError(uriInfo, t);
//			return null;
//		}
	}


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
