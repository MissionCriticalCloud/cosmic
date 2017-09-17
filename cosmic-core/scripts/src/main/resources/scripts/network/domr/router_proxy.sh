#! /bin/bash

# used as a proxy to call script inside virtual router

#set -x

cert="/root/.ssh/id_rsa.cloud"

script=$1
shift

domRIp=$1
shift

tries=0
until ssh -p 3922 -q -o StrictHostKeyChecking=no -o BatchMode=yes -o ConnectTimeout=1 -i ${cert} root@${domRIp} "/opt/cosmic/router/bin/$script $*"
do
  exit_status=$?
  if [ "${exit_status}" -lt 255 ]; then
      echo -n "Got exit status ${exit_status}, not retrying"
      exit ${exit_status}
  fi
  sleep 1
  let "tries++"
  if [ "${tries}" -ge 5 ];
    then echo -n "Failed after ${tries} tries, exiting"
    exit ${exit_status}
  fi
done
