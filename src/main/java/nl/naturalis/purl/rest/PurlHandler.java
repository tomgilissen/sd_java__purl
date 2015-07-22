package nl.naturalis.purl.rest;

import javax.ws.rs.core.Response;

/**
 * Interface specifying the capabilities of PURL request handling classes.
 * 
 * @author Ayco Holleman
 * @created Jul 22, 2015
 *
 */
public interface PurlHandler {

	/**
	 * Handle the request for a PURL.
	 * 
	 * @return The HTTP response to be sent back to the client; most likely a
	 *         temporary redirect if all goes well, or a server error if
	 *         something goes wrong.
	 * 
	 */
	Response handlePurl();

}