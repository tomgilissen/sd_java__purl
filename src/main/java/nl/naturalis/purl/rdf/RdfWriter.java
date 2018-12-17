package nl.naturalis.purl.rdf;

import java.io.OutputStream;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import nl.naturalis.nba.api.Path;
import nl.naturalis.nba.api.model.Specimen;
import nl.naturalis.nba.common.PathValueReader;

public class RdfWriter {

  private static final String DC_NAMESPACE = "http://purl.org/dc/terms/";
  private static final String DWC_NAMESPACE = "http://rs.tdwg.org/dwc/terms/";

  private static final Path PATH_SCIENTIFIC_NAME = new Path("identifications.0.scientificName.fullScientificName");
  private static final Path PATH_FAMILY = new Path("identifications.0.defaultClassification.family");
  private static final Path PATH_RECORD_BASIS = new Path("kindOfUnit");
  private static final Path PATH_COLLECTOR = new Path("gatheringEvent.gatheringPersons.0.fullName");
  private static final Path PATH_COLLECTOR_FIELDNO = new Path("collectorsFieldNumber");
  private static final Path PATH_MULTIMEDIA = new Path("associatedMultiMediaUris");
  private static final Path PATH_LATITUDE = new Path("gatheringEvent.siteCoordinates.0.latitudeDecimal");
  private static final Path PATH_LONGITUDE = new Path("gatheringEvent.siteCoordinates.0.longitudeDecimal");

  private final ModelBuilder builder;
  private final ValueFactory vf;

  public RdfWriter() {
    builder = new ModelBuilder();
    vf = SimpleValueFactory.getInstance();
    builder.setNamespace("dc", DC_NAMESPACE);
    builder.setNamespace("dwc", DWC_NAMESPACE);
  }

  public void write(Specimen specimen, OutputStream out) {
    IRI iri = vf.createIRI("http://data.biodiversitydata.nl/naturalis/specimen/" + specimen.getUnitID());
    builder.subject(iri);
    addProperty("dc:title", readString(specimen, PATH_SCIENTIFIC_NAME));
    addProperty("dwc:family", readString(specimen, PATH_FAMILY));
    addProperty("dc:type", readString(specimen, PATH_RECORD_BASIS));
    addProperty("dwc:recordedBy", readString(specimen, PATH_COLLECTOR));
    addProperty("dwc:fieldNumber", readString(specimen, PATH_COLLECTOR_FIELDNO));
    addProperty("dwc:decimalLatitude", readString(specimen, PATH_LATITUDE));
    addProperty("dwc:decimalLongitude", readString(specimen, PATH_LONGITUDE));
    Object obj = read(specimen, PATH_MULTIMEDIA);
    if (obj != null) {
      builder.add("dwc:associatedMedia", specimen.getAssociatedMultiMediaUris().get(0).getAccessUri().toString());
    }
    Model model = builder.build();
    Rio.write(model, out, RDFFormat.RDFXML);
  }

  private void addProperty(String predicate, String object) {
    if (object != null) {
      builder.add(predicate, vf.createLiteral(object));
    }
  }

  private static Object read(Object specimen, Path path) {
    return new PathValueReader(path).read(specimen);
  }

  private static String readString(Object specimen, Path path) {
    PathValueReader pvr = new PathValueReader(path);
    Object val = pvr.read(specimen);
    return val == null ? null : val.toString();
  }

}
