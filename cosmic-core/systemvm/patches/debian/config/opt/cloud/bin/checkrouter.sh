#!/usr/bin/env bash


STATUS=UNKNOWN
ROUTER_TYPE=$(cat /etc/cloudstack/cmdline.json | grep type | awk '{print $2;}' | sed -e 's/[,\"]//g')
if [ "$ROUTER_TYPE" = "router" ]
then
	ROUTER_STATE=$(ip addr | grep eth2 | grep state | awk '{print $9;}' | xargs bash -c 'if [ \"$0\" == \"UP\" ]; then echo \"MASTER\"; else echo \"BACKUP\"; fi')
	STATUS=$ROUTER_STATE
else
	ROUTER_STATE=$(ip addr | grep eth1 | grep state | awk '{print $9;}')
	if [ "$ROUTER_STATE" = "UP" ]
	then
	    STATUS=MASTER
	elif [ "$ROUTER_STATE" = "DOWN" ]
	then
	    STATUS=BACKUP
	fi
fi

echo "Status: ${STATUS}"
