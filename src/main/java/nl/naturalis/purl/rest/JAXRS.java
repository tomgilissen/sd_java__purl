package nl.naturalis.purl.rest;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import nl.naturalis.purl.util.AppInfo;

/**
 * @author Ayco Holleman
 *
 */
@ApplicationPath("/")
public class JAXRS extends Application {

	public JAXRS()
	{
		super();
		AppInfo.initialize();
	}
}
