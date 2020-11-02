# Task - application that returns a filtered set of listings from csv web file (first working version) 

Input file:  https://server-assignment.s3.amazonaws.com/listing-details.csv   
API sample: GET /listings?min_price=100000&max_price=200000&min_bed=2&max_bed=2&min_bath=2&max_bath=2  


Suggested solution is intended for processing Big Files.
Basic flow is following:
1. CSV File downloaded from Web and saved on disk
2. Processing BIG file: splitting it to small files, and run thread to process each small file.  
	* Not waiting for split all, but working as following:
	* Read orig file, and write to new small file (where lines numvber is limited)  
		* If number of lines > MAX:
			* complete write to current file
			* Start processing current file (Thread)
				* check if line is matching. If Yes:
					* Convert csv line to Json
					* Print Json
			* Create new file and continue write to it
			.....


3. Helm for deployment - included
4. Dockerfile for image creation - included

5. Test
Testing with following URI: 
http://localhost:8080/listings?min_price=280000&min_bed=4

Fragment of results printed on stdin:
{"geometry":{"coordinates":"[-112.07703173007913,33.58674602826895]","type":"Point"},"type":"Feature","properties":{"bedrooms":"4","price":"299876","street":"383 Franklin Vis","sq_ft":"2929","id":"1572","bathrooms":"2"}}
{"geometry":{"coordinates":"[-112.24478790515825,33.496608040419865]","type":"Point"},"type":"Feature","properties":{"bedrooms":"4","price":"293263","street":"791 4th Cir","sq_ft":"2219","id":"1574","bathrooms":"1"}}
{"geometry":{"coordinates":"[-112.20643226271122,33.36599468093761]","type":"Point"},"type":"Feature","properties":{"bedrooms":"4","price":"292935","street":"18 Mission Trl","sq_ft":"2075","id":"1583","bathrooms":"2"}}
{"geometry":{"coordinates":"[-112.08364150325278,33.507521134704646]","type":"Point"},"type":"Feature","properties":{"bedrooms":"4","price":"294547","street":"350 Turk Ave","sq_ft":"2726","id":"1585","bathrooms":"2"}}

6. Test on Kubernetes: TODO
