package nl.naturalis.purl.rdf;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import nl.naturalis.nba.api.model.Specimen;

import static nl.naturalis.purl.ContentNegotiationUtil.MEDIATYPE_RDF_JSONLD;
import static nl.naturalis.purl.ContentNegotiationUtil.MEDIATYPE_RDF_TURTLE;
import static nl.naturalis.purl.ContentNegotiationUtil.MEDIATYPE_RDF_XML;
import static nl.naturalis.purl.rest.ResourceUtil.plainTextResponse;

public class RdfResponseProvider {

  private final Specimen specimen;
  private final MediaType mediaType;

  public RdfResponseProvider(Specimen specimen, MediaType mediaType) {
    this.specimen = specimen;
    this.mediaType = mediaType;
  }

  public Response createRdfResponse(boolean debug) {
    if (mediaType.isCompatible(MEDIATYPE_RDF_XML)) {
      return createRdfXmlResponse(debug);
    }
    if (mediaType.isCompatible(MEDIATYPE_RDF_TURTLE)) {
      return createTurtleResponse();
    }
    if (mediaType.isCompatible(MEDIATYPE_RDF_JSONLD)) {
      return createJsonLdResponse();
    }
    throw new AssertionError("Unexpected RDF format");
  }

  private Response createRdfXmlResponse(boolean debug) {
    if (debug) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
      new RdfWriter().writeRdfXml(specimen, baos);
      try {
        return plainTextResponse(baos.toString("UTF-8"));
      } catch (UnsupportedEncodingException e) { // Won't happen
        throw new AssertionError();
      }
    }
    StreamingOutput stream = (output) -> {
      new RdfWriter().writeRdfXml(specimen, output);
    };
    return Response.ok(stream).type(MEDIATYPE_RDF_XML).build();
  }

  private Response createTurtleResponse() {
    StreamingOutput stream = (output) -> {
      new RdfWriter().writeTurtle(specimen, output);
    };
    return Response.ok(stream).type(MEDIATYPE_RDF_TURTLE).build();
  }

  private Response createJsonLdResponse() {
    StreamingOutput stream = (output) -> {
      new RdfWriter().writeJsonLd(specimen, output);
    };
    return Response.ok(stream).type(MEDIATYPE_RDF_JSONLD).build();
  }

}
