package nl.naturalis.purl;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import javax.ws.rs.core.UriBuilder;

public class PurlUtil {

  /**
   * Constructs a valid URI from the specified URL template. The URL template is presumed to contain the specified placeholder for the
   * actual object ID. We currently support two placeholder strings: either "${unitID}" or "${sourceSystemId}". The placeholder is replaced
   * with the specified concrete ID, properly encoded according to whether the ID is a path segment or a query parameter. Only provide the
   * name of the placeholder ("unitID" or "sourceSystemId"), without the dollar sign and curly braces.
   * 
   * @param urlTemplate
   * @param placeholder
   * @param id
   * @return
   */
  public static URI createUrl(String urlTemplate, String placeholder, String id) {
    placeholder = new StringBuilder(placeholder.length() + 3)
        .append("${")
        .append(placeholder)
        .append('}')
        .toString();
    int x = urlTemplate.indexOf(placeholder);
    if (x == -1) {
      String fmt = "Missing placeholder \"${%s}\" in URL template \"%s\" (check configuration)";
      String msg = String.format(fmt, placeholder, urlTemplate);
      throw new PurlException(msg);
    }
    int y = urlTemplate.indexOf('?');
    String idEncoded = null;
    if (y == -1 || y > x) { // The id is a path element
      idEncoded = UriBuilder.fromPath(id).build().toString();
    } else { // The id is a query parameter
      try {
        idEncoded = URLEncoder.encode(id, "UTF-8");
      } catch (UnsupportedEncodingException e) { // Won't happen
      }
    }
    try {
      return new URI(urlTemplate.replace(placeholder, idEncoded));
    } catch (URISyntaxException e) {
      throw new PurlException(e);
    }
  }

  private PurlUtil() {}

}
