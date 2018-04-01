#!/usr/bin/env bash

BASE_DIR="/var/www/html/copy/template/"
PASSWDFILE="/etc/nginx/upload.htpasswd"

write_passwd() {
  local user=$1
  local passwd=$2
  htpasswd -bc $PASSWDFILE $user $passwd
  return $?
}

if [ $# -ne 2 ] ; then
	echo $"Usage: `basename $0` username password "
	exit 0
fi

write_passwd $1 $2
if [ $? -ne 0 ]
then
  echo "Failed to update password"
  exit 2
fi
exit $?
