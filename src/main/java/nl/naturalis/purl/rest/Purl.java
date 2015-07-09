package nl.naturalis.purl.rest;

import javax.ws.rs.core.Response;

public interface Purl {

	Response redirect() throws Exception;

}