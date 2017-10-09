def merge(dbag, data):
    """ Simply overwrite the existsing bag as, the whole configuration is sent every time """
    if "rules" not in data:
        return dbag
    dbag['config'] = data['rules']
    return dbag
