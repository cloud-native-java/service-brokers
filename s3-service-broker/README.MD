# Cloud Foundry S3 Service Broker

This project is a Cloud Foundry S3 service broker using Spring Boot.

## Usage

Register the service broker:

> cf create-service-broker amazon-s3 admin admin http://s3-broker.local.micropcf.io

Enable service access:

> cf enable-service-access amazon-s3

Create service instance:

> cf create-service amazon-s3 s3-basic s3-sample-service

