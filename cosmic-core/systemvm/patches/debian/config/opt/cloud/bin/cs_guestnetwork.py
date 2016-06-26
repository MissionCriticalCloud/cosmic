keys = ['eth1', 'eth2', 'eth3', 'eth4', 'eth5', 'eth6', 'eth7', 'eth8', 'eth9']


def merge(dbag, gn):
    device = gn['device']

    if not gn['add'] and device in dbag:

        if dbag[device]:
            device_to_die = dbag[device][0]
            try:
                dbag[device].remove(device_to_die)
            except ValueError, e:
                print "[WARN] cs_guestnetwork.py :: Error occurred removing item from databag. => %s" % device_to_die
                del (dbag[device])
        else:
            del (dbag[device])

    else:
        dbag.setdefault(device, []).append(gn)

    return dbag
