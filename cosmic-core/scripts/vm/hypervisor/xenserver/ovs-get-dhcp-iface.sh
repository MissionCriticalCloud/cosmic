#! /bin/bash

#!/bin/bash

bridge=$1
dhcp_name=$2
dom_id=`xe vm-list is-control-domain=false power-state=running params=dom-id name-label=$dhcp_name|cut -d ':' -f 2 |tr -d ' ' `
iface="vif${dom_id}.0"
echo $iface
