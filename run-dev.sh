#!/bin/bash

# execute DrugMatch during development

java \
-Dlog4j.configuration="file:src/main/resource/log4j.xml" \
-Dsetting.file="src/main/resource/setting-dev.properties" \
-jar target/ihtsdo-sct-drugmatch-1.0-MILESTONE_2-ALPHA-RELEASE_1-dist.jar;
