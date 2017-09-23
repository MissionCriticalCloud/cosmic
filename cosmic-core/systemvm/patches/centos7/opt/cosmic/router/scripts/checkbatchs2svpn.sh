#!/bin/bash


for i in $*
do
    info=`/opt/cloud/bin/checks2svpn.sh $i`
    ret=$?
    batchInfo+="$i:$ret:$info&"
done

echo -n ${batchInfo}
