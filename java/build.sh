#!/bin/sh
mvn clean install deploy

cd hiro-client
mvn -f pom-open.xml install deploy -DskipTests
cd ..
