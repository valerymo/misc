# Java Microservice - filtering csv file

This is the sample application, that taking csv file from web (https://server-assignment.s3.amazonaws.com/listing-details.csv) and filtering it, according to input parameters.  
Suggested solution is intended for processing of Big Files.

Usage examples:  
http://localhost:8080/listings?min_price=280000&min_bed=4

http://localhost:8080/listings?min_price=100000&max_price=200000&min_bed=2&max_bed=2&min_bath=2&max_bath=2  

http://localhost:8080/hello
```
{"id":1,"content":"Hello, FeatureCollection!"}
```

http://localhost:8080/actuator/health
```
{"status":"UP"}
```

## Basic flow
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

5. Test sample
Testing with following URI: 
http://localhost:8080/listings?min_price=280000&min_bed=4

Fragment of results printed:  
```
{"geometry":{"coordinates":"[-112.07703173007913,33.58674602826895]","type":"Point"},"type":"Feature","properties":{"bedrooms":"4","price":"299876","street":"383 Franklin Vis","sq_ft":"2929","id":"1572","bathrooms":"2"}}
{"geometry":{"coordinates":"[-112.24478790515825,33.496608040419865]","type":"Point"},"type":"Feature","properties":{"bedrooms":"4","price":"293263","street":"791 4th Cir","sq_ft":"2219","id":"1574","bathrooms":"1"}}
{"geometry":{"coordinates":"[-112.20643226271122,33.36599468093761]","type":"Point"},"type":"Feature","properties":{"bedrooms":"4","price":"292935","street":"18 Mission Trl","sq_ft":"2075","id":"1583","bathrooms":"2"}}
{"geometry":{"coordinates":"[-112.08364150325278,33.507521134704646]","type":"Point"},"type":"Feature","properties":{"bedrooms":"4","price":"294547","street":"350 Turk Ave","sq_ft":"2726","id":"1585","bathrooms":"2"}}
```

## Test on Kubernetes  

Using WSL/Ubuntu environmnet:
```
$ uname -a
Linux DESKTOP-1G5SDPG 4.19.104-microsoft-standard #1 SMP Wed Feb 19 06:37:35 UTC 2020 x86_64 x86_64 x86_64 GNU/Linux
```
```
$ k version
Client Version: version.Info{Major:"1", Minor:"18", GitVersion:"v1.18.8", GitCommit:"9f2892aab98fe339f3bd70e3c470144299398ace", GitTreeState:"clean", BuildDate:"2020-08-13T16:12:48Z", GoVersion:"go1.13.15", Compiler:"gc", Platform:"linux/amd64"}
Server Version: version.Info{Major:"1", Minor:"18", GitVersion:"v1.18.8", GitCommit:"9f2892aab98fe339f3bd70e3c470144299398ace", GitTreeState:"clean", BuildDate:"2020-08-13T16:04:18Z", GoVersion:"go1.13.15", Compiler:"gc", Platform:"linux/amd64"}
```
### Create Docker Image  
### docker_build file
```
#!/bin/bash
IMAGE="valerym1/csv-filter:0.1.0"
docker build . -t $IMAGE
```
To create image: 
	- compile application
	- define Image name in docker_build file
	- run docker_build

### Deploy Helm Chart
Helm Chart is part of current development
To Install MS:
```
$ helm install csv-filter helm-chart/service-a
```
See that MS installed and running:
```
$helm ls |grep csv
csv-filter      test6           1               2020-11-02 19:08:15.9407078 +0200 IST   deployed        service-a-csv-filter-0.1.0
```
```
$ k get pod |grep csv
csv-filter-service-a-csv-filter-8444cf75db-xxtmh          1/1     Running   0          2m24s
```
```
$ k exec csv-filter-service-a-csv-filter-8444cf75db-xxtmh -it sh
# curl localhost:8080/hello
{"id":1,"content":"Hello, FeatureCollection!"}#
```
```
# curl localhost:8080/listings
```
See output in pod's log:
```
$ k logs csv-filter-service-a-csv-filter-8444cf75db-xxtmh
```
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v2.3.2.RELEASE)

2020-11-02 17:53:47.409  INFO 6 --- [           main] c.e.restservice.RestServiceApplication   : Starting RestServiceApplication v0.0.1-SNAPSHOT on csv-filter-service-a-csv-filter-858689cd4c-fs7fl with PID 6 (/app.jar started by root in /)
2020-11-02 17:53:47.415  INFO 6 --- [           main] c.e.restservice.RestServiceApplication   : No active profile set, falling back to default profiles: default
20.......
vlet'
2020-11-02 17:53:57.575  INFO 6 --- [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 12 ms
Start Processing ...
Downloaded csv File: mystorage/listing-detailsXXX.csv
{"type": "FeatureCollection", "features": [#### processBigCSVFile
processSmallCSVFile: mystorage/listing-detailsXXX_1csv
{"geometry":{"coordinates":"[-112.11971469843907,33.36944420834164]","type":"Point"},"type":"Feature","properties":{"bedrooms":"4","price":"299727","street":"545 2nd Pl","sq_ft":"1608","id":"0","bathrooms":"1"}}
{"geometry":{"coordinates":"[-112.11512153436901,33.476759305937215]","type":"Point"},"type":"Feature","properties":{"bedrooms":"5","price":"123081","street":"320 Blake St","sq_ft":"3125","id":"1","bathrooms":"3"}}
{"geometry":{"coordinates":"[-112.22879647183072,33.468811357715914]","type":"Point"},"type":"Feature","properties":{"bedrooms":"5","price":"172219","street":"740 2nd Pl","sq_ft":"1208","id":"2","bathrooms":"2"}}
{"geometry":{"coordinates":"[-112.08779982484374,33.58858496101036]","type":"Point"},"type":"Feature","properties":{"bedrooms":"5","price":"277683","street":"533 8th Rd","sq_ft":"1431","id":"3","bathrooms":"2"}}
{"geometry":{"coordinates":"[-112.28351948295969,33.51415333566995]","type":"Point"},"type":"Feature","properties":{"bedrooms":"4","price":"284196","street":"557 4th Cir","sq_ft":"3173","id":"4","bathrooms":"2"}}
{"geometry":{"coordinates":"[-112.09392248468063,33.45057358654926]","type":"Point"},"type":"Feature","properties":{"bedrooms":"5","price":"115253","street":"466 8th Dv","sq_ft":"2890","id":"5","bathrooms":"2"}}
{"geometry":{"coordinates":"[-111.8783296742273,33.52960566586986]","type":"Point"},"type":"Feature","properties":{"bedrooms":"5","price":"269910","street":"195 2nd Cir","sq_ft":"2804","id":"6","bathrooms":"2"}}
```

