#!/usr/bin/env bash




#runs the secondary storage service as a standalone server
#i.e., not in the system vm

CP=./:./conf
for file in *.jar
do
  CP=${CP}:$file
done
keyvalues=
#LOGHOME=/var/log/cloud/
LOGHOME=$PWD/

java -Djavax.net.ssl.trustStore=./certs/realhostip.keystore -Dlog.home=$LOGHOME -cp $CP com.cloud.agent.AgentShell $keyvalues $@
