#!/usr/bin/env bash

git clone --branch modified-version https://github.com/fermadeiral/sonar-java/
cd sonar-java/
mvn install -DskipTests
