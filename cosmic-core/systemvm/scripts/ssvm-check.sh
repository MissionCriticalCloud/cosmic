#!/usr/bin/env bash

# Health check script for the Secondary Storage VM
# DNS server is specified.

MGMTSERVER=`cat /etc/cosmic/agent/agent.properties  | grep host= | cut -d= -f2`

# ping dns server
echo ================================================
DNSSERVER=`egrep '^nameserver' /etc/resolv.conf  | awk '{print $2}'| head -1`
echo "First DNS server is " $DNSSERVER
ping -c 2  $DNSSERVER
if [ $? -eq 0 ]
then
    echo "Good: Can ping DNS server"
else
    echo "WARNING: cannot ping DNS server"
    echo "route follows"
    route -n
fi


# check dns resolve
echo ================================================
nslookup google.com 1> /tmp/dns 2>&1
grep 'no servers could' /tmp/dns 1> /dev/null 2>&1
if [ $? -eq 0 ]
then
    echo "ERROR: DNS not resolving google.com"
    echo resolv.conf follows
    cat /etc/resolv.conf
    exit 2
else
    echo "Good: DNS resolves google.com"
fi


# check to see if we have the NFS volume mounted
echo ================================================
mount | grep nfs | grep -v /proc/fs/nfsd | grep -v sunrpc 1> /dev/null 2>&1
if [ $? -eq 0 ]
then
    echo "$storage is currently mounted"
    # check for write access
    for MOUNTPT in `mount | grep nfs | grep -v /proc/fs/nfsd | grep -v sunrpc | awk '{print $3}'`
    do
        echo Mount point is $MOUNTPT
        touch $MOUNTPT/foo
        if [ $? -eq 0 ]
        then
            echo "Good: Can write to mount point"
            rm $MOUNTPT/foo
        else
            echo "ERROR: Cannot write to mount point"
            echo "You need to export with norootsquash"
        fi
     done
fi


# check for connectivity to the management server
echo ================================================
echo Management server is $MGMTSERVER.  Checking connectivity.
socatout=$(echo | socat - TCP:$MGMTSERVER:8250,connect-timeout=3 2>&1)
if [ $? -eq 0 ]
then
    echo "Good: Can connect to management server port 8250"
else
    echo "ERROR: Cannot connect to $MGMTSERVER port 8250"
    echo $socatout
    exit 4
fi


# check for the java process running
echo ================================================
ps -eaf|grep -v grep|grep java 1> /dev/null 2>&1
if [ $? -eq 0 ]
then
    echo "Good: Java process is running"
else
    echo "ERROR: Java process not running.  Try restarting the SSVM."
    exit 3
fi

echo ================================================
echo Tests Complete.  Look for ERROR or WARNING above.

exit 0
