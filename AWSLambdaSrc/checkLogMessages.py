import configparser
import logging
import json
from datetime import datetime
from datetime import timedelta
import boto3 as boto3


## lambda function to check if logs messages exits in the given time interval
def lambda_handler(event, context):
    #Initialize the logger
    logger = logging.getLogger()
    #logger.setLevel(logging.INFO)

    # fetch the query parameters that are send from the API gateway
    iDate = event['queryStringParameters']['date']
    iTimeStamp = event['queryStringParameters']['timestamp']
    iTimeDiff = event['queryStringParameters']['interval']
    logger.info("iData %s", iDate)
    logger.info("iTimeStamp %s", iTimeStamp)
    logger.info("iTimeDiff %s", iTimeDiff)

    # Initialize the config parser to get the name of S3 bucket
    config = configparser.ConfigParser()
    config.read('config.ini')
    bucket = config['S3Conf']['BUCKET']
    # construct a key value by using the Date component
    key = "LogFileGenerator." + iDate + ".log"
    s3 = boto3.client("s3")

    ## Check for the condition when the bucket is not present
    response = s3.get_object(Bucket=bucket, Key=key)  # Enter bucket name and key(file name)
    fileData = response['Body'].read().decode('utf-8')

    # Split the data on \n's
    logLines =list(filter(None, fileData.split('\n')))

    # Read the Time stamp and get the date and the time components
    timeStamp = iTimeStamp
    dt = iTimeDiff

    # Use this time format
    timeFormat = "%H:%M:%S"
    d1 = datetime.strptime(timeStamp, timeFormat)
    diff = datetime.strptime(dt, timeFormat)
    start_time = d1 - timedelta(hours = float(diff.hour), minutes = float(diff.minute), seconds= float(diff.second))
    end_time = d1 + timedelta(hours = float(diff.hour), minutes = float(diff.minute), seconds= float(diff.second))

    logLinesStartTime = logLines[0].split()[0]
    logLinesEndTime = logLines[len(logLines) - 1].split()[0]
    logLineStartDateTime = datetime.strptime(logLinesStartTime, timeFormat)
    logLinesEndTimeDateTime = datetime.strptime(logLinesEndTime, timeFormat)

    logger.info("Start Time -> %s", str(start_time.time()))
    logger.info("End Time -> %s", str(end_time.time()))
    logger.info("Log Lines Start time -> %s", str(logLineStartDateTime.time()))
    logger.info("Log Lines End time -> %s", str(logLinesEndTimeDateTime.time()))

    if (start_time >= logLineStartDateTime and end_time < logLinesEndTimeDateTime):
        # Dict to store JSON data
        responseMsg = {}
        responseMsg['data'] = True

        # Create a response Object
        responseObj = {}
        responseObj['statusCode'] = 200
        responseObj['headers'] = {}
        responseObj['headers']['Content-Type'] = 'application/json'
        responseObj['body'] = json.dumps(responseMsg)
        return responseObj
    else:
        # Dict to store JSON data
        responseMsg = {}
        responseMsg['data'] = False

        # Create a response Object
        responseObj = {}
        responseObj['statusCode'] = 400
        responseObj['headers'] = {}
        responseObj['headers']['Content-Type'] = 'application/json'
        responseObj['body'] = json.dumps(responseMsg)
        return responseObj




