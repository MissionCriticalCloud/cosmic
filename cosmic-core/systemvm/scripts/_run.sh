#!/usr/bin/env bash

set -x

COSMIC_CONF=/etc/cosmic/agent
COSMIC_HOME=/opt/cosmic/agent
CMDLINE=$(cat /var/cache/cloud/cmdline)

# Empty agent.properties
mkdir -p $COSMIC_CONF
echo "" > $COSMIC_CONF/agent.properties

for i in $CMDLINE
  do
     KEY=$(echo $i | cut -s -d= -f1)
     VALUE=$(echo $i | cut -s -d= -f2)
     [ "$KEY" == "" ] && continue
     echo "$KEY=$VALUE" >> $COSMIC_CONF/agent.properties
  done


cd $COSMIC_HOME; java -Djavax.net.ssl.trustStore=./certs/realhostip.keystore -Djsse.enableSNIExtension=false -jar $COSMIC_HOME/cloud-agent-*.jar
