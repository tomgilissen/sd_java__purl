package nl.naturalis.purl.rest.resource;

import static nl.naturalis.nda.domain.ObjectType.SPECIMEN;
import static nl.naturalis.purl.rest.ContentNegotiator.JPEG;
import static nl.naturalis.purl.rest.ContentNegotiator.JSON;
import static nl.naturalis.purl.rest.ContentNegotiator.OCTETS;

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

import nl.naturalis.nda.client.NBAResourceException;
import nl.naturalis.nda.domain.ObjectType;
import nl.naturalis.purl.rest.ContentNegotiator;
import nl.naturalis.purl.util.AppInfo;

@Path("/")
public class PurlResource {

	@Context
	private UriInfo uriInfo;

	@Context
	private HttpServletRequest request;


	@GET
	@Path("/{institute: (naturalis|floron|voff)}/{objecttype: (specimen|taxon|multimedia)}/{id}")
	@Produces("text/plain;charset=UTF-8")
	public Response handle(@PathParam("institute") String institute, @PathParam("objecttype") String type, @PathParam("id") String id)
	{
		ObjectType objectType = ObjectType.forName(type);
		try {
			if (!resourceExists(objectType, id)) {
				return Response.status(Status.NOT_FOUND).build();
			}
		}
		catch (NBAResourceException e) {
			return Response.serverError().entity(e.getServerInfoAsString()).build();
		}
		ContentNegotiator negotiator = new ContentNegotiator(request, objectType);
		MediaType mediaType = negotiator.negotiate();
		if (mediaType == null) {
			return Response.notAcceptable(negotiator.getAlternatives()).build();
		}
		return null;
	}


	private Response redirect(ObjectType objectType, String id, MediaType mediaType)
	{
		if (mediaType == JPEG || mediaType == OCTETS) {
			return redirectToMedialib(id);
		}
		else if (mediaType == JSON) {
			return redirectToNBA(objectType, id);
		}
		return redirectToBioportal(objectType, id);
	}


	private Response redirectToMedialib(String id)
	{
		return null;
	}


	private Response redirectToNBA(ObjectType objectType, String id)
	{
		return null;
	}


	private Response redirectToBioportal(ObjectType objectType, String id)
	{
		return null;
	}


	private boolean resourceExists(ObjectType objectType, String id) throws NBAResourceException
	{
		if (objectType == SPECIMEN) {
			return AppInfo.instance().getSpecimenClient().exists(id);
		}
		// objectType == MULTIMEDIA
		return AppInfo.instance().getMultiMediaClient().exists(id);
	}

}
