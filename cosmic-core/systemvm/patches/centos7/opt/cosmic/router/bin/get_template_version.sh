#!/bin/bash

SYSTEMVM_RELEASE=$(cat /etc/cosmic-release)
SCRIPT_SHA512SUM=$(cat /opt/cosmic/patch/cloud-scripts.tgz.sha512)

echo "${SYSTEMVM_RELEASE}&${SCRIPT_SHA512SUM}"
