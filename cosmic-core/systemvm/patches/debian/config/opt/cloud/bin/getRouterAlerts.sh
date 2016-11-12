#!/usr/bin/env bash

# getRouterAlerts.sh  --- Send the alerts from routerServiceMonitor.log to Management Server

#set -x

filename=/var/log/routerServiceMonitor.log #Monitor service log file
if [ -n "$1" -a -n "$2" ]
then
        reqDateVal=$(date -d "$1 $2" "+%s");
else
        reqDateVal=0
fi
if [ -f $filename ]
then
        while read line
        do
            if [ -n "$line" ]
            then
                dateval=`echo $line |awk '{print $1, $2}'`
                IFS=',' read -a array <<< "$dateval"
                dateval=${array[0]}

                toDateVal=$(date -d "$dateval" "+%s")

                if [ "$toDateVal" -gt "$reqDateVal" ]
                then
                    alerts="$line\n$alerts"
                else
                    break
                fi
            fi
        done < <(tac $filename)
fi
if [ -n "$alerts" ]; then
       echo $alerts
else
       echo "No Alerts"
fi
