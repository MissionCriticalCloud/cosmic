
def merge(dbag, ip):
    for dev in dbag:
        if dev == "id":
            continue
        for address in dbag[dev]:
            if address['router_guest_ip'] == ip['router_guest_ip']:
                dbag[dev].remove(address)

    dbag.setdefault('eth' + str(ip['nic_dev_id']), []).append(ip)

    return dbag
