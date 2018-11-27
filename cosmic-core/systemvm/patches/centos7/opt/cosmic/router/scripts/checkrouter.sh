#!/usr/bin/env bash

STATUS=UNKNOWN
if [ -f /opt/cosmic/router/keepalived_state ]; then
    STATUS=$(cat /opt/cosmic/router/keepalived_state)
fi

echo "Status: ${STATUS}"
