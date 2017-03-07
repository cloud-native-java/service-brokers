#!/bin/bash

function app_domain(){
    D=`cf apps | grep $1 | tr -s ' ' | cut -d' ' -f 6 | cut -d, -f1`
    echo $D
}

rd=$( cd `dirname $0` && pwd )
app=s3-broker
service_brokers=$broker
db=s3-broker-db
broker=amazon-s3
plan=s3-basic


cf d -f $app
cf delete-orphaned-routes -f

cf s | grep $db || cf cs $db p-mysql 100mb
cd $rd/s3-service-broker

path_to_jar=$rd/s3-service-broker/target/s3-service-broker.jar

[ -e $path_to_jar ] && echo "jar ${path_to_jar} already exists. Not rebuilding." || mvn -DskipTests=true clean install

cf a | grep $app || cf push -f $rd/s3-service-broker/manifest.yml

app_url=http://`app_domain $app`
echo $app_url

cf create-service-broker $broker admin admin $app_url

cf enable-service-access $service_brokers
cf create-service $service_brokers $plan my-s3-sample-service
