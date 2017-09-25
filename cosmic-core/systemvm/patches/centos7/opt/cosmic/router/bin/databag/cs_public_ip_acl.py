def merge(dbag, data):
    dbag[data['public_ip']] = data
    return dbag
