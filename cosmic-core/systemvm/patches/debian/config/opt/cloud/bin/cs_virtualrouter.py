from netaddr import *


def merge(dbag, data):
    print(dbag)

    for key in dbag.keys():
        del dbag[key]

    print(dbag)

    for key in data:
        if key == "type":
            dbag['id'] = data[key]

        if key == "vpc_name":
            dbag[key] = data[key]

        if key == "source_nat_list":
            cidrs = data[key].split(',')
            for cidr in cidrs:
                try:
                    net = IPNetwork(cidr)
                except:
                    print('[ERROR] So it seems we have an faulty CIDR in the source NAT list: ' + cidr)
                    exit(1)

            dbag[key] = data[key]

    return dbag
