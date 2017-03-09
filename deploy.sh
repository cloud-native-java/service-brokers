#!/usr/bin/env bash

# Deploys and installs the Amazon S3 broker on Cloud Foundry
# Requires a valid AWS access key with IAM and S3 privileges

root=$(cd `dirname $0` && pwd);

mvn -DskipTests=true clean install

function reset(){

    cf d -f s3-sample-app
    cf d -f s3-broker

    cf ds -f s3-service
    cf ds -f s3-broker-db

    cf purge-service-instance -f s3-service
    cf delete-service-broker -f amazon-s3
    cf delete-orphaned-routes -f
    cf purge-service-offering amazon-s3 -f
}

function deploy_service_broker_app(){
    cd ${root}/s3-service-broker
    cf create-service p-mysql 512mb s3-broker-db
    cf push --no-start
    cf set-env s3-broker AWS_ACCESS_KEY_ID ${AWS_ACCESS_KEY_ID}
    cf set-env s3-broker AWS_SECRET_ACCESS_KEY ${AWS_SECRET_ACCESS_KEY}
    cf start s3-broker
}

function configure_service_broker(){
    cf create-service-broker amazon-s3 admin admin http://s3-broker.local.pcfdev.io
    cf enable-service-access amazon-s3
}

function deploy_sample_app(){
    cd ${root}/s3-sample
    cf create-service amazon-s3 s3-basic s3-service
    cf push
}

reset
deploy_service_broker_app
configure_service_broker
deploy_sample_app