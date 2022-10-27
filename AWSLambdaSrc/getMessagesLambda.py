import configparser
import logging
import json
import re
from datetime import datetime
from datetime import timedelta
import boto3 as boto3


# Function to get the Log messages which are in the given time interval
# Return 400 level HTTP status code if the messages or the .log file doesn't exists
def lambda_handler(event, context):
    logger = logging.getLogger()

    iDate = event['queryStringParameters']['date']
    iTimeStamp = event['queryStringParameters']['timestamp']
    iTimeDiff = event['queryStringParameters']['interval']
    logger.info("iData %s", str(iDate))
    logger.info("iTimeStamp %s", str(iTimeStamp))
    logger.info("iTimeDiff %s", str(iTimeDiff))


    config = configparser.ConfigParser()
    config.read('config.ini')
    bucket = config['S3Conf']['BUCKET']
    reGex = str(config['S3Conf']['PATTERN'])
    key = "LogFileGenerator." + iDate + ".log"
    s3 = boto3.client("s3")
    response = s3.get_object(Bucket=bucket, Key=key)
    fileData = response['Body'].read().decode('utf-8')

    # Split the data on \n's
    logLines =list(filter(None, fileData.split('\n')))

    # Read the Time stamp and get the date and the time components
    timeStamp = iTimeStamp
    dt = iTimeDiff

    timeFormat = "%H:%M:%S"
    d1 = datetime.strptime(timeStamp, timeFormat)
    diff = datetime.strptime(dt, timeFormat)
    start_time = d1 - timedelta(hours = float(diff.hour), minutes = float(diff.minute), seconds= float(diff.second))
    end_time = d1 + timedelta(hours = float(diff.hour), minutes = float(diff.minute), seconds= float(diff.second))

    logLinesStartTime = logLines[0].split()[0]
    logLinesEndTime = logLines[len(logLines) - 1].split()[0]
    logLineStartDateTime = datetime.strptime(logLinesStartTime, timeFormat)
    logLinesEndTimeDateTime = datetime.strptime(logLinesEndTime, timeFormat)

    logger.info("Start Time %s", str(start_time.time()))
    logger.info("End Time %s", str(end_time.time()))
    logger.info("Log Lines Start time %s", str(logLineStartDateTime.time()))
    logger.info("Log Lines End time %s", str(logLinesEndTimeDateTime.time()))

    if (start_time < logLineStartDateTime or end_time > logLinesEndTimeDateTime):
        responseMsg = {}
        responseMsg['data'] = False
        responseObj = {}
        responseObj['statusCode'] = 400
        responseObj['headers'] = {}
        responseObj['headers']['Content-Type'] = 'application/json'
        responseObj['body'] = json.dumps(responseMsg)
        return responseObj

    # Perform Binary Search to find the upper component and lower component
    # Then get the log lines between those two time stamps
    # Collect them in an array and then filter out the messages which have the regex pattern
    start_index = binarySearchModified(logLines, start_time)
    end_index = binarySearchModified(logLines, end_time)

    regPattern = re.compile(reGex)
    matchingLogLines = list(filter(
        (lambda item: regPattern.search(item.split()[len(item.split()) - 1])),
        logLines[start_index: end_index]))
    logger.info(matchingLogLines)

    responseMsg= {}
    responseMsg['data'] = matchingLogLines

    responseObj = {}
    responseObj['statusCode'] = 200
    responseObj['headers'] = {}
    responseObj['headers']['Content-Type'] = 'application/json'
    responseObj['body'] = json.dumps(responseMsg)

    return responseObj


# Binary Search function to get the logLine correponding to the searchTime
# If the log line doesn't exits then a log record which is closer to the searchTime is returned
def binarySearchModified(arrOfLogLines, searchTime):
    timeFormat = "%H:%M:%S"
    topIndex = 0
    bottonIndex = len(arrOfLogLines)
    middle = int((topIndex + bottonIndex)/2)
    strSearchTime = str(searchTime.time())
    while(bottonIndex > topIndex):
        if (arrOfLogLines[middle].split()[0] == strSearchTime):
            return middle
        if (datetime.strptime(arrOfLogLines[middle].split()[0], timeFormat) > searchTime):
            bottonIndex = middle
            # If the middle is not going any closer to the searchTime record. Return the middle
            if (int((topIndex + bottonIndex)/2) == middle):
                return middle
            middle = int((topIndex + bottonIndex)/2)
        else:
            topIndex = middle
            # If the middle is not going any closer to the searchTime record. Return the middle
            if (int((topIndex + bottonIndex)/2) == middle):
                return middle
            middle = int((topIndex + bottonIndex)/2)
    return middle

