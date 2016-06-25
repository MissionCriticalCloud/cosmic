#!/usr/bin/python
#

#

from xmlrpclib import ServerProxy, Error


def spCon(proto, host, port):
    print "trying %s on %s:%s" % (proto, host, port)
    try:
        x = ServerProxy("%s://%s:%s" % (proto, host, port))
        x.echo(proto)
        return x
    except Error, v:
        print "ERROR", v
        return


def getCon(host, port):
    try:
        server = spCon("http", host, port)
    except Error, v:
        print "ERROR", v
        server = spCon("https", host, port)

    return server


# hmm master actions don't apply to a slave
port = 8899
user = "oracle"
password = "test123"
auth = "%s:%s" % (user, password)
host = "localhost"

print "setting up password"
try:
    con = getCon(host, port)
    print con.update_agent_password(user, password)
except Error, v:
    print "ERROR", v
