#!/bin/bash

# execute DrugMatch during development

java \
-Dlog4j.configuration="file:src/main/resource/log4j.xml" \
-Dsetting.file="src/main/resource/setting-dev.properties" \
-jar target/ihtsdo-sct-drugmatch-2.0.0-dist.jar \
--matchAttributeReport;
