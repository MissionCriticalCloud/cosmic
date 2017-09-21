#!/usr/bin/env python

import json
import sys
import subprocess

NETWORK_OVERVIEW = "/etc/cosmic/router/network_overview.json"


def main():
    batchinfo = ""
    exittext = "IPsec SA not found;Site-to-site VPN have not connected"
    with open(NETWORK_OVERVIEW, 'r') as f:
        JSON = f.read()

        vpns = json.loads(JSON).get('vpn', {})
        for vpn in vpns['site2site']:
            exitcode = 11
            peers = vpn['peer_list'].split(',')

            for peer in peers:
                vpnname = "vpn-%s-%s" % (vpn['right'], peer.replace("/", "_"))
                output = subprocess.check_output(["strongswan", "status", vpnname])
                if "INSTALLED" in output:
                    exittext = "IPsec SA found;Site-to-site VPN have connected"
                    exitcode = 0

                batchinfo += "%s:%s:%s&" % (vpn['right'], exitcode, exittext)
            print batchinfo
    return 0


if __name__ == '__main__':
    sys.exit(main())
