#!/bin/sh

CONNTRACKD_BIN=/usr/sbin/conntrackd
CONNTRACKD_LOCK=/var/lock/conntrack.lock
CONNTRACKD_CONFIG=/etc/conntrackd/conntrackd.conf
KEEPALIVED_STATE=/tmp/keepalived_state

case "$1" in
  primary)
    #
    # commit the external cache into the kernel table
    #
    $CONNTRACKD_BIN -C $CONNTRACKD_CONFIG -c
    if [ $? -eq 1 ]
    then
        logger "ERROR: failed to invoke conntrackd -c"
    fi

    #
    # flush the internal and the external caches
    #
    $CONNTRACKD_BIN -C $CONNTRACKD_CONFIG -f
    if [ $? -eq 1 ]
    then
        logger "ERROR: failed to invoke conntrackd -f"
    fi

    #
    # resynchronize my internal cache to the kernel table
    #
    $CONNTRACKD_BIN -C $CONNTRACKD_CONFIG -R
    if [ $? -eq 1 ]
    then
        logger "ERROR: failed to invoke conntrackd -R"
    fi

    #
    # send a bulk update to backups
    #
    $CONNTRACKD_BIN -C $CONNTRACKD_CONFIG -B
    if [ $? -eq 1 ]
    then
        logger "ERROR: failed to invoke conntrackd -B"
    fi

    #
    # save file to mention we're master
    #
    /usr/bin/echo MASTER > $KEEPALIVED_STATE
    if [ $? -eq 1 ]
    then
        logger "ERROR: failed to write to ${KEEPALIVED_STATE}"
    fi
    ;;
  backup)
    #
    # is conntrackd running? request some statistics to check it
    #
    $CONNTRACKD_BIN -C $CONNTRACKD_CONFIG -s
    if [ $? -eq 1 ]
    then
        #
	# something's wrong, do we have a lock file?
	#
    if [ -f $CONNTRACKD_LOCK ]
	then
	    logger "WARNING: conntrackd was not cleanly stopped."
	    logger "If you suspect that it has crashed:"
	    logger "1) Enable coredumps"
	    logger "2) Try to reproduce the problem"
	    logger "3) Post the coredump to netfilter-devel@vger.kernel.org"
	    rm -f $CONNTRACKD_LOCK
	fi
	$CONNTRACKD_BIN -C $CONNTRACKD_CONFIG -d
	if [ $? -eq 1 ]
	then
	    logger "ERROR: cannot launch conntrackd"
	    exit 1
	fi
    fi
    #
    # shorten kernel conntrack timers to remove the zombie entries.
    #
    $CONNTRACKD_BIN -C $CONNTRACKD_CONFIG -t
    if [ $? -eq 1 ]
    then
        logger "ERROR: failed to invoke conntrackd -t"
    fi

    #
    # request resynchronization with master firewall replica (if any)
    # Note: this does nothing in the alarm approach.
    #
    $CONNTRACKD_BIN -C $CONNTRACKD_CONFIG -n
    if [ $? -eq 1 ]
    then
        logger "ERROR: failed to invoke conntrackd -n"
    fi

    #
    # save file to mention we're backup
    #
    /usr/bin/echo BACKUP > $KEEPALIVED_STATE
    if [ $? -eq 1 ]
    then
        logger "ERROR: failed to write to ${KEEPALIVED_STATE}"
    fi
    ;;
  fault)
    #
    # shorten kernel conntrack timers to remove the zombie entries.
    #
    $CONNTRACKD_BIN -C $CONNTRACKD_CONFIG -t
    if [ $? -eq 1 ]
    then
        logger "ERROR: failed to invoke conntrackd -t"
    fi
    #
    # save file to mention we're in fault/unknown state
    #
    /usr/bin/echo UNKNOWN > $KEEPALIVED_STATE
    if [ $? -eq 1 ]
    then
        logger "ERROR: failed to write to ${KEEPALIVED_STATE}"
    fi
    ;;
  *)
    logger "ERROR: unknown state transition"
    echo "Usage: primary-backup.sh {primary|backup|fault}"
    exit 1
    ;;
esac

exit 0
