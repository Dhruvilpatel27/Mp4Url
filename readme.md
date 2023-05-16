## SpringBoot Application
Spring Boot-based application with controller for parsing mp4 files and Integrated with Project reactor to introduce non-blocking and reactive capabilities to the application for feasibility

Required: JDK, Any IDE,  Spring Dependencies -1)Spring Web 2) Spring Boot Dev Tools

### Program Logic :
1. The client makes an HTTP GET request to the /analyze endpoint with a URL parameter specifying the MP4 file URL and then analyzeMP4 method is invoked.
2. URL parameter is validated and A unique file name is generated then MP4 file is downloaded
3. ObjectMapper and InputStream is used in analyzeMP4File and A new object is created to represent the box and the type and size properties are set and Depending on the box type, it either recursively reads sub-boxes or skips over the payload(On the basis of assumption below).
4. The tag and use of Mono<ResponseEntity<String>>, indicates a reactive stream which will emit a ResponseEntity<String> when the analysis is completed , Also downloadFile() returns a Mono<String> that represents the asynchronous file downloading operation. Reactive operators like flatMap(), defaultIfEmpty(), and onErrorResume() are used to compose and transform reactive streams.
5. The analyzed MP4 file structure is returned as a JSON response in the ResponseEntity.ok method.


## How to Run
1) Use POSTMAN, With GET and specify the URL by
--> http://localhost:8080/analyze?url=https://demo.castlabs.com/tmp/text0.mp4
    
    
or any other URL to be diagnosed some open source sample for mp4
    
https://storage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4
https://storage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4


![alt text](https://imgtr.ee/images/2023/05/15/v1rCi.png)


2) Simply running the application also prints the output in the terminal(but not on a payload form)

3) make a sample request with curl using this URL for the mp4 file
curl -X GET "http://localhost:8080/analyze?url=http://demo.castlabs.com/tmp/text0.mp4"


### Assumptions and more:
for the exercise this box types are considered and there size and type are returned after parsing
MOOF, MFHD, TRAF, TFHD,TRUN,UUID,UUID,MDAT.

● The box types MOOF and TRAF only contain other boxes, All other boxes contain payload and do not contain other boxes.

    
    
    
Maven Dependecy used: Project Reactor
    
    ```
    <dependency>
           <groupId>io.projectreactor</groupId>
           <artifactId>reactor-core</artifactId>
           <version>3.4.10</version>
       </dependency>
       ```
