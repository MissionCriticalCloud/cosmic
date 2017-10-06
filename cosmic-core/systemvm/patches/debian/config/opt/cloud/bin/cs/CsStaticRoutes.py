#!/usr/bin/python
# -- coding: utf-8 --


import sys

from CsDatabag import CsDataBag
from CsRedundant import *


class CsStaticRoutes(CsDataBag):
    def process(self):
        is_master = self.cl.is_master()
        if self.cl.is_redundant() and not is_master:
            logging.debug("Not processing CsStaticRoutes file ==> %s because redundant state is %s" %
                          (self.dbag, str(is_master)))
            return True

        logging.debug("Processing CsStaticRoutes file ==> %s" % self.dbag)

        for item in self.dbag:
            if item == "id":
                continue
            result = self.__update(self.dbag[item])
            logging.debug("Processing item from data bag: %s, returncode: %s" % (self.dbag[item], result))
            if result is not None and result is False:
                logging.debug("Executing static route command returned False, exiting.")
                sys.exit(1)

    def get_routes(self):
        try:
            if len(self.routes) == 0:
                self.set_routes()
        except:
            self.set_routes()
        return self.routes

    def set_routes(self):
        command = "ip route show | grep via | awk '{print $1, $3}'"
        output = CsHelper.get_output_of_command(command)
        self.routes = { }
        for line in output.split('\n'):
            data = line.split()
            if data:
                key = data.pop(0)
                if key == 'default':
                    key = '0.0.0.0/0'
                self.routes[key] = data.pop(0)
        logging.debug("Found these existing routes: %s" % self.routes)
        return self.routes

    def route_exists(self, cidr):
        if cidr in self.routes:
            return True
        return False

    def __update(self, route):
        # Compatibility with old names pre 5.1.0
        if 'cidr' not in route:
            route['cidr'] = route['network']
        if 'ip_address' not in route:
            route['ip_address'] = route['gateway']

        self.get_routes()

        # /32 routes do not need a CIDR notation for adding, and should not have it for deleting
        if route['cidr'].find('/32') > -1:
            logging.debug("Stripping /32 from cidr %s" % route['cidr'])
            route['cidr'] = route['cidr'].replace('/32', '')

        # Only delete when the route is active
        if route['revoke'] and self.route_exists(route['cidr']):
            logging.debug("Route for %s to %s found, deleting.." % (route['cidr'], route['ip_address']))
            command = "ip route del %s" % (route['cidr'])
            result = CsHelper.execute2(command)
            logging.debug("Executed %s, returncode: %s" % (command, result.returncode))

        # Only add when route is not active yet
        if not route['revoke'] and not self.route_exists(route['cidr']):
            logging.debug("Route for %s to %s NOT found, adding.." % (route['cidr'], route['ip_address']))
            command = "ip route add %s via %s" % (route['cidr'], route['ip_address'])
            result = CsHelper.execute2(command)
            logging.debug("Executed %s, returncode: %s" % (command, result.returncode))
            if result.returncode > 0:
                return False

        # Only replace when route is active but with the wrong gateway
        if not route['revoke'] and self.route_exists(route['cidr']) and self.routes[route['cidr']] != route['ip_address']:
            logging.debug("Route for %s to %s found, which is not %s via %s, replacing.." %
                          (route['cidr'], self.routes[route['cidr']], route['cidr'], route['ip_address']))
            command = "ip route replace %s via %s" % (route['cidr'], route['ip_address'])
            result = CsHelper.execute2(command)
            logging.debug("Executed %s, returncode: %s" % (command, result.returncode))
            if result.returncode > 0:
                return False
