package nl.naturalis.purl.rest.resource;

import static nl.naturalis.nda.domain.ObjectType.SPECIMEN;
import static nl.naturalis.purl.rest.ContentNegotiator.JPEG;
import static nl.naturalis.purl.rest.ContentNegotiator.JSON;
import static nl.naturalis.purl.rest.ContentNegotiator.OCTETS;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.domainobject.util.ConfigObject;

import nl.naturalis.nda.client.NBAResourceException;
import nl.naturalis.nda.domain.ObjectType;
import nl.naturalis.nda.domain.Specimen;
import nl.naturalis.purl.rest.ContentNegotiator;
import nl.naturalis.purl.rest.MultimediaPurl;
import nl.naturalis.purl.rest.Purl;
import nl.naturalis.purl.rest.SpecimenPurl;
import nl.naturalis.purl.util.AppInfo;

@Path("/")
public class PurlResource {

	@Context
	private UriInfo uriInfo;

	@Context
	private HttpServletRequest request;

	private final ConfigObject config;


	public PurlResource()
	{
		super();
		config = AppInfo.instance().getConfig();
	}


	@GET
	@Path("/{institute: (naturalis|floron|voff)}/{objecttype: (specimen|taxon|multimedia)}/{objectId}")
	@Produces("text/plain;charset=UTF-8")
	public Response handle(@PathParam("institute") String institute, @PathParam("objecttype") String type, @PathParam("objectId") String id)
	{
		ObjectType objectType = ObjectType.forName(type);
		try {
			if (!resourceExists(objectType, id)) {
				return Response.status(Status.NOT_FOUND).build();
			}
			ContentNegotiator negotiator = new ContentNegotiator(request, objectType);
			MediaType mediaType = negotiator.negotiate();
			if (mediaType == null) {
				return Response.notAcceptable(negotiator.getAlternatives()).build();
			}
		}
		catch (NBAResourceException e) {
			return Response.serverError().entity(e.getServerInfoAsString()).build();
		}
		return null;
	}
	
	@GET
	@Path("/naturalis/specimen/{objectId}")
	public Response handleSpecimenPurl(@PathParam("objectId") String id) {
		return null;
	}
	


	private Response redirect(ObjectType objectType, String id, MediaType mediaType) throws NBAResourceException
	{
		Purl purl = null;
		switch(objectType) {
			case SPECIMEN:
				purl = new SpecimenPurl(id, mediaType);
				break;
			case MULTIMEDIA:
				purl = new MultimediaPurl(id, mediaType);
				break;
			default:
				assert(false);
		}
		return null;
	}


	private static boolean resourceExists(ObjectType objectType, String id) throws NBAResourceException
	{
		if (objectType == SPECIMEN) {
			return AppInfo.instance().getSpecimenClient().exists(id);
		}
		// objectType == MULTIMEDIA
		return AppInfo.instance().getMultiMediaClient().exists(id);
	}

}
