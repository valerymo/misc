package com.example.restservice;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@RestController
public class ServiceController {
	
	private static final String template = "Hello, %s!";
	private final AtomicLong counter = new AtomicLong();

	@GetMapping("/hello")
	public Hello hello(@RequestParam(value = "name", defaultValue = "FeatureCollection") String name) {
		return new Hello(counter.incrementAndGet(), String.format(template, name));
	}
	
	@GetMapping("/listings")
	///listings?min_price=100000&max_price=200000&min_bed=2&max_bed=2&min_bath=2&ma
	public void startProcessing(	@RequestParam(required = false) String min_price,
									@RequestParam(required = false) String max_price,
									@RequestParam(required = false) String min_bed,
									@RequestParam(required = false) String max_bed,
									@RequestParam(required = false) String min_bath,
									@RequestParam(required = false) String max_bath) {
			System.out.println("Start Processing ...");	
			ProcessingCsvWebFile csvProcessor = new ProcessingCsvWebFile();
			try {
				csvProcessor.run(min_price,max_price,min_bed, max_bed, min_bath, max_bath);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	}
	
}
