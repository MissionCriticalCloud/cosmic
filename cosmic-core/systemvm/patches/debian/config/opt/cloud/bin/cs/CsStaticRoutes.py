#!/usr/bin/python
# -- coding: utf-8 --
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

from CsDatabag import CsDataBag
from CsRedundant import *
import sys


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
        self.routes = {}
        for line in output.split('\n'):
            data = line.split()
            if data:
                key = data.pop(0)
                self.routes[key] = data
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
