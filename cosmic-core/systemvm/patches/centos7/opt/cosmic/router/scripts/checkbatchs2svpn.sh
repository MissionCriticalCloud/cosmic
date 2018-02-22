#!/bin/bash

# TODO: Replace this with proper python script
for i in $1
do
    info=`/opt/cosmic/router/scripts/checks2svpn.sh ${i}-1`
    ret=$?
    batchInfo+="$i:$ret:$info&"
done

echo -n ${batchInfo}
