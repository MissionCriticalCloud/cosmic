#! /bin/bash

#!/bin/bash

usage() {
  printf "Usage: %s: (-A|-D) -b <bridge/switch> -p <primary vlan> -i <secondary isolated vlan> -d <DHCP server IP> -m <DHCP server MAC> -v <VM MAC> -h \n" $(basename $0) >&2
  exit 2
}

br=
pri_vlan=
sec_iso_vlan=
dhcp_ip=
dhcp_mac=
vm_mac=
op=

while getopts 'ADb:p:i:d:m:v:h' OPTION
do
  case $OPTION in
  A)  op="add"
      ;;
  D)  op="del"
      ;;
  b)  br="$OPTARG"
      ;;
  p)  pri_vlan="$OPTARG"
      ;;
  i)  sec_iso_vlan="$OPTARG"
      ;;
  d)  dhcp_ip="$OPTARG"
      ;;
  m)  dhcp_mac="$OPTARG"
      ;;
  v)  vm_mac="$OPTARG"
      ;;
  h)  usage
      exit 1
      ;;
  esac
done

if [ -z "$op" ]
then
    echo Missing operation pararmeter!
    exit 1
fi

if [ -z "$br" ]
then
    echo Missing parameter bridge!
    exit 1
fi

if [ -z "$vm_mac" ]
then
    echo Missing parameter VM MAC!
    exit 1
fi

if [ -z "$pri_vlan" ]
then
    echo Missing parameter secondary isolate vlan!
    exit 1
fi

if [ -z "$sec_iso_vlan" ]
then
    echo Missing parameter secondary isolate vlan!
    exit 1
fi

# try to find the physical link to outside, only supports eth and em prefix now
trunk_port=`ovs-ofctl show $br | egrep "\((eth|em)[0-9]" | cut -d '(' -f 1|tr -d ' '`

if [ "$op" == "add" ]
then
    ovs-ofctl add-flow $br priority=50,dl_vlan=0xffff,dl_src=$vm_mac,actions=mod_vlan_vid:$sec_iso_vlan,resubmit:$trunk_port
    ovs-ofctl add-flow $br priority=60,dl_vlan=$sec_iso_vlan,dl_src=$vm_mac,actions=output:$trunk_port
else
    ovs-ofctl del-flows --strict $br priority=50,dl_vlan=0xffff,dl_src=$vm_mac
    ovs-ofctl del-flows --strict $br priority=60,dl_vlan=$sec_iso_vlan,dl_src=$vm_mac
fi

