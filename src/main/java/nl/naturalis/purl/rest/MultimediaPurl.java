package nl.naturalis.purl.rest;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class MultimediaPurl extends AbstractPurl {

	public MultimediaPurl(String objectId, MediaType accept)
	{
		super(objectId, accept);
	}


	@Override
	public Response redirect()
	{
		return null;
	}

}
