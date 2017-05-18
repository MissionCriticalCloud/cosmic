from netaddr import *


def merge(dbag, data):
    # always empty the databag as we will receive all new data
    for key in dbag.keys():
        del dbag[key]

    for key in data:
        if key == "type":
            dbag['id'] = data[key]

        if key == "vpc_name":
            dbag[key] = data[key]

        if key == "source_nat_list":
            # let's verify that the list contains valid CIDRs
            cidrs = data[key].split(',')
            for cidr in cidrs:
                try:
                    net = IPNetwork(cidr)
                except:
                    print('[ERROR] So it seems we have an faulty CIDR in the source NAT list: ' + cidr)
                    exit(1)

            dbag[key] = data[key]

    return dbag
