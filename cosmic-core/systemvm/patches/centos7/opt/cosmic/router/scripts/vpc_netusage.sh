#!/usr/bin/env bash

source /opt/cosmic/router/scripts/func.sh


getEthByIp (){
  local ip=$1
  for dev in `ls -1 /sys/class/net | grep eth`
  do
    sudo ip addr show dev $dev | grep $ip\/ > /dev/null
    if [ $? -eq 0 ]
    then
      echo $dev
      return 0
    fi
  done
  return 1
}

getVPCcidr () {
  CMDLINE=$(cat /var/cache/cloud/cmdline)
  for i in $CMDLINE
  do
    # search for foo=bar pattern and cut out foo
    KEY=$(echo $i | cut -d= -f1)
    VALUE=$(echo $i | cut -d= -f2)
    if [ "$KEY" == "vpccidr" ]
    then
      echo "$VALUE"
      return 0
    fi
  done
  return 1
}

removeRulesForIp() {
  local ip=$1
  iptables-save -t mangle | grep $ip | grep "\-A"  | while read rule
  do
    rule=$(echo $rule | sed 's/\-A/\-D/')
    sudo iptables -t mangle $rule
  done
  iptables-save -t nat | grep $ip | grep "\-A"  | while read rule
  do
    rule=$(echo $rule | sed 's/\-A/\-D/')
    sudo iptables -t nat $rule
  done
  iptables-save -t filter | grep $ip | grep "\-A"  | while read rule
  do
    rule=$(echo $rule | sed 's/\-A/\-D/')
    sudo iptables -t filter $rule
  done
}


vpnoutmark="0x525"
vpninmark="0x524"
lock="biglock"
locked=$(getLockFile $lock)
if [ "$locked" != "1" ]
then
    exit 1
fi

usage() {
  printf "Usage: %s -[c|g|r|n|d] [-l <public gateway>] [-v <vpc cidr>] \n" $(basename $0)  >&2
}

create_usage_rules () {
  iptables-save|grep "NETWORK_STATS_$ethDev" > /dev/null
  if [ $? -gt 0 ]
  then
    iptables -N NETWORK_STATS_$ethDev > /dev/null;
    iptables -I FORWARD -j NETWORK_STATS_$ethDev > /dev/null;
    iptables -A NETWORK_STATS_$ethDev -o $ethDev -s $vcidr > /dev/null;
    iptables -A NETWORK_STATS_$ethDev -i $ethDev -d $vcidr > /dev/null;
  fi
  return $?
}

create_vpn_usage_rules () {
  iptables-save|grep "VPN_STATS_$ethDev" > /dev/null
  if [ $? -gt 0 ]
  then
    iptables -t mangle -N VPN_STATS_$ethDev > /dev/null;
    iptables -t mangle -I FORWARD -j VPN_STATS_$ethDev > /dev/null;
    iptables -t mangle -A VPN_STATS_$ethDev -o $ethDev -m mark --mark $vpnoutmark > /dev/null;
    iptables -t mangle -A VPN_STATS_$ethDev -i $ethDev -m mark --mark $vpninmark > /dev/null;
  fi
  return $?
}

remove_usage_rules () {
  return 0
}

get_usage () {
  iptables -L NETWORK_STATS_$ethDev -n -v -x 2> /dev/null | awk '$1 ~ /^[0-9]+$/ { printf "%s:", $2}'; > /dev/null
  return 0
}

get_vpn_usage () {
  iptables -t mangle -L VPN_STATS_$ethDev -n -v -x | awk '$1 ~ /^[0-9]+$/ { printf "%s:", $2}'; > /dev/null
  if [ $? -gt 0 ]
  then
     printf $?
     return 1
  fi
}

reset_usage () {
  iptables -Z NETWORK_STATS_$ethDev > /dev/null
  if [ $? -gt 0  -a $? -ne 2 ]
  then
     return 1
  fi
}

#set -x

cflag=
gflag=
rflag=
lflag=
vflag=
nflag=
dflag=

while getopts 'cgndrl:v:' OPTION
do
  case $OPTION in
  c)	cflag=1
	;;
  g)	gflag=1
	;;
  r)	rflag=1
	;;
  l)    lflag=1
        publicIp="$OPTARG"
        ;;
  v)    vflag=1
        vcidr="$OPTARG"
        ;;
  n)	nflag=1
	;;
  d)	dflag=1
	;;
  i)    #Do nothing, since it's parameter for host script
        ;;
  ?)	usage
        unlock_exit 2 $lock $locked
	;;
  esac
done

ethDev=$(getEthByIp $publicIp)
if [ "$cflag" == "1" ]
then
  if [ "$ethDev" != "" ]
  then
    create_usage_rules
    create_vpn_usage_rules
    unlock_exit 0 $lock $locked
   fi
fi

if [ "$gflag" == "1" ]
then
  get_usage
  unlock_exit $? $lock $locked
fi

if [ "$nflag" == "1" ]
then
  #get_vpn_usage
  unlock_exit $? $lock $locked
fi

if [ "$dflag" == "1" ]
then
  #remove_usage_rules
  unlock_exit 0 $lock $locked
fi

if [ "$rflag" == "1" ]
then
  reset_usage
  unlock_exit $? $lock $locked
fi


unlock_exit 0 $lock $locked
