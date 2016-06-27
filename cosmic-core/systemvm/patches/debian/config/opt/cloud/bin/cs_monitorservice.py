from netaddr import *


def merge(dbag, data):
    if "config" in data:
        dbag['config'] = data["config"]
    return dbag
