#!/bin/bash -ex

case $CIRCLE_NODE_INDEX in
    0)
        mvn -q verify -P postgres
        ;;
    1)
        mvn -q verify -P mysql
        ;;
esac
