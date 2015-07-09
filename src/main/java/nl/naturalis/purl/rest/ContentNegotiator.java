package nl.naturalis.purl.rest;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Variant;

import nl.naturalis.nda.domain.ObjectType;

/**
 * A {@code ContentNegotiator} establishes the type of content to be served by
 * comparing the Accept headers in the request with the serviceable content
 * types for a given resource (specimen, multimedia, etc.).
 * 
 * @author Ayco Holleman
 *
 */
public class ContentNegotiator {

	/*
	 * Explicitly define all potentially served media types here, without
	 * relying on predefined types like MediaType.TEXT_HTML_TYPE. Comparison of
	 * requested and serviceable media types is done solely based on their type
	 * (e.g. "text") and subtype (e.g. "html"). We don't know exactly how the
	 * predefined mediatypes were defined (e.g. possibly also with a charset
	 * parameter).
	 */
	public static final MediaType HTML = new MediaType("text", "html");
	public static final MediaType JSON = new MediaType("application", "json");
	public static final MediaType JPEG = new MediaType("image", "jpeg");
	public static final MediaType OCTETS = new MediaType("application", "octet-stream");

	public static final Variant HTML_VAR = new Variant(HTML, (String) null, null);
	public static final Variant JSON_VAR = new Variant(JSON, (String) null, null);
	public static final Variant JPEG_VAR = new Variant(JPEG, (String) null, null);
	public static final Variant OCTETS_VAR = new Variant(OCTETS, (String) null, null);

	private static final HashMap<ObjectType, List<MediaType>> map = new HashMap<>(4, 1.0f);
	private static final HashMap<ObjectType, List<Variant>> variants = new HashMap<>(4, 1.0f);

	/*
	 * Define serviceable media types per object type. Per object type the media
	 * types that can and will be served are listed. The first media type in the
	 * list is the default media type (the media type that will we served if
	 * there is no Accept header in the HTTP request). If the requested media
	 * type cannot be served, a 406 (NOT ACCEPTABLE) status code is returned,
	 * along with a list of acceptable alternatives (modeled in JAX-RS by the
	 * Variant class).
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

	private final HttpServletRequest request;
	private final ObjectType objectType;


	public ContentNegotiator(HttpServletRequest request, ObjectType objectType)
	{
		this.request = request;
		this.objectType = objectType;
	}


	public MediaType negotiate()
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


	public List<Variant> getAlternatives()
	{
		return variants.get(objectType);
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
