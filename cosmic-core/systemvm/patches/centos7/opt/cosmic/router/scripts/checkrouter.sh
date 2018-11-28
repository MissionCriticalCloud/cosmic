#!/usr/bin/env bash

STATUS=UNKNOWN
if [ -f /opt/cosmic/router/keepalived_state ]; then
    STATUS=$(cat /opt/cosmic/router/keepalived_state)
# Legacy pre-Cosmic 6.2 path
elif [ -f /tmp/keepalived_state ]; then
    STATUS=$(cat /tmp/keepalived_state)
fi
echo "Status: ${STATUS}"
