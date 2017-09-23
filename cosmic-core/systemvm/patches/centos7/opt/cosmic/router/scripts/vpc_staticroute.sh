#!/usr/bin/env bash

# @VERSION@

source /root/func.sh
source /opt/cloud/bin/vpc_func.sh
lock="biglock"
locked=$(getLockFile $lock)
if [ "$locked" != "1" ]
then
    exit 1
fi

usage() {
  printf "Usage: %s:  -a < routes > \n" $(basename $0) >&2
}

#set -x

flush_table_backup() {
  flush_table "static_route_back"
}

flush_table() {
  local tab=$1
  sudo ip route flush table $tab
}

copy_table() {
  local from=$1
  local to=$2
  sudo ip route show table $from | while read route
  do
    sudo ip route add table $to $route
  done
}

backup_table() {
  flush_table "static_route_back"
  copy_table "static_route" "static_route_back"
  flush_table "static_route"
}

restore_table() {
  flush_table "static_route"
  copy_table "static_route_back" "static_route"
  flush_table "static_route_back"
}

static_route() {
  local rule=$1
  local ip=$(echo $rule | cut -d: -f1)
  if [ $ip == "Revoke" ]
  then
    return 0
  fi
  local gateway=$(echo $rule | cut -d: -f2)
  local cidr=$(echo $rule | cut -d: -f3)
  logger -t cloud "$(basename $0): static route: public ip=$ip \
  	gateway=$gateway cidr=$cidr"
  local dev=$(getEthByIp $ip)
  if [ $? -gt 0 ]
  then
    return 1
  fi
  sudo ip route add $cidr dev $dev via $gateway table static_route &>/dev/null
  result=$?
  logger -t cloud "$(basename $0): done static route: public ip=$ip \
  	gateway=$gateway cidr=$cidr"
  return $result
}

gflag=
aflag=
while getopts 'a:' OPTION

do
  case $OPTION in
  a)    aflag=1
        rules="$OPTARG"
        ;;
  ?)    usage
        unlock_exit 2 $lock $locked
        ;;
  esac
done

if [ -n "$rules" ]
then
  rules_list=$(echo $rules | cut -d, -f1- --output-delimiter=" ")
fi

success=0

backup_table

for r in $rules_list
do
  static_route $r
  success=$?
  if [ $success -gt 0 ]
  then
    logger -t cloud "$(basename $0): failure to apply fw rules for guest network: $gcidr"
    break
  else
    logger -t cloud "$(basename $0): successful in applying fw rules for guest network: $gcidr"
  fi
done

if [ $success -gt 0 ]
then
  logger -t cloud "$(basename $0): restoring from backup for guest network: $gcidr"
  restore_table
else
  logger -t cloud "$(basename $0): deleting backup for guest network: $gcidr"
  flush_table_backup
fi
unlock_exit $success $lock $locked

