package nl.naturalis.purl.rest;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import nl.naturalis.purl.util.Registry;

/**
 * @author Ayco Holleman
 *
 */
@ApplicationPath("/")
public class JAXRS extends Application {

	public JAXRS()
	{
		super();
		Registry.initialize();
	}
}
