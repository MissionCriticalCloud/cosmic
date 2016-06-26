# -- coding: utf-8 --



def merge(dbag, vpn):
    key = vpn['vpn_server_ip']
    op = vpn['create']
    if key in dbag.keys() and not op:
        del (dbag[key])
    else:
        dbag[key] = vpn
    return dbag
