package nl.naturalis.purl.rest;

import static nl.naturalis.purl.rest.ResourceUtil.JPEG;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Variant;

import nl.naturalis.nda.domain.MultiMediaObject;
import nl.naturalis.nda.domain.ServiceAccessPoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@code ContentNegotiator} establishes the type of content to be served to
 * the client by comparing Accept headers with the actually available content
 * types for the requested object. N.B. content type is more formally known as
 * media type.
 * 
 * @author Ayco Holleman
 *
 */
public class ContentNegotiator {

	private static final Logger logger = LoggerFactory.getLogger(ContentNegotiator.class);


	/**
	 * Retrieve Accept headers from the HTTP request and convert them to an
	 * array of {@code MediaType} instances. Note that clients can supply
	 * multiple Accept headers, but they can also supply one Accept header with
	 * a comma-separated list of media types, or they could do both. For debug
	 * purposes you can mimic Accept headers by adding an "__accept" query
	 * parameter to the PURL. If you do this, the actual Accept headers (if any)
	 * will be ignored.
	 * 
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static MediaType[] getRequestedMediaTypes(HttpServletRequest request)
	{
		String acceptParam = request.getParameter("__accept");
		if (acceptParam != null) {
			return getRequestedMediaTypesDebug(acceptParam);
		}
		List<MediaType> types = new ArrayList<>();
		Enumeration<String> acceptHeaders = request.getHeaders("Accept");
		while (acceptHeaders.hasMoreElements()) {
			String acceptHeader = acceptHeaders.nextElement();
			String[] mediaTypes = acceptHeader.split(",");
			for (String one : mediaTypes) {
				try {
					types.add(MediaType.valueOf(one));
				}
				catch (IllegalArgumentException e) {
					if (mediaTypes.length == 1) {
						logger.warn("Invalid Accept header in request: \"" + one + "\" (ignored)");
					}
					else {
						logger.warn("Invalid media type in Accept header: \"" + one + "\" (ignored)");
					}
				}
			}
		}
		return types.toArray(new MediaType[types.size()]);
	}

	private MediaType[] requestedMediaTypes;
	private MediaType[] generatedMediaTypes;
	private MediaType[] repositoryMediaTypes;


	/*
	 * Package visibility. You must use a ContentNegotiatorFactory to get hold
	 * of a ContentNegotiator.
	 */
	ContentNegotiator()
	{

	}


	void setAccept(MediaType[] accept)
	{
		this.requestedMediaTypes = accept;
	}


	void setGeneratedMediaTypes(MediaType[] basicMediaTypes)
	{
		this.generatedMediaTypes = basicMediaTypes;
	}


	void setRepositoryMediaTypes(MediaType[] repositoryMediaTypes)
	{
		this.repositoryMediaTypes = repositoryMediaTypes;
	}


	/**
	 * Establish the media type to be served. This version of {@code negotiate}
	 * assumes that the client has not passed any Accept headers denoting a
	 * media type that is served by the medialib (or some other multimedia
	 * repository).
	 * 
	 * @return
	 */
	public MediaType negotiate()
	{
		if (requestedMediaTypes.length == 0) {
			// No Accept header in request
			return generatedMediaTypes[0];
		}
		for (MediaType requested : requestedMediaTypes) {
			for (MediaType provided : generatedMediaTypes) {
				if (requested.isCompatible(provided)) {
					return provided;
				}
			}
		}
		return null;
	}


	/**
	 * Establish the media type to be served. This version of {@code negotiate}
	 * assumes that the client passed at least one Accept header denoting a
	 * media type that is served by the medialib (or some other multimedia
	 * repository). In that case we must first check which media types exist for
	 * the object specified by the PURL.
	 * 
	 * @param multimedia
	 *            The available multimedia for the object for which we are
	 *            trying to establish the preferred representation
	 * @return
	 */
	public MediaType negotiate(MultiMediaObject[] multimedia)
	{
		if (requestedMediaTypes.length == 0) {
			return generatedMediaTypes[0];
		}
		Set<MediaType> repoMediaTypes = extractMediaTypes(multimedia);
		for (MediaType requested : requestedMediaTypes) {
			for (MediaType provided : generatedMediaTypes) {
				if (requested.isCompatible(provided)) {
					return provided;
				}
			}
			for (MediaType provided : repoMediaTypes) {
				if (requested.isCompatible(provided)) {
					return provided;
				}
			}
		}
		return null;
	}


	/**
	 * Is there at least one Accept header in the PURL request that specifies a
	 * media type that is served from the medialib or some other image
	 * repository?
	 * 
	 * @return
	 */
	public boolean clientAcceptsRepositoryMediaType()
	{
		for (MediaType requested : requestedMediaTypes) {
			for (MediaType repoType : repositoryMediaTypes) {
				if (repoType.isCompatible(requested)) {
					return true;
				}
			}
		}
		return false;
	}


	/**
	 * Provide alternative media types if the client has requested a media type
	 * that we cannot serve.
	 * 
	 * @param multimedia
	 *            The available multimedia for the object requested by the
	 *            client
	 * @return
	 */
	public List<Variant> getAlternatives(MultiMediaObject[] multimedia)
	{
		List<Variant> alternatives = new ArrayList<>();
		for (MediaType t : generatedMediaTypes) {
			alternatives.add(new Variant(t, (String) null, null));
		}
		for (MediaType t : extractMediaTypes(multimedia)) {
			alternatives.add(new Variant(t, (String) null, null));
		}
		return alternatives;
	}


	/*
	 * Extract the serviceAccessPoints.format field from the specified
	 * MultiMediaObject instances.
	 */
	private static Set<MediaType> extractMediaTypes(MultiMediaObject[] multimedia)
	{
		Set<MediaType> mediaTypes = new LinkedHashSet<>();
		for (MultiMediaObject mmo : multimedia) {
			if (mmo.getServiceAccessPoints() != null) {
				Set<ServiceAccessPoint.Variant> variants = mmo.getServiceAccessPoints().keySet();
				for (ServiceAccessPoint.Variant variant : variants) {
					if (mmo.getServiceAccessPoints() != null) {
						ServiceAccessPoint sap = mmo.getServiceAccessPoints().get(variant);
						// TODO: HACK. Media type not always set. Solve in import!
						String format = sap.getFormat() == null ? JPEG : sap.getFormat();
						mediaTypes.add(MediaType.valueOf(format));
					}
				}
			}
		}
		return mediaTypes;
	}


	private static MediaType[] getRequestedMediaTypesDebug(String requestParam)
	{
		String[] chunks = requestParam.split(",");
		List<MediaType> types = new ArrayList<>(chunks.length);
		for (String chunk : chunks) {
			try {
				types.add(MediaType.valueOf(chunk));
			}
			catch (IllegalArgumentException e) {
				logger.warn("Invalid media type in __accept parameter: \"" + chunk + "\" (ignored)");
			}
		}
		return types.toArray(new MediaType[types.size()]);
	}

}
