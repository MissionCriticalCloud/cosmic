# -- coding: utf-8 --



def merge(dbag, staticroutes):
    for route in staticroutes['routes']:
        key = route['cidr']
        dbag[key] = route
    return dbag
