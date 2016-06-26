from netaddr import *


def merge(dbag, data):
    search(dbag, data['host_name'])
    # A duplicate ip address wil clobber the old value
    # This seems desirable ....
    if "add" in data and data['add'] is False and \
                    "ipv4_adress" in data:
        if data['ipv4_adress'] in dbag:
            del (dbag[data['ipv4_adress']])
        return dbag
    else:
        dbag[data['ipv4_adress']] = data
    return dbag


def search(dbag, name):
    """
    Dirty hack because CS does not deprovision hosts
    """
    hosts = []
    for o in dbag:
        if o == 'id':
            continue
        print "%s %s" % (dbag[o]['host_name'], name)
        if dbag[o]['host_name'] == name:
            hosts.append(o)
    for o in hosts:
        del (dbag[o])
