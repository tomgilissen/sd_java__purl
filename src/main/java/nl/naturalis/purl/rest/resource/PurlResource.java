package nl.naturalis.purl.rest.resource;

import static nl.naturalis.nda.domain.ObjectType.MULTIMEDIA;
import static nl.naturalis.nda.domain.ObjectType.SPECIMEN;
import static nl.naturalis.nda.domain.ObjectType.TAXON;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;

import nl.naturalis.nda.domain.ObjectType;

@Path("/")
public class PurlResource {

	@Context
	private UriInfo uriInfo;

	@Context
	private HttpServletRequest request;

	/*
	 * Explicitly define all serviceable media type here, without relying on
	 * predefined types like MediaType.TEXT_HTML_TYPE. Comparison of requested
	 * and serviceable media types is done solely based on their type (e.g.
	 * "text") and subtype (e.g. "html"). We don't know exactly how the
	 * predefined mediatypes were defined (e.g. possibly also with a charset
	 * parameter).
	 */
	private static final MediaType HTML = new MediaType("text", "html");
	private static final MediaType JSON = new MediaType("application", "json");
	private static final MediaType JPEG = new MediaType("image", "jpeg");
	private static final MediaType OCTETS = new MediaType("application", "octet-stream");

	private static final Variant HTML_VAR = new Variant(HTML, (String) null, null);
	private static final Variant JSON_VAR = new Variant(JSON, (String) null, null);
	private static final Variant JPEG_VAR = new Variant(JPEG, (String) null, null);
	private static final Variant OCTETS_VAR = new Variant(OCTETS, (String) null, null);

	private static final HashMap<ObjectType, List<MediaType>> map = new HashMap<>(4, 1.0f);
	private static final HashMap<ObjectType, List<Variant>> variants = new HashMap<>(4, 1.0f);

	/*
	 * Build a map of serviceable media types. Per object type the media types
	 * that can and will be served are listed. The first media type in the list
	 * is the default media type (the media type that will we served if there is
	 * no Accept header in the HTTP request). If the requested media type cannot
	 * be served, a 406 (NOT ACCEPTABLE) status code is returned, along with a
	 * list of acceptable alternatives (modeled in JAX-RS by the Variant class).
	 * 
	 * Note that we currently have no PURLs for taxa.
	 */
	static {

		map.put(SPECIMEN, Arrays.asList(HTML, JSON, JPEG));
		map.put(TAXON, Collections.<MediaType> emptyList());
		map.put(MULTIMEDIA, Arrays.asList(JPEG, OCTETS, JSON));

		variants.put(SPECIMEN, Arrays.asList(HTML_VAR, JSON_VAR, JPEG_VAR));
		variants.put(TAXON, Collections.<Variant> emptyList());
		variants.put(MULTIMEDIA, Arrays.asList(JPEG_VAR, OCTETS_VAR, JSON_VAR));

	}


	@GET
	@Path("/{institute: (naturalis|floron|voff)}/{objecttype: (specimen|taxon|multimedia)}/{id}")
	@Produces("text/plain;charset=UTF-8")
	public Response handle(@PathParam("institute") String institute, @PathParam("objecttype") String objectType, @PathParam("id") String id)
	{
		ObjectType ot = ObjectType.forName(objectType);
		MediaType mediaType = negotiate(ot);
		if (mediaType == null) {
			return Response.notAcceptable(variants.get(ot)).build();
		}
		return null;
	}


	private MediaType negotiate(ObjectType objectType)
	{
		MediaType handshake = null;
		List<MediaType> requested = getRequestedMediaTypes();
		if (requested.size() == 0) {
			handshake = map.get(objectType).get(0);
		}
		else {
			for (MediaType mediaType : requested) {
				MediaType temp = new MediaType(mediaType.getType(), mediaType.getSubtype());
				int i = map.get(objectType).indexOf(temp);
				if (i != -1) {
					handshake = map.get(objectType).get(i);
					break;
					/*
					 * TODO: maybe also check, for example, that JSON is not
					 * requested with a non-standard character set.
					 */
				}
			}
		}
		return handshake;
	}


	private List<MediaType> getRequestedMediaTypes()
	{
		List<MediaType> types = new ArrayList<>();
		Enumeration<String> acceptHeaders = request.getHeaders("Accept");
		while (acceptHeaders.hasMoreElements()) {
			try {
				types.add(MediaType.valueOf(acceptHeaders.nextElement()));
			}
			catch (IllegalArgumentException e) {
				// JAX-RS doesn't know how to parse the Accept header
				// We will not be trying to be more intelligent than
				// JAX-RS for now
			}
		}
		return types;
	}

}
