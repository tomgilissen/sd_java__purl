package nl.naturalis.purl.rest;

import javax.ws.rs.core.Response;

public interface PurlHandler {

	/**
	 * Handle the request for a PURL.
	 * 
	 * @return The HTTP response (usually a temporary redirect)
	 * 
	 */
	Response handlePurl();

}