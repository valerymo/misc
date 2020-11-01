package com.example.restservice;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.LineIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.*;
import org.springframework.scheduling.annotation.Async;



public class ProcessingCsvWebFile  {

	public static String STORAGE_PATH = "./";
	public static int MAX_LINES = 1000;
	
	public void run(String min_price, String max_price,
					String min_bed, String max_bed, 
					String min_bath, String max_bath) {
		
		String uri = "https://server-assignment.s3.amazonaws.com/listing-details.csv";
		try {
			//Download file from web and save on disk
			String downloadedFileFullName = makeAPICallToDownloadCsvFile(uri);
			
			//Split file if it's Big and return list of small CSV Files
			ArrayList<String> arr = splitBigCSVFile(downloadedFileFullName);
			Map<String,String> requestParams = new HashMap<>();
			requestParams.put("min_price", min_price);
			requestParams.put("max_price", min_price);
			requestParams.put("min_bed", min_price);
			requestParams.put("max_bed", min_price);
			requestParams.put("min_bath", min_price);
			requestParams.put("max_bath", min_price);
			
			for (int i = 0; i <arr.size(); i++){
				String fileName = arr.get(i);
				processSmallCSVFiles(fileName, requestParams);
			}
	  
		  System.out.print("downloadedFileFullName: " + downloadedFileFullName +"\n");
		  
		} catch (IOException e) {
		  System.out.println("Error: cannont access content - " + e.toString());
		} catch (URISyntaxException e) {
		  System.out.println("Error: Invalid URL " + e.toString());
		}		
		   
	}

	
	public static String makeAPICallToDownloadCsvFile(String uri)
		      throws URISyntaxException, IOException {
		    String response_content = "";

		    URIBuilder query = new URIBuilder(uri);

		    CloseableHttpClient client = HttpClients.createDefault();
		    HttpGet request = new HttpGet(query.build());
		    request.setHeader(HttpHeaders.ACCEPT, "application/json");
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
	
	private ArrayList<String> splitBigCSVFile(String downloadedFileFullName) throws FileNotFoundException {
		
		ArrayList<String> arr =  new ArrayList<String>();
		if (!checkIfFileIsBig(downloadedFileFullName)) {
			arr.add(downloadedFileFullName);
		}
		else {
			String extension = "csv";
			String fileName = FilenameUtils.removeExtension(downloadedFileFullName);
			try (Scanner s = new Scanner(new FileReader(String.format("%s", fileName, extension)))) {
			    int ind = 0;
			    int cnt = 0;
			    BufferedWriter writer = new BufferedWriter(new FileWriter(String.format("%s_%d.%s", fileName, ind, extension)));
			
			    while (s.hasNext()) {
			        writer.write(s.next() + System.lineSeparator());
			        if (++cnt == MAX_LINES && s.hasNext()) {
			            writer.close();
			            arr.add(fileName + "_" + ind + extension);
			            writer = new BufferedWriter(new  FileWriter(String.format("%s_%d.%s", fileName, ++ind, extension)));
			            cnt = 0;
			        }
			    }
			    writer.close();
			    arr.add(fileName + "_" + ind + extension);
			} catch (Exception e) {
			    e.printStackTrace();
			}		
		}
		return arr;
	}
	
	private boolean checkIfFileIsBig(String downloadedFileFullName) throws FileNotFoundException {
		
	   //Get Number of lines in file
		Scanner scanner = new Scanner(new FileReader(downloadedFileFullName));
		int noOfLines = 0;
		while (scanner.hasNextLine()) {
		    scanner.nextLine();
		    noOfLines++;
		}
		if (noOfLines > MAX_LINES)
		   return true;
	   
		return false;
	}


	private void processSmallCSVFiles(String smallCsvFileName, Map<String,String> params) throws IOException {
		
		File file = new File(smallCsvFileName);
		LineIterator it = FileUtils.lineIterator(file, "UTF-8");
		try {
		    while (it.hasNext()) {
		        String line = it.nextLine();
		        if (lineMatching(line, params)) {
		        	getJsonObject(line);
		        	//write Json object to DB
		        	//need DB as working with Big Data / CSV files could be Big
		        	//if small - all will be simpler, but this solution draft is for Big Data
		        }
		    }
		} finally {
		    LineIterator.closeQuietly(it);
		}
	}


	private JSONObject getJsonObject(String line) {
		// TODO 
		JSONObject jsonObj = new JSONObject();
		//add 
		// TODO ...
		return jsonObj;
	
	}


	private boolean lineMatching(String line, Map<String,String> params) {
		// TODO Auto-generated method stub
		//check if line is matching
		return false;
	}
	

}
