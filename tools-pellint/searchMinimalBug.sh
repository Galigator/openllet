#!/bin/bash
mvn exec:java -Dexec.mainClass="openllet.pellint.SearchMinimalBug" -Dexec.args="$@"
