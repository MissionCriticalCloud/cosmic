#! /bin/bash

nw_label=$1
br=`xe network-list name-label="$nw_label" params=bridge |cut -d ':' -f 2 |tr -d ' ' `
pbr=`ovs-vsctl br-to-parent $br`
while [ "$br" != "$pbr" ]
do
    br=$pbr
    pbr=`ovs-vsctl br-to-parent $br`
done
echo $pbr
