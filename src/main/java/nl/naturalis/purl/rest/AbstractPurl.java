package nl.naturalis.purl.rest;

import javax.ws.rs.core.MediaType;

/**
 * 
 * @author Ayco Holleman
 * @created Jul 9, 2015
 *
 */
public abstract class AbstractPurl implements Purl {

	protected final String objectId;
	protected final MediaType accept;


	public AbstractPurl(String objectId, MediaType accept)
	{
		this.objectId = objectId;
		this.accept = accept;
	}


	/**
	 * Get the &quot;local scope id&quot; of the object request by the PURL. The
	 * local scope id is part of the PURL and extracted from it by the service
	 * handler ({@link PurlResource}). It is the ID used by the naturalis source
	 * system that registered the object.
	 * 
	 * @return
	 */
	public String getObjectId()
	{
		return objectId;
	}


	public MediaType getMediaType()
	{
		return accept;
	}

}