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

## Setting up AWS Lambda 

Login to the AWS Console then goto AWS Lambda by typing in `lambda` in the search bar. Once you are on the lambda page, create a lambda function with default settings and with s3 read access ( For more details on this check the Youtube video). 
Once you have created lambda function, upload the checkLogMessage.zip or getMessageLambda.zip. Once the code is uploaded on the lambda function.

## Setting up AWS Gateway 


## Setting up EC2 and log generator


## Project Deliverables

### gRPC Client and Server

### REST Call with Akka HTTP

### Lambda functions

