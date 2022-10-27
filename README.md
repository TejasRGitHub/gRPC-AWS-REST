# Tejas Dhananjay Rajopadhye (UIN - 675873639)

## CS441_HW2_GRPC

Tasks - 
1. gRPC Client to fetch boolean information if a log messages are present in a particular time range (interval). This tasks makes use of gRPC client and server which communicate with protobuf's message format. In order to fetch information about messages gRPC server makes an HTTP  REST request to AWS Lambda 
2. Client to call RESTful services to fetch log lines which contain designated regex pattern. This makes use Akka HTTP library to make a REST request to a REST ful service created with AWS Lambda. 

Link to youtube video - 

## Setting up project and compiling

1. Please clone / pull the repository from https://github.com/Tejas-UIC/CS441_HW2_GRPC.git
2. Once the repository is cloned, open the project in IntelliJ (This code was built and tested on IntelliJ IDEA 2022.2.1)
3. If sbt is installed on the system, type on terminal in this order sbt clean, sbt compile OR if you have sbt and scala plugins installed on IntelliJ then type clean, compile.
4. Once the compilations is done the scala classes for protobuf file will appear in the `target/scala2.13/src_managed/` folder 
5. In order to start the server, please open up a terminal window in the directory where the project is present. Then type `sbt`. This will open a fresh sbt server. Then type `run` and you will be prompted to select the program that you want to run.  

| Package.MainMethod name | Description |
|------------| ----|
| CS441HW2.gRPCClient | starts the gRPC client program |
| CS441HW2.gRPCServer | starts the gRPC server on port mentioned in the application.conf|
| RESTCallerPackage.RESTCaller | Calls the Akka HTTP which creates a RESTful request |

The output for each of the above programs is present in the `output` directory of this repo.

## Input Configurations 

All the inputs and other configurations are set in the application.conf file. Below are the configurations used to run this project

| Input Parameters | Value |
|------------------| ------|
|Date |  |
| Timestamp | |
| dT | |

Other parameters like the AWS API Gateway URL, gRPC server port number and wait and timeout seconds are also configurable by changing the application.conf.
If you happen to change the configurations please restart the gRPC server. 

This project assumes that the logs are generated in ascending order and that there is not date change when searching for logs.
The logs are generated and follow the log format specified in the `./src/main/rescources/logback.xml` file.  

## Setting up AWS Lambda 

Login to the AWS Console then goto AWS Lambda by typing in `lambda` in the search bar. Once you are on the lambda page, create a lambda function with default settings and with s3 read access ( For more details on this check the Youtube video). 
Once you have created lambda function, upload the checkLogMessage.zip or getMessageLambda.zip. 
Once the code is uploaded on the lambda function then `Deploy` the code. Edit the execution by specifying the functionName.lambda_handler
Do the same process for both the .zip file and setup two lambda functions.

## Setting up AWS Gateway 

Login to AWS Console and goto the API Gateway by searching for it. Then click on Create button for REST Service. Once you
are redirected to the APIs page type in the name of the API , select New API and click next. Once you are on the APIs page, 
click on Action button and select Create Resource and then fill in the details. Then click on the action button and create method and select GET.
Fill in the details to have execution context as Lambda and select the Lambda Proxy Integration. Click Create and then select action and deploy the API.
Goto to the GET API section and select the URL pointing corresponding to this GET service

## Setting up EC2 and log generator



## Project Deliverables

The project required to two tasks. 
1. gRPC client using protobuf which communicates AWS Lambda via a GET HTTP method to get information if log messages are present or not.
2. Client using Akka HTTP which calls AWS Lambda via GET HTTP method to retrieve log messages containing regex pattern.

### gRPC Client and Server

The message format for communicating using gRPC is mentioned in the .proto file in the `protobuf` folder. 
The message format contains 3 input params - date, timestamp, interval. Date corresponds to the date of the log file in which
log messages are to be searched against a timestamp and given interval. The interval value is used to find the time range
i.e. from  (timestamp - interval) to (timestamp + interval). 

All these parameters are string and are passed to the gRPC server. These parameters are serialized under LogFormatInput message format type. 
gRPC server is started on `port` mentioned in the `application.conf` file. When protobuf message reaches gRPC it is 
deserialized internally adhering to the LogFormatInput. Thus , input values from the protobuf are available in the gRPC context.
These input values are then passed onto the lambda function via an API gateway. This is performed by the RESTCaller Object written in RESTCaller scala file. The results returned by the server are 
200 HTTP Response with JSON data containing flag (true ) OR 400 HTTP Response with JSON data containing flag (false). 
gRPC server reads these values , converting to the LogSearchOutput format and sends it to the gRPC Client. 

### REST Call with Akka HTTP

REST Calls are performed using the Akka HTTP library. RESTCaller file contains a function `sendRequest` which creates
a REST HTTP Call with the given `url` input paramater. `sendRequest` functions takes in url, date, timeStamp, interval as inputs
Once the HTTP request is made it is captured in the Future[HTTPResponse] type. This then is unwrapped using flatMap method 
and the HTTP Status code corresponding the request is extracted. Based on this HTTP Status Code, further response is bifurcated.
If the HTTP status code is 200 then the entire JSON data is sent. If it is 400, then string containing failure of the GET request is sent.

### Lambda functions

The AWS Lambda functions are present in the `AWSLambdaSrc` folder and contain two file 
1. checkLogMessages.py - Contains lambda function which asserts whether logs messages are present in the given time range.
   This lambda function takes the inputs like date, timestamp, interval from the queryParameters passed onto by the event variable.
   The lambda function then finds the correct log file and then split the entire file by the '\n' character and stores the record into an array.
   As the logs records are assumed to be in ascending order , the start time and end time of the logs can be easily found out.
   Later, it is checked if the timestamps are between the start and the end time. If it is a 200 HTTP status response is sent with {data : true}
   If the records are not found then 400 HTTP status response code is send with {data: false}
2. getMessagesLambda.py - Contains lambda function which extracts logs messages which contain / have injected regex pattern from a time interval.
   This lambda function takes the inputs like date, timestamp, interval from the queryParameters passed onto by the event variable.
   The lambda function then finds the correct log file and then split the entire file by the '\n' character and stores the record into an array.
   As the logs records are assumed to be in ascending order , the start time and end time of the logs can be easily found out.
   Later, it is checked if the timestamps are between the start and the end time. If the timestamp is present in the log files then 
   a binary Search is initiated to find the start timestamp of time interval and end timestamp of the time interval (i.e. T - dT and T + dT). 
   Once the time intervals are found , the binary search returns the index of the timestamp records ( and returns the closest if the timestamp doesn't exist ). 
   In order to get the logs messages which have injected regex pattern, `filter` operation in python is used. After this, response is constructed containing the messages which contain the messages.


#### References

1. [Scala Pb gRPC Support](https://scalapb.github.io/docs/grpc)
2. [Single Request using Akka HTTP](https://doc.akka.io/docs/akka-http/current/client-side/request-level.html)
3. [Exposing Lambda as REST Services](https://www.youtube.com/watch?v=uFsaiEhr1zs)