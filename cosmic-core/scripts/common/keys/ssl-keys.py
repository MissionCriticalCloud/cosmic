#! /bin/bash


# Copies keys that enable SSH communication with system vms
# $1 = new public key
# $2 = new private key
'''
All imports go here...
'''
import os
import socket
import subprocess
import sys
import traceback


def generateSSLKey(outputPath):
    logf = open("ssl-keys.log", "w")
    hostName = socket.gethostbyname(socket.gethostname())
    keyFile = outputPath + os.sep + "cloudmanagementserver.keystore"
    logf.write("HostName = %s\n" % hostName)
    logf.write("OutputPath = %s\n" % keyFile)
    dname = 'cn="Cloudstack User",ou="' + hostName + '",o="' + hostName + '",c="Unknown"';
    logf.write("dname = %s\n" % dname)
    logf.flush()
    try:
        return_code = subprocess.Popen(
            ["keytool", "-genkey", "-keystore", keyFile, "-storepass", "vmops.com", "-keypass", "vmops.com", "-keyalg", "RSA", "-validity", "3650", "-dname", dname], shell=True,
            stdout=logf, stderr=logf)
        return_code.wait()
    except OSError as e:
        logf.flush()
        traceback.print_exc(file=logf)
    logf.flush()
    logf.write("SSL key generated is : %s" % return_code)
    logf.flush()


argsSize = len(sys.argv)
if argsSize != 2:
    print("Usage: ssl-keys.py <SSL File Key Path>")
    sys.exit(None)
sslKeyPath = sys.argv[1]

generateSSLKey(sslKeyPath)
