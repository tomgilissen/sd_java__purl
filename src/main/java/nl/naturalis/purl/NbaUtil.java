package nl.naturalis.purl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.naturalis.nba.api.InvalidQueryException;
import nl.naturalis.nba.api.QueryCondition;
import nl.naturalis.nba.api.QueryResult;
import nl.naturalis.nba.api.QueryResultItem;
import nl.naturalis.nba.api.QuerySpec;
import nl.naturalis.nba.api.model.MultiMediaObject;
import nl.naturalis.nba.api.model.Specimen;
import nl.naturalis.nba.client.MultiMediaObjectClient;
import nl.naturalis.nba.client.SpecimenClient;

/**
 * Utility class for interacting with the NBA.
 */
public class NbaUtil {

  private static final Logger logger = LogManager.getLogger(NbaUtil.class);

  private NbaUtil() {}

  /**
   * Retrieves the specimen with the specified unitID.
   * 
   * @param unitID
   * @return
   * @throws PurlException
   */
  public static Specimen getSpecimen(String unitID) {
    logger.info("Retrieving specimen with UnitID " + unitID);
    SpecimenClient client = Registry.getInstance().getSpecimenClient();
    Specimen[] specimens = client.findByUnitID(unitID);
    if (specimens.length == 0) {
      return null;
    }
    if (specimens.length > 1) {
      throw new PurlException("Duplicate unitID: " + unitID);
    }
    return specimens[0];
  }

  /**
   * Get multimedia for specified specimen.
   */
  public static MultiMediaObject[] getMultiMedia(Specimen specimen) {
    logger.info("Retrieving multimedia for specimen with UnitID " + specimen.getUnitID());
    MultiMediaObjectClient client = Registry.getInstance().getMultiMediaClient();
    String field = "associatedSpecimenReference";
    String value = specimen.getId();
    QueryCondition condition = new QueryCondition(field, "=", value);
    QuerySpec query = new QuerySpec();
    query.setConstantScore(true);
    query.addCondition(condition);
    QueryResult<MultiMediaObject> result;
    try {
      result = client.query(query);
    } catch (InvalidQueryException e) {
      throw new PurlException(e);
    }
    MultiMediaObject[] multimedia = new MultiMediaObject[result.size()];
    int i = 0;
    for (QueryResultItem<MultiMediaObject> qri : result) {
      multimedia[i++] = qri.getItem();
    }
    logger.info("Number of multimedia found: " + multimedia.length);
    return multimedia;
  }

}
