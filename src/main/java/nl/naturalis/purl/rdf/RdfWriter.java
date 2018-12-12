package nl.naturalis.purl.rdf;

import java.io.OutputStream;
import java.util.List;

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

  public static final RdfWriter INSTANCE = new RdfWriter();

  private static final String DC_NAMESPACE = "http://purl.org/dc/terms/";
  private static final String DWC_NAMESPACE = "http://rs.tdwg.org/dwc/terms/";

  private static final Path PATH_UNIT_ID = new Path("unitID");
  private static final Path PATH_SCIENTIFIC_NAME = new Path("identifications.0.scientificName.fullScientificName");
  private static final Path PATH_FAMILY = new Path("identifications.0.defaultClassification.family");
  private static final Path PATH_RECORD_BASIS = new Path("kindOfUnit");
  private static final Path PATH_COLLECTOR = new Path("gatheringEvent.gatheringPersons.0.fullName");
  private static final Path PATH_COLLECTOR_FIELDNO = new Path("collectorsFieldNumber");
  private static final Path PATH_MULTIMEDIA = new Path("associatedMultiMediaUris");
  private static final Path PATH_LATITUDE = new Path("gatheringEvent.siteCoordinates.0.latitudeDecimal");
  private static final Path PATH_LONGITUDE = new Path("gatheringEvent.siteCoordinates.0.longitudeDecimal");

  private ModelBuilder builder;
  private ValueFactory vf;

  private RdfWriter() {
    builder = new ModelBuilder();
    vf = SimpleValueFactory.getInstance();
    builder.setNamespace("dc", DC_NAMESPACE);
    builder.setNamespace("dwc", DWC_NAMESPACE);
  }

  @SuppressWarnings("unchecked")
  public void write(Specimen specimen, OutputStream out) {
    String unitID = (String) read(specimen,PATH_UNIT_ID);
    IRI iri = vf.createIRI("http://data.biodiversitydata.nl/naturalis/specimen/" + unitID);
    String val = readString(specimen,PATH_SCIENTIFIC_NAME);
    builder.subject(iri).add("dc:title", vf.createLiteral(val));
    if ((val = readString(specimen,PATH_FAMILY)) != null) {
      builder.add("dwc:family", val);
    }
    if ((val = readString(specimen,PATH_RECORD_BASIS)) != null) {
      builder.add("dc:type", val);
    }
    if ((val = readString(specimen,PATH_COLLECTOR)) != null) {
      builder.add("dwc:recordedBy", val);
    }
    if ((val = readString(specimen,PATH_COLLECTOR_FIELDNO)) != null) {
      builder.add("dwc:fieldNumber", val);
    }
    Object obj = read(specimen,PATH_MULTIMEDIA);
    if (obj != null) {
      builder.add("dwc:associatedMedia", ((List<Object>) obj).get(0).toString());
    }
    if ((val = readString(specimen,PATH_LATITUDE)) != null) {
      builder.add("dwc:decimalLatitude", val);
    }
    if ((val = readString(specimen,PATH_LONGITUDE)) != null) {
      builder.add("dwc:decimalLongitude", val);
    }
    Model model = builder.build();
    Rio.write(model, out, RDFFormat.RDFXML);
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
