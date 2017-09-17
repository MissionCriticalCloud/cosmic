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
  printf "Usage:\n %s -A -l <public-ip-address>\n" $(basename $0) >&2
  printf " %s -D -l <public-ip-address>\n" $(basename $0) >&2
}


add_snat() {
  logger -t cloud "$(basename $0):Added SourceNAT $pubIp on interface $ethDev"
  vpccidr=$(getVPCcidr)
  sudo iptables -D FORWARD -s $vpccidr ! -d $vpccidr -j ACCEPT
  sudo iptables -A FORWARD -s $vpccidr ! -d $vpccidr -j ACCEPT
  sudo iptables -t nat -D POSTROUTING   -j SNAT -o $ethDev --to-source $pubIp
  sudo iptables -t nat -A POSTROUTING   -j SNAT -o $ethDev --to-source $pubIp
  return $?
}
remove_snat() {
  logger -t cloud "$(basename $0):Removing SourceNAT $pubIp on interface $ethDev"
  sudo iptables -t nat -D POSTROUTING   -j SNAT -o $ethDev --to-source $pubIp
  return $?
}

#set -x
lflag=0
cflag=0
op=""

while getopts 'ADl:c:' OPTION
do
  case $OPTION in
  A)	Aflag=1
		op="-A"
		;;
  D)	Dflag=1
		op="-D"
		;;
  l)	lflag=1
		pubIp="$OPTARG"
		;;
  c)	cflag=1
		ethDev="$OPTARG"
		;;
  ?)	usage
                unlock_exit 2 $lock $locked
		;;
  esac
done

if [ "$Aflag$Dflag" != "1" ]
then
  usage
  unlock_exit 2 $lock $locked
fi

if [ "$lflag$cflag" != "11" ]
then
  usage
  unlock_exit 2 $lock $locked
fi

if [ "$Aflag" == "1" ]
then
  add_snat  $publicIp
  unlock_exit $? $lock $locked
fi

if [ "$Dflag" == "1" ]
then
  remove_sat  $publicIp
  unlock_exit $? $lock $locked
fi

unlock_exit 1 $lock $locked
