import logging
from jinja2 import Environment, FileSystemLoader


class CsKeepalived(object):
    def __init__(self, dbag):
        self.dbag = dbag

        self.filenames = []
        self.jinja_env = Environment(loader=FileSystemLoader('/opt/cosmic/router/bin/cs/templates'))

        self.keepalived_config_path = '/etc/keepalived/conf.d/'

    def sync(self):
        logging.debug(self.dbag)
        self.write_global_defs()
        self.write_sync_group()
        self.zap_keepalived_config_directory()

    def write_global_defs(self):
        content = self.jinja_env.get_template('keepalived_global_defs.conf').render(
            router_id="hostname_todo"
        )

        self.write_keepalived_config('global_defs.conf', content)

    def write_sync_group(self):
        content = self.jinja_env.get_template('keepalived_sync_group.conf').render(
            sync_group_name="test_sync_group_name",
            vrrp_instances=["instance1", "instance2"]
        )

        logging.debug(content)
        self.write_keepalived_config('sync_group.conf', content)

    def write_keepalived_config(self, filename, content):
        self.filenames.append(filename)

        with open(self.keepalived_config_path + filename, 'w') as f:
            f.write(content)

    def zap_keepalived_config_directory(self):
        # TODO list files in self.keepalived_config_path, exclude the ones in self.filenames and remove them!
        pass
