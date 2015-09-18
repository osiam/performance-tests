#!/bin/bash -ex

mkdir -p $CIRCLE_TEST_REPORTS/jmeter/
find . -type f -regex ".*/target/jmeter-reports/.*html" -exec cp {} $CIRCLE_TEST_REPORTS/jmeter/ \;
