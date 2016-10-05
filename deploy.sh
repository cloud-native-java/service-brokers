#!/usr/bin/env bash

# Deploys and installs the Amazon S3 broker on Cloud Foundry
# Requires a valid AWS access key with IAM and S3 privileges
cf create-service p-mysql 1gb s3-broker-db
cd ./s3-service-broker
mvn clean install
cf push --no-start
cf set-env s3-broker AWS_ACCESS_KEY_ID $AWS_ACCESS_KEY_ID
cf set-env s3-broker AWS_SECRET_ACCESS_KEY $AWS_SECRET_ACCESS_KEY
cf start s3-broker
cf create-service-broker amazon-s3 admin admin http://s3-broker.local.pcfdev.io
cf enable-service-access amazon-s3

# Deploys the sample application and connects it to the Amazon S3 backing service
cd ../spring-boot-amazon-s3-master
mvn clean install
cd ./spring-boot-amazon-s3-sample
cf create-service amazon-s3 s3-basic s3-service
cf push