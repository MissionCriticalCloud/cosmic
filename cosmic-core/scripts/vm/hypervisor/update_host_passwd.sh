#! /bin/bash

username=$1
new_passwd=$2
expected="successfully."
result=`echo -e "$new_passwd\n$new_passwd" | passwd --stdin $username | grep successfully | awk '{ print $6 }'`

if [ $result = $expected ]; then
   exit 0
else
   exit 1
fi
