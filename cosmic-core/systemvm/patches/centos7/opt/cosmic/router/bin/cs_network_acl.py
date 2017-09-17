from netaddr import *


def merge(dbag, data):
    dbag[data['device']] = data
    return dbag
