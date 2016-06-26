#!/bin/bash


# Usage
#   save_password -v <user VM IP> -p <password>

while getopts 'v:p:' OPTION
do
  case $OPTION in
  v)    VM_IP="$OPTARG"
        ;;
  p)    PASSWORD="$OPTARG"
        ;;
  ?)    echo "Incorrect usage"
        ;;
  esac
done
TOKEN_FILE="/tmp/passwdsrvrtoken"
TOKEN=""
if [ -f $TOKEN_FILE ]; then
    TOKEN=$(cat $TOKEN_FILE)
fi
ps aux | grep passwd_server_ip.py |grep -v grep 2>&1 > /dev/null
if [ $? -eq 0 ]
then
    ips=$(ip addr show | grep inet | awk '{print $2}')
    for ip in $ips; do
        server_ip=$(echo $ip | awk -F'/' '{print $1}')
        curl --header "DomU_Request: save_password" "http://$server_ip:8080/" -F "ip=$VM_IP" -F "password=$PASSWORD" -F "token=$TOKEN" >/dev/null 2>/dev/null &
    done
fi
