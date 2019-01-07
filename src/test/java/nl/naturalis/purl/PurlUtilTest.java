package nl.naturalis.purl;

import java.net.URI;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PurlUtilTest {

  @Test
  public void createUrl1() {
    String tmpl = "http://bioportal.naturalis.nl/specimen/${unitID}";
    URI uri = PurlUtil.createUrl(tmpl, "unitID", "L   085.750");
    // System.out.println("createUrl1(): " + uri.toString());
    assertEquals("http://bioportal.naturalis.nl/specimen/L%20%20%20085.750", uri.toString());
  }

  @Test
  public void createUrl2() {
    String tmpl = "http://bioportal.naturalis.nl/specimen/?unitID=${unitID}";
    URI uri = PurlUtil.createUrl(tmpl, "unitID", "L   085.750");
    // System.out.println("createUrl2(): " + uri.toString());
    assertEquals("http://bioportal.naturalis.nl/specimen/?unitID=L+++085.750", uri.toString());
  }

  @Test
  public void createUrl3() {
    String tmpl = "http://bioportal.naturalis.nl/specimen/${unitID}/";
    URI uri = PurlUtil.createUrl(tmpl, "unitID", "L$%@&?085.750");
    // System.out.println("createUrl3(): " + uri.toString());
    assertEquals("http://bioportal.naturalis.nl/specimen/L$%25@&%3F085.750/", uri.toString());
  }

  @Test
  public void createUrl4() {
    String tmpl = "http://bioportal.naturalis.nl/specimen/?unitID=${unitID}";
    URI uri = PurlUtil.createUrl(tmpl, "unitID", "L$%@&?085.750");
    // System.out.println("createUrl4(): " + uri.toString());
    assertEquals("http://bioportal.naturalis.nl/specimen/?unitID=L%24%25%40%26%3F085.750", uri.toString());
  }
}
