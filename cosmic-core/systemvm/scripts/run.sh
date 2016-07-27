#!/usr/bin/env bash

readonly PROGNAME=$(basename "$0")
readonly LOCKDIR=/tmp
readonly LOCKFD=500

CLOUDSTACK_HOME="/opt/cosmic/agent"
. $CLOUDSTACK_HOME/utils.sh

LOCKFILE=$LOCKDIR/$PROGNAME.xlock
lock $LOCKFILE $LOCKFD
if [ $? -eq 1 ];then
  exit 1
fi

while true
do
  pid=$(get_pids)
  action=`cat /opt/cosmic/agent/user_request`
  if [ "$pid" == "" ] && [ "$action" == "start" ] ; then
    ./_run.sh "$@" &
    wait
    ex=$?
    if [ $ex -eq 0 ] || [ $ex -eq 1 ] || [ $ex -eq 66 ] || [ $ex -gt 128 ]; then
        # permanent errors
        sleep 5
    fi
  fi

  # user stop agent by service cloud stop
  grep 'stop' /opt/cosmic/agent/user_request &>/dev/null
  if [ $? -eq 0 ]; then
      timestamp=$(date)
      echo "$timestamp User stops cloud.com service" >> /var/log/cloud.log
      exit 0
  fi
  sleep 5
done
