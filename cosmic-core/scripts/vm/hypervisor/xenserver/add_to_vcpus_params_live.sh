#!/bin/sh


set -x

vmname=$1
key=$2
value=$3
uuid=`xe vm-list name-label=$vmname | grep uuid | awk '{print $NF}'`
if [[ $key == "weight" ]]
then
    xe vm-param-set VCPUs-params:weight=$value uuid=$uuid
fi
if [[ $key == "cap" ]]
then
    xe vm-param-set VCPUs-params:cap=$value uuid=$uuid
fi

