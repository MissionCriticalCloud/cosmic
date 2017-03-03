# -- coding: utf-8 --

from netaddr import *


def merge(dbag, ip):
    nic_dev_id = None
    for dev in dbag:
        if dev == "id":
            continue
        for address in dbag[dev]:
            if address['public_ip'] == ip['public_ip']:
                if 'nic_dev_id' in address:
                    nic_dev_id = address['nic_dev_id']
                dbag[dev].remove(address)

    ipo = IPNetwork(ip['public_ip'] + '/' + ip['netmask'])
    if 'nic_dev_id' in ip:
        nic_dev_id = ip['nic_dev_id']
    ip['device'] = 'eth' + str(nic_dev_id)
    ip['broadcast'] = str(ipo.broadcast)
    ip['cidr'] = str(ipo.ip) + '/' + str(ipo.prefixlen)
    ip['size'] = str(ipo.prefixlen)
    ip['network'] = str(ipo.network) + '/' + str(ipo.prefixlen)
    if 'nw_type' not in ip.keys():
        ip['nw_type'] = 'public'
    else:
        ip['nw_type'] = ip['nw_type'].lower()
    if ip['nw_type'] == 'control':
        dbag['eth' + str(nic_dev_id)] = [ip]
    else:
        dbag.setdefault('eth' + str(nic_dev_id), []).append(ip)

    return dbag
