#!/usr/bin/env bash
echo "Building microservice JARs through the modules"
echo ""
cd ../
mvn install

echo ""

echo "Copying fat JARs to be Dockerized"

echo ""

echo "Copying quote-generator fat JAR to project folder"
cd quote-generator
cp target/quote-generator-1.0-SNAPSHOT.jar .
echo "Ok"

echo ""

echo "Copying micro-trader-dashboard fat JAR to project folder"
cd ../micro-trader-dashboard
cp target/micro-trader-dashboard-1.0-SNAPSHOT.jar .
echo "Ok"

echo ""

echo "Copying portfolio-service fat JAR to project folder"
cd ../portfolio-service
cp target/portfolio-service-1.0-SNAPSHOT.jar .
echo "Ok"

echo ""

echo "Deploying microservices to Kubernetes through the YAMLs"

echo ""

echo "Deploying config-map"
cd ../quote-generator
kubectl create configmap app-config --from-file=src/main/resources/config.json

echo ""

echo "Deploying the microservices application"
cd ../yaml
kubectl apply -f .


echo ""

echo "Well done!"