#! /bin/bash

sbt compile run
echo "Completed Running Task"
aws s3 cp log s3://log-bucket-lambda-test/ --recursive
echo "Copied Successfully"