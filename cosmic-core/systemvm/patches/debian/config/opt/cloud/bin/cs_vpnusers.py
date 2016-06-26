# -- coding: utf-8 --


import copy


def merge(dbag, data):
    dbagc = copy.deepcopy(dbag)

    print dbag
    print data
    if "vpn_users" not in data:
        return dbagc

    # remove previously deleted user from the dict
    for user in dbagc.keys():
        if user == 'id':
            continue
        userrec = dbagc[user]
        add = userrec['add']
        if not add:
            del (dbagc[user])

    for user in data['vpn_users']:
        username = user['user']
        add = user['add']
        if username not in dbagc.keys():
            dbagc[username] = user
        elif username in dbagc.keys() and not add:
            dbagc[username] = user

    return dbagc
