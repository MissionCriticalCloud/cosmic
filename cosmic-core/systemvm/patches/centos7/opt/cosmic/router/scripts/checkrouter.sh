#!/usr/bin/env bash

STATUS=UNKNOWN
if [ -f /tmp/keepalived_state ]; then
    STATUS=$(cat /tmp/keepalived_state)
fi

echo "Status: ${STATUS}"
