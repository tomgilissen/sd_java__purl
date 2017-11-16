package nl.naturalis.purl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.ws.rs.core.UriBuilder;
import nl.naturalis.nba.utils.http.SimpleHttpGet;

public class PurlTest {

	public static void main(String[] args) throws UnsupportedEncodingException
	{
    System.out.println(UriBuilder.fromPath("ay  & co").toString());
    System.out.println(URLEncoder.encode("ay  & co", "UTF-8"));
    
		
	}

}
