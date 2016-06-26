#!/bin/sh


#!/bin/bash
# This script runs the Openllet CLI.
# Before running this script for the first time
# you may need to run:
# chmod +x openllet.sh
#
# run ./openllet.sh for the usage

if [ ! -d "cli/target/openlletcli/bin" ]; then
mvn -quiet clean install -DskipTests
fi

chmod u+x tools-cli/target/openlletcli/bin/*
tools-cli/target/openlletcli/bin/openllet $@
