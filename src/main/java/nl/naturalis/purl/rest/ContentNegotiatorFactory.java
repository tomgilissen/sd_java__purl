package nl.naturalis.purl.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import nl.naturalis.purl.Registry;

import org.domainobject.util.ConfigObject;

/**
 * A {@code ContentNegotiatorFactory} handles the configuration and creation of
 * {@link ContentNegotiator}s. For each type of object (specimens, multimedia ,
 * etc.) you can request a tailor-made {@code ContentNegotiator}.
 * 
 * @author Ayco Holleman
 * @created Jul 16, 2015
 *
 */
public class ContentNegotiatorFactory {

	private static ContentNegotiatorFactory instance;


	/**
	 * Return a {@code ContentNegotiatorFactory} instance.
	 * 
	 * @return
	 */
	public static ContentNegotiatorFactory getInstance()
	{
		if (instance == null) {
			instance = new ContentNegotiatorFactory();
		}
		return instance;
	}

	private final MediaType[] generatedSpecimenMediaTypes;
	private final MediaType[] generatedMultiMediaMediaTypes;
	private final MediaType[] repositoryMediaTypes;


	private ContentNegotiatorFactory()
	{
		generatedSpecimenMediaTypes = getGeneratedMediaTypes("specimen");
		generatedMultiMediaMediaTypes = getGeneratedMediaTypes("multimedia");
		repositoryMediaTypes = getRepositoryMediaTypes();
	}


	/**
	 * Get a content negotiator for specimen PURLs.
	 * 
	 * @return
	 */
	public ContentNegotiator forSpecimens(MediaType[] accept)
	{
		ContentNegotiator negotiator = new ContentNegotiator();
		negotiator.setGeneratedMediaTypes(generatedSpecimenMediaTypes);
		negotiator.setRepositoryMediaTypes(repositoryMediaTypes);
		negotiator.setAccept(accept);
		return negotiator;
	}


	/**
	 * Get a content negotiator for multimedia PURLs.
	 * 
	 * @return
	 */
	public ContentNegotiator forMultimedia(MediaType[] accept)
	{
		ContentNegotiator negotiator = new ContentNegotiator();
		negotiator.setGeneratedMediaTypes(generatedMultiMediaMediaTypes);
		negotiator.setRepositoryMediaTypes(repositoryMediaTypes);
		negotiator.setAccept(accept);
		return negotiator;
	}


	/*
	 * Returns media types like text/html, application/json and application/xml.
	 * In other words, mostly structured media types with content that is
	 * generated on demand by bespoke systems like the NBA and the BioPortal.
	 */
	private static MediaType[] getGeneratedMediaTypes(String prefix)
	{
		List<MediaType> types = new ArrayList<>();
		ConfigObject config = Registry.getInstance().getConfig();
		types.add(MediaType.valueOf(config.required(prefix + ".purl.accept.default")));
		for (int i = 0;; ++i) {
			String key = prefix + ".purl.accept." + i;
			String val = config.get(key);
			if (val == null) {
				break;
			}
			types.add(MediaType.valueOf(val));
		}
		return types.toArray(new MediaType[types.size()]);
	}


	/*
	 * Returns media types like image/* and video/*. In other words, mostly
	 * binary media types that are served as is by multimedia servers like the
	 * medialib.
	 */
	private static MediaType[] getRepositoryMediaTypes()
	{
		List<MediaType> types = new ArrayList<>();
		ConfigObject config = Registry.getInstance().getConfig();
		for (int i = 0;; ++i) {
			String key = "medialib.mediatypes." + i;
			String val = config.get(key);
			if (val == null) {
				break;
			}
			types.add(MediaType.valueOf(val));
		}
		return types.toArray(new MediaType[types.size()]);
	}

}
