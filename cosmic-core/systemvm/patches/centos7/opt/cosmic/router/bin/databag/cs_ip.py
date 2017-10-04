from netaddr import *


def merge(dbag, ip):

    # Private gateway ip address needs to be in ips.json too, but doesn't have 'public_ip' field
    if 'public_ip' not in ip:
        ip['public_ip'] = ip['ip_address']

    for dev in dbag:
        if dev == "id":
            continue
        for address in dbag[dev]:
            if address['public_ip'] == ip['public_ip']:
                dbag[dev].remove(address)

    ipo = IPNetwork(ip['public_ip'] + '/' + ip['netmask'])
    ip['device'] = 'eth' + str(ip['nic_dev_id'])
    ip['broadcast'] = str(ipo.broadcast)
    ip['cidr'] = str(ipo.ip) + '/' + str(ipo.prefixlen)
    ip['size'] = str(ipo.prefixlen)
    ip['network'] = str(ipo.network) + '/' + str(ipo.prefixlen)
    if 'nw_type' not in ip.keys():
        ip['nw_type'] = 'public'
    else:
        ip['nw_type'] = ip['nw_type'].lower()
    if ip['nw_type'] == 'control':
        dbag['eth' + str(ip['nic_dev_id'])] = [ip]
    else:
        dbag.setdefault('eth' + str(ip['nic_dev_id']), []).append(ip)

    return dbag
