#!/bin/bash
#

#
for i in `xm list | awk '{ print $1 }' | egrep -v "Name|Domain-0"`
do
    xm destroy $i
done
rm /etc/ovs-agent/db/server
rm /etc/ovs-agent/db/repository
rm /etc/ocfs2/cluster.conf
rm /nfsmnt/*/*.img
rm /nfsmnt/*/.ovspoolfs
rm /nfsmnt/*/.generic_fs_stamp
rm /OVS/Repositories/*/.generic_fs_stamp
rm /OVS/Repositories/*/.ovsrepo
/etc/init.d/ovs-agent restart
/etc/init.d/ocfs2 restart
for i in `mount | grep cs-mgmt | awk '{ print $1 }'`
do
    umount $i
done
rm -rf /OVS/Repositories/*
rm -rf /nfsmnt/*
ip addr del 192.168.1.230 dev c0a80100
ip addr del 192.168.1.161 dev c0a80100
rm /etc/sysconfig/network-scripts/ifcfg-control0
reboot
