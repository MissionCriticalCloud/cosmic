#!/usr/bin/env bash

STATUS=UNKNOWN
ROUTER_TYPE=$(cat /etc/cosmic/router/cmdline.json | grep type | awk '{print $2;}' | sed -e 's/[,\"]//g')
if [ "$ROUTER_TYPE" = "router" ]
then
    ROUTER_STATE=$(ip addr | grep eth2 | grep state | awk '{print $9;}')
else
    ROUTER_STATE=$(ip addr | grep eth1 | grep state | awk '{print $9;}')
fi

if [ "$ROUTER_STATE" = "UP" ]
then
    STATUS=MASTER
elif [ "$ROUTER_STATE" = "DOWN" ]
then
    STATUS=BACKUP
fi

echo "Status: ${STATUS}"
