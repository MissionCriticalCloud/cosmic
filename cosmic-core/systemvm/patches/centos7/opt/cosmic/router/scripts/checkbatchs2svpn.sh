#!/bin/bash


for i in $*
do
    info=`/opt/cosmic/router/scripts/checks2svpn.sh $i`
    ret=$?
    batchInfo+="$i:$ret:$info&"
done

echo -n ${batchInfo}
