#!/usr/bin/env bash


# @VERSION@

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
