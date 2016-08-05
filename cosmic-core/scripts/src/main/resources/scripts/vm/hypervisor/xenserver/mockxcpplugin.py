#! /usr/bin/python

# This is for test purpose, to test xcp plugin

import XenAPI
import socket
import sys


def getHost():
    hostname = socket.gethostname()
    url = "http://localhost"
    session = XenAPI.Session(url)
    session.xenapi.login_with_password("root", "password")
    host = session.xenapi.host
    hosts = session.xenapi.host.get_by_name_label(hostname)
    if len(hosts) != 1:
        print "can't find host:" + hostname
        sys.exit(1)
    localhost = hosts[0]
    return [host, localhost]


def callPlugin(pluginName, func, params):
    hostPair = getHost()
    host = hostPair[0]
    localhost = hostPair[1]
    return host.call_plugin(localhost, pluginName, func, params)


def main():
    if len(sys.argv) < 3:
        print "args: pluginName funcName params"
        sys.exit(1)

    pluginName = sys.argv[1]
    funcName = sys.argv[2]

    paramList = sys.argv[3:]
    if (len(paramList) % 2) != 0:
        print "params must be name/value pair"
        sys.exit(2)
    params = { }
    pos = 0;
    for i in range(len(paramList) / 2):
        params[str(paramList[pos])] = str(paramList[pos + 1])
        pos = pos + 2
    print "call: " + pluginName + " " + funcName + ", with params: " + str(params)
    print "return: " + callPlugin(pluginName, funcName, params)


if __name__ == "__main__":
    main()
