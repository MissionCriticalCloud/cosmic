# -- coding: utf-8 --

from netaddr import *


def merge(dbag, ip):
    for dev in dbag:
        if dev == "id":
            continue
        for address in dbag[dev]:
            if address['ip_address'] == ip['ip_address']:
                dbag[dev].remove(address)

    ipo = IPNetwork(ip['ip_address'] + '/' + ip['netmask'])
    ip['broadcast'] = str(ipo.broadcast)
    ip['cidr'] = str(ipo.ip) + '/' + str(ipo.prefixlen)
    ip['size'] = str(ipo.prefixlen)
    ip['network'] = str(ipo.network) + '/' + str(ipo.prefixlen)

    dbag.setdefault(ip['mac_address'], []).append(ip)

    return dbag
