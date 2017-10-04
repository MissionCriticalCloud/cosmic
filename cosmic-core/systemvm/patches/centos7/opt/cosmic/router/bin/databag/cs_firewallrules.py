import copy


def merge(dbag, data):
    dbagc = copy.deepcopy(dbag)
    if "rules" not in data:
        return dbagc
    for rule in data['rules']:
        id = str(rule['id'])
        if rule['revoked']:
            if id in dbagc.keys():
                del (dbagc[id])
        elif id not in dbagc.keys():
            dbagc[id] = rule
    return dbagc
