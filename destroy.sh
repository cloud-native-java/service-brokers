#!/usr/bin/env bash

# Destroys the S3 broker installation in the correct sequence

# Disconnect sample application
cf unbind-service s3-sample-app s3-service
cf delete -f s3-sample-app

# Delete s3-service
cf delete-service -f s3-service
cf delete-service-broker -f amazon-s3
cf unbind-service s3-broker s3-broker-db
cf delete -f s3-broker
cf delete-service -f s3-broker-db