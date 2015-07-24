#!/bin/bash -ex

case $CIRCLE_NODE_INDEX in
    0)
        mvn verify -P postgres
        ;;
    1)
        mvn verify -P mysql
        ;;
esac
