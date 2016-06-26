#!/bin/bash



set +u

mgmt_nic_ip=$1
internal_server_ip=$2
gateway_ip=$3

ip route | grep "$internal_server_ip" > /dev/null

if [ $? -ne 0 ]; then
    ip route add $internal_server_ip via $gateway_ip
fi

iptables-save | grep -- "-A POSTROUTING -d $internal_server_ip" > /dev/null

if [ $? -ne 0 ]; then
    iptables -t nat -A POSTROUTING -d $internal_server_ip -j SNAT --to-source $mgmt_nic_ip
fi


iptables-save | grep -- "-A INPUT -i eth0 -p udp -m udp --dport 69 -j ACCEPT" > /dev/null
if [ $? -ne 0 ]; then
    iptables -I INPUT -i eth0 -p udp -m udp --dport 69 -j ACCEPT
fi

iptables-save | grep -- "-A FORWARD -i eth1 -o eth0 -j ACCEPT" > /dev/null
if [ $? -ne 0 ]; then
    iptables -A FORWARD -i eth1 -o eth0 -j ACCEPT
fi

rule="-A FORWARD -d $internal_server_ip/32 -i eth0 -o eth1 -j ACCEPT"
iptables-save | grep -- "$rule" > /dev/null
if [ $? -ne 0 ]; then
    iptables -I FORWARD -d $internal_server_ip/32 -i eth0 -o eth1 -j ACCEPT
fi

