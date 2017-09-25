#!/bin/bash

if [ -z $1 ]
then
    echo "Fail to find VPN peer address!"
    exit 1
fi

if [ -f /etc/cosmic-release ]
  then
    VERSION_DATA=$(cat /etc/cosmic-release | cut -d" " -f3)
    export IFS="."
    for part in ${VERSION_DATA}; do
        SYSTEMVM_VERSION=${SYSTEMVM_VERSION}$(printf "%02d" ${part})
    done
    export IFS=";"
fi

# Strongswan check
if (( "${SYSTEMVM_VERSION}" > "170312" )); then
    ipsec status  vpn-$1 > /tmp/vpn-$1.status

    cat /tmp/vpn-$1.status | grep "INSTALLED" > /dev/null
    ipsecok=$?
    if [ $ipsecok -ne 0 ]
    then
        echo -n "IPsec SA not found;"
        echo "Site-to-site VPN have not connected"
        exit 11
    fi
    echo -n "IPsec SA found;"
    echo "Site-to-site VPN have connected"
    exit 0
fi

# Openswan check
ipsec auto --status | grep vpn-$1 > /tmp/vpn-$1.status

cat /tmp/vpn-$1.status | grep "ISAKMP SA established" > /dev/null
isakmpok=$?
if [ $isakmpok -ne 0 ]
then
    echo -n "ISAKMP SA NOT found but checking IPsec;"
else
    echo -n "ISAKMP SA found;"
fi

cat /tmp/vpn-$1.status | grep "IPsec SA established" > /dev/null
ipsecok=$?
if [ $ipsecok -ne 0 ]
then
    echo -n "IPsec SA not found;"
    echo "Site-to-site VPN have not connected"
    exit 11
fi
echo -n "IPsec SA found;"
echo "Site-to-site VPN have connected"
exit 0
