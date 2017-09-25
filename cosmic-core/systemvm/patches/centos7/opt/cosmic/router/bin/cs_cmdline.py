def merge(dbag, cmdline):
    if 'redundant_router' not in cmdline['cmd_line']:
        cmdline['cmd_line']['redundant_router'] = "false"
    dbag['config'] = cmdline['cmd_line']
    return dbag
