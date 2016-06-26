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
/etc/init.d/ovs-agent restart
/etc/init.d/ocfs2 restart
for i in `mount | grep cs-mgmt | awk '{ print $1 }'`
do
    umount $i
done
