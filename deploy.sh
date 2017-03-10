#!/usr/bin/env bash

# Deploys and installs the Amazon S3 broker on Cloud Foundry
# Requires a valid AWS access key with IAM and S3 privileges

root=$(cd `dirname $0` && pwd);

service_broker=s3-service-broker
mvn -DskipTests=true -f s3-sample/pom.xml clean install
mvn -DskipTests=true -f s3-service-broker/pom.xml clean install

function reset(){
    cf purge-service-instance -f s3-service
    cf d -f s3-sample-app
    cf d -f s3-service-broker

    cf ds -f s3-service
    cf ds -f s3-service-broker-db

    cf purge-service-instance -f ${service_broker}
    cf delete-service-broker -f amazon-s3
    cf delete-orphaned-routes -f
    cf purge-service-offering amazon-s3 -f
}

function deploy_service_broker_app(){
    cd ${root}/s3-service-broker
    cf create-service p-mysql 512mb ${service_broker}-db
    cf push --no-start
    cf set-env $service_broker AWS_ACCESS_KEY_ID ${AWS_ACCESS_KEY_ID}
    cf set-env $service_broker AWS_SECRET_ACCESS_KEY ${AWS_SECRET_ACCESS_KEY}
    cf start $service_broker
}

function configure_service_broker(){
    cf create-service-broker amazon-s3 admin admin http://${service_broker}.local.pcfdev.io -v
    cf enable-service-access $service_broker -p basic
}

function deploy_sample_app(){
    cd ${root}/s3-sample
    cf create-service $service_broker basic s3-service
    cf push
}

function update_broker(){
  cd ${root}/${service_broker}
  cf push
}

reset
deploy_service_broker_app
configure_service_broker
# update_broker
deploy_sample_app
