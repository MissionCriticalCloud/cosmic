#! /bin/bash

# used as a proxy to call script inside virtual router

#set -x

check_gw() {
  ping -c 1 -n -q $1 > /dev/null
  if [ $? -gt 0 ]
  then
    sleep 1
    ping -c 1 -n -q $1 > /dev/null
  fi
  if [ $? -gt 0 ]
  then
    exit 1
  fi
}

cert="/root/.ssh/id_rsa.cloud"

script=$1
shift

domRIp=$1
shift

check_gw "$domRIp"

ssh -p 3922 -q -o StrictHostKeyChecking=no -i $cert root@$domRIp "/opt/cloud/bin/$script $*"
exit $?
