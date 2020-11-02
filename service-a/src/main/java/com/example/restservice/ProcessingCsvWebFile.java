package com.example.restservice;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
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
	public static final int MAX_LINES = 1000;
	
	public static final String JSON_TMPLT = "src/main/resources/sample_template.json";
	
	public void run(String min_price, String max_price,
					String min_bed, String max_bed, 
					String min_bath, String max_bath) {
		
		String uri = "https://server-assignment.s3.amazonaws.com/listing-details.csv";
		try {
			//Download file from web and save on disk	
			String downloadedFileFullName = "mystorage/listing-detailsXXX.csv";
			downloadFile(uri, downloadedFileFullName);
			System.out.print("Downloaded csv File: " + downloadedFileFullName +"\n");
			JSONObject jsonTempl = createJsonTemplate();

			Map<String,String> requestParams = new HashMap<>();
			requestParams.put("min_price", min_price);
			requestParams.put("max_price", max_price);
			requestParams.put("min_bed", min_bed);
			requestParams.put("max_bed", max_bed);
			requestParams.put("min_bath", min_bath);
			requestParams.put("max_bath", max_bath);
			
			System.out.print("{\"type\": \"FeatureCollection\", \"features\": [");		
			processBigCSVFile(downloadedFileFullName, requestParams, jsonTempl);
			System.out.print("]}");
		  
		} catch (IOException e) {
		  System.out.println("Error: cannont access content - " + e.toString());
		}	   
	}

	
	private JSONObject createJsonTemplate() {
		
		String featureTemplate =  " {"
		+	" \"type\": \"Feature\","
		+	"\"geometry\": {\"type\": \"Point\", \"coordinates\": [-112.1,33.4]},"
		+ 	"\"properties\": {"
		+		"\"id\": \"123ABC\","
		+ 		"\"price\": 200000,"
		+		"\"street\": \"123 Walnut St\","
		+		"\"bedrooms\": 3,"
		+		"\"bathrooms\": 2,"
		+		"\"sq_ft\": 1500 }}";

		JSONObject obj = new JSONObject(featureTemplate);
		return obj;
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


	private void processBigCSVFile(String origFileFullName,
						Map<String,String> requestParams,
						JSONObject jsonTempl ) throws IOException {
		
		System.out.println("#### processBigCSVFile");
		
		String extension = "csv";
		String fileName = FilenameUtils.removeExtension(origFileFullName);
		try (Scanner s = new Scanner(new FileReader(origFileFullName))) {
		    int ind = 1;
		    int count = 1;
        	String smallFileName = fileName + "_" + ind + extension;
		    BufferedWriter writer = new BufferedWriter(new FileWriter(smallFileName));
		    s.nextLine();
		    while (s.hasNext()) {
		    	//System.out.println("#### processBigCSVFile - debug 2, count: " + count);
		        writer.write(s.nextLine() + System.lineSeparator());
		        if (count == MAX_LINES && s.hasNext()) {
		        	writer.close();
		        	processSmallCSVFile(smallFileName, requestParams, jsonTempl);
		        	smallFileName = fileName + "_" + ind + extension;
		            writer = new BufferedWriter(new  FileWriter(smallFileName));
		            count = 0;
		            ind++;
		        }
		        count++;
		        
		    }
		    writer.close();
		} catch (Exception e) {
		    e.printStackTrace();
		}
	}
	
	
	@SuppressWarnings("deprecation")
	@Async
	private void processSmallCSVFile(String smallCsvFileName,
								Map<String,String> params,
								JSONObject jsonTempl) throws IOException {
		
		System.out.println("#### processSmallCSVFile: " + smallCsvFileName);
		
		File file = new File(smallCsvFileName);
		LineIterator it = FileUtils.lineIterator(file, "UTF-8");
		try {
		    while (it.hasNext()) {
		        String line = it.nextLine();
		        if (lineMatching(line, params)) {
		        	JSONObject obj = csvLineToJsonArr(line, jsonTempl);
		        	if (obj == null) {
		        		System.out.println("Problem.... Line: " + line);
		        	}else {
		        		System.out.println(obj.toString());
		        	}
		        }
		    }
		} finally {
		    LineIterator.closeQuietly(it);
		}
	}


	private JSONObject csvLineToJsonArr(String line, JSONObject jsonTempl) {

		JSONObject obj = jsonTempl;
		String[] values = line.split(COMMA_DELIMITER);
		//System.out.println("Line: " +  line);
		if (values.length < 9) {
			return null;
		}
		
		JSONObject geometry = (JSONObject) obj.get("geometry");
		JSONObject properties = (JSONObject) obj.get("properties");
		
		String coordinates = "[" + values[8] + "," + values[7]+"]";
		geometry.put("coordinates",coordinates);
		properties.put("id",  values[0]);
		properties.put("price", values[3]);
		properties.put("street",  values[1]);
		properties.put("bedrooms",  values[4]);
		properties.put("bathrooms",  values[5]);
		properties.put("sq_ft",  values[6]);
		
		//System.out.println("csvLineToJsonArr, Json: " + obj.toString());	
		return obj;
	
	}


	private boolean lineMatching(String line, Map<String,String> params) {
		boolean ret = true;
		String[] values = line.split(COMMA_DELIMITER);
		//id,street,status,price,bedrooms,bathrooms,sq_ft,lat,lng
		if (params.get("min_price") != null) {
			if (Integer.parseInt(values[3]) < Integer.parseInt(params.get("min_price"))){
				return false;
			}
		}
		if (params.get("max_price") != null) {
			if (Integer.parseInt(values[3]) > Integer.parseInt(params.get("max_price"))){
				return false;
			}
		}		
		if (params.get("min_bed") != null) {
			if (Integer.parseInt(values[4]) < Integer.parseInt(params.get("min_bed"))){
				return false;
			}
		}
		if (params.get("max_bed") != null) {
			if (Integer.parseInt(values[4]) > Integer.parseInt(params.get("max_bed"))){
				return false;
			}
		}
		if (params.get("min_bath") != null) {
			if (Integer.parseInt(values[5]) < Integer.parseInt(params.get("min_bath"))){
				return false;
			}
		}		
		if (params.get("max_bath") != null) {
			if (Integer.parseInt(values[5]) > Integer.parseInt(params.get("max_bath"))){
				return false;
			}
		}		
		return true;
	}
	

}
