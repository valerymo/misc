package com.example.restservice;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.*;


public class BitcoinPrice  {
	
	private static String apiKey = "b54bcf4d-1bca-4e8e-9a24-22ff2c3d462c";

	public float run() {
		String uri = "https://api.coindesk.com/v1/bpi/currentprice.json";
		float fRate = 0;
		try {
		  String result = makeAPICall(uri);
		  JSONObject jsonObject = new JSONObject(result);
		  JSONObject usdObject = jsonObject.getJSONObject("bpi").getJSONObject("USD");
		  String rate = usdObject.get("rate").toString().replace(",", "");
		  fRate = Float.parseFloat(rate);
	  
		  //System.out.println(result);
		  System.out.print(fRate + "\tUSD   " + new Date()+"\n");
		  
		} catch (IOException e) {
		  System.out.println("Error: cannont access content - " + e.toString());
		} catch (URISyntaxException e) {
		  System.out.println("Error: Invalid URL " + e.toString());
		}		
		   
		return fRate;
	}

	
	public static String makeAPICall(String uri)
		      throws URISyntaxException, IOException {
		    String response_content = "";

		    URIBuilder query = new URIBuilder(uri);

		    CloseableHttpClient client = HttpClients.createDefault();
		    HttpGet request = new HttpGet(query.build());

		    request.setHeader(HttpHeaders.ACCEPT, "application/json");
		    request.addHeader("X-CMC_PRO_API_KEY", apiKey);

		    CloseableHttpResponse response = client.execute(request);

		    try {
		      //System.out.println(response.getStatusLine());
		      HttpEntity entity = response.getEntity();
		      response_content = EntityUtils.toString(entity);
		      EntityUtils.consume(entity);
		    } finally {
		      response.close();
		    }

		    return response_content;
		  }


}
