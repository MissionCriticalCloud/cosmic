#!/bin/bash


# As the last command send to router before any rules operation, wait until boot up done

__TIMEOUT=240
__FLAGFILE=/var/cache/cloud/boot_up_done
done=0
for i in `seq 1 $(($__TIMEOUT * 10))`
do
    if [ -e $__FLAGFILE ]
    then
        done=1
        break
    fi
    sleep 0.1
    if [ $((i % 10)) -eq 0 ]
    then
        logger -t cloud "Waiting for VM boot up done for one second"
    fi
done

if [ -z $done ]
then
    # declare we failed booting process
    echo "Waited 60 seconds but boot up haven't been completed"
    exit
fi

echo -n `cat /etc/cloudstack-release`'&'
cat /var/cache/cloud/cloud-scripts-signature
