#!/bin/bash

java \
-Dlog4j.configuration="file:log4j.xml" \
-Dsetting.file="setting.properties" \
-jar ihtsdo-sct-drugmatch-1.0.2-dist.jar \
--match;
