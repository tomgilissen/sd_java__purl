package nl.naturalis.purl.rest;

import javax.ws.rs.core.Response;

public interface Purl {

	/**
	 * Handle the request for a PURL. Note that although this usually means
	 * resolving and redirecting to another URL, this is not required by the
	 * specs (see https://purl.org/docs/faq.html#toc7.4). Nevertheless we still
	 * use {@code redirect} as the name for the method that handles the PURL.
	 * 
	 * @return The HTTP response, usually but not necessarily with an HTTP 302
	 *         status (if all goes well).
	 * 
	 */
	Response handle();

}