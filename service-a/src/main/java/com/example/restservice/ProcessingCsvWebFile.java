package com.example.restservice;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
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

	public static final String COMMA_DELIMITER = ",";
	public static final String STORAGE_PATH = "./";
	public static final int MAX_LINES = 1000;
	
	public void run(String min_price, String max_price,
					String min_bed, String max_bed, 
					String min_bath, String max_bath) {
		
		String uri = "https://server-assignment.s3.amazonaws.com/listing-details.csv";
		try {
			//Download file from web and save on disk
			String downloadedFileFullName = "listing-detailsXXX.csv";
			downloadFile(uri, downloadedFileFullName);
			System.out.print("Downloaded csv File: " + downloadedFileFullName +"\n");

			Map<String,String> requestParams = new HashMap<>();
			requestParams.put("min_price", min_price);
			requestParams.put("max_price", min_price);
			requestParams.put("min_bed", min_price);
			requestParams.put("max_bed", min_price);
			requestParams.put("min_bath", min_price);
			requestParams.put("max_bath", min_price);
			
			processBigCSVFile(downloadedFileFullName, requestParams);	  
		  
		} catch (IOException e) {
		  System.out.println("Error: cannont access content - " + e.toString());
		}		
		   
	}

	
	private void downloadFile(String uri, String downloadedFileFullName) {
		
		try (BufferedInputStream in = new BufferedInputStream(new URL(uri).openStream());
				  FileOutputStream fileOutputStream = new FileOutputStream(downloadedFileFullName)) {
				    byte dataBuffer[] = new byte[1024];
				    int bytesRead;
				    while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
				        fileOutputStream.write(dataBuffer, 0, bytesRead);
				    }
				} catch (IOException e) {
					System.out.println("Error: cannont download file: " + uri + "\n" +  e.toString());
				}		
	}



	private void processBigCSVFile(String origFileFullName, Map<String,String> requestParams) throws IOException {
		
		ArrayList<String> arr =  new ArrayList<String>();
		String extension = "csv";
		String fileName = FilenameUtils.removeExtension(origFileFullName);
		try (Scanner s = new Scanner(new FileReader(String.format("%s", fileName, extension)))) {
		    int ind = 1;
		    int cnt = 0;
        	String smallFileName = fileName + "_" + ind + extension;
		    BufferedWriter writer = new BufferedWriter(new FileWriter(smallFileName));

		    while (s.hasNext()) {
		        writer.write(s.next() + System.lineSeparator());
		        if (++cnt == MAX_LINES && s.hasNext()) {
		        	writer.close();
		            writer = new BufferedWriter(new  FileWriter(smallFileName));
		            cnt = 0;			            
		            arr.add(smallFileName);
		            processSmallCSVFile(smallFileName, requestParams);
		            ind++;
		        }
		    }
		    writer.close();
		    arr.add(smallFileName);
		} catch (Exception e) {
		    e.printStackTrace();
		}
	}
	
	
	@Async
	private void processSmallCSVFile(String smallCsvFileName, Map<String,String> params) throws IOException {
		
		File file = new File(smallCsvFileName);
		LineIterator it = FileUtils.lineIterator(file, "UTF-8");
		try {
		    while (it.hasNext()) {
		        String line = it.nextLine();
		        if (lineMatching(line, params)) {
		        	//JSONObject json = csvLineToJson(line);
		        	//printJSONObject(json);
		        	System.out.println("Matching Line: " + line);
		        	
		        }
		    }
		} finally {
		    LineIterator.closeQuietly(it);
		}
	}


	private void printJSONObject(JSONObject json) {
		// TODO Auto-generated method stub
		
	}


	private JSONObject csvLineToJson(String line) {
		// TODO 
		JSONObject jsonObj = new JSONObject();
		//add 
		// TODO ...
		return jsonObj;
	
	}


	private boolean lineMatching(String line, Map<String,String> params) {
		boolean ret = true;
		String[] values = line.split(COMMA_DELIMITER);
		//id,street,status,price,bedrooms,bathrooms,sq_ft,lat,lng
		if (!params.get("min_price").isEmpty()) {
			if (Integer.parseInt(values[3]) < Integer.parseInt(params.get("min_price"))){
				return false;
			}
		}
		if (!params.get("max_price").isEmpty()) {
			if (Integer.parseInt(values[3]) > Integer.parseInt(params.get("max_price"))){
				return false;
			}
		}		
		if (!params.get("min_bed").isEmpty()) {
			if (Integer.parseInt(values[4]) < Integer.parseInt(params.get("min_bed"))){
				return false;
			}
		}
		if (!params.get("max_bed").isEmpty()) {
			if (Integer.parseInt(values[4]) > Integer.parseInt(params.get("max_bed"))){
				return false;
			}
		}
		if (!params.get("min_bath").isEmpty()) {
			if (Integer.parseInt(values[5]) < Integer.parseInt(params.get("min_bath"))){
				return false;
			}
		}		
		if (!params.get("max_bath").isEmpty()) {
			if (Integer.parseInt(values[5]) > Integer.parseInt(params.get("max_bath"))){
				return false;
			}
		}		
		return true;
	}
	

}
