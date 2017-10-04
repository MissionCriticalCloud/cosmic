# -- coding: utf-8 --

from netaddr import *
from cs.CsHelper import *

def merge(dbag, ip):

    # Private gateway ip address needs to be in ips.json too, but doesn't have 'public_ip' field
    if 'public_ip' not in ip:
        ip['public_ip'] = ip['ip_address']

    for identifier in dbag:
        if identifier == "id":
            continue
        for address in dbag[identifier]:
            if address['public_ip'] == ip['public_ip']:
                dbag[identifier].remove(address)

    ipo = IPNetwork(ip['public_ip'] + '/' + ip['netmask'])
    ip['broadcast'] = str(ipo.broadcast)
    ip['cidr'] = str(ipo.ip) + '/' + str(ipo.prefixlen)
    ip['size'] = str(ipo.prefixlen)
    ip['network'] = str(ipo.network) + '/' + str(ipo.prefixlen)
    if 'nw_type' not in ip.keys():
        ip['nw_type'] = 'public'
    else:
        ip['nw_type'] = ip['nw_type'].lower()

    # Get device from mac address
    device = get_device_from_mac_address(ip['mac_address'])
    if device == "false":
        device = ""
    ip['device'] = device

    # Merge
    dbag.setdefault(ip['mac_address'], []).append(ip)

    return dbag
