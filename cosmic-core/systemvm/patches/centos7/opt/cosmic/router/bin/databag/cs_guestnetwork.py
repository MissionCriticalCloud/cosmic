def merge(dbag, ip):

    dbag[ip['device']] = [ip]
    return dbag
