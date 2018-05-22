#!/usr/bin/python

from optparse import OptionParser

import os
import yaml

if __name__ == '__main__':
    parser = OptionParser()
    parser.add_option("-m", "--host", dest="mgt", help="Management server hostname or IP-Address")
    parser.add_option("-z", "--zone", dest="zone", help="zone id")
    parser.add_option("-p", "--pod", dest="pod", help="pod id")
    parser.add_option("-c", "--cluster", dest="cluster", help="cluster id")
    parser.add_option("-g", "--guid", dest="guid", help="guid")

    (options, args) = parser.parse_args()

    config = ''

    with open('/etc/cosmic/agent/application.yml', 'r') as f:
        config = yaml.load(f)
        config['cosmic']['guid'] = options.guid
        config['cosmic']['hosts'] = [options.mgt]
        config['cosmic']['cluster'] = options.cluster
        config['cosmic']['pod'] = options.pod
        config['cosmic']['zone'] = options.zone
        config['cosmic']['localstorage']['uuid'] = options.guid

    with open('/etc/cosmic/agent/application.yml', 'w') as f:
        yaml.dump(config, f, default_flow_style=False)

    os.system("sudo systemctl start cosmic-agent")
