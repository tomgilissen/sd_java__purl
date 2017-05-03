package nl.naturalis.purl;

import nl.naturalis.nba.utils.http.SimpleHttpGet;

public class PurlTest {

	public static void main(String[] args)
	{
		SimpleHttpGet request = new SimpleHttpGet();
		request.setBaseUrl("http://localhost:8080/purl");
		request.setPath("naturalis/specimen/ZMA.MAM.1419");
		request.setAccept("application/json");
		request.execute();
		System.out.println(new String(request.getResponseBody()));
	}

}
