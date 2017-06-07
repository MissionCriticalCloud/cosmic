#!/usr/bin/python


import gzip
import json
import logging
import os
import shutil
import uuid
import sys

import cs_cmdline
import cs_dhcp
import cs_firewallrules
import cs_forwardingrules
import cs_guestnetwork
import cs_ip
import cs_loadbalancer
import cs_monitorservice
import cs_network_acl
import cs_public_ip_acl
import cs_remoteaccessvpn
import cs_site2sitevpn
import cs_staticroutes
import cs_vmdata
import cs_vpnusers
import cs_privategateway
import cs_virtualrouter
import cs.CsHelper as csHelper


class DataBag:
    DPATH = "/etc/cloudstack"

    def __init__(self):
        self.bdata = { }

    def load(self):
        data = self.bdata
        if not os.path.exists(self.DPATH):
            os.makedirs(self.DPATH)
        self.fpath = os.path.join(self.DPATH, self.key + '.json')

        try:
            with open(self.fpath, 'r') as _fh:
                logging.debug("Loading data bag type %s", self.key)
                data = json.load(_fh)
        except IOError:
            logging.debug("Creating data bag type %s", self.key)
            data.update({ "id": self.key })
        finally:
            self.dbag = data

    def save(self, dbag):
        try:
            with open(self.fpath, 'w') as _fh:
                logging.debug("Writing data bag type %s", self.key)
                json.dump(
                    dbag, _fh,
                    sort_keys=True,
                    indent=2
                )
        except IOError:
            logging.error("Could not write data bag %s", self.key)

    def getDataBag(self):
        return self.dbag

    def setKey(self, key):
        self.key = key


class updateDataBag:
    DPATH = "/etc/cloudstack"

    def __init__(self, qFile):
        self.qFile = qFile
        self.fpath = ''
        self.bdata = {}
        self.process()

    def process(self):
        self.db = DataBag()
        if self.qFile.type == "staticnatrules" or self.qFile.type == "forwardrules":
            self.db.setKey("forwardingrules")
        else:
            self.db.setKey(self.qFile.type)
        self.db.load()
        logging.info("Command of type %s received", self.qFile.type)

        if self.qFile.type == 'ips':
            dbag = self.process_ip(self.db.getDataBag())
        elif self.qFile.type == 'guestnetwork':
            dbag = self.process_guestnetwork(self.db.getDataBag())
        elif self.qFile.type == 'cmdline':
            dbag = self.processCL(self.db.getDataBag())
        elif self.qFile.type == 'networkacl':
            dbag = self.process_network_acl(self.db.getDataBag())
        elif self.qFile.type == 'publicipacl':
            dbag = self.process_public_ip_acl(self.db.getDataBag())
        elif self.qFile.type == 'firewallrules':
            dbag = self.process_firewallrules(self.db.getDataBag())
        elif self.qFile.type == 'loadbalancer':
            dbag = self.process_loadbalancer(self.db.getDataBag())
        elif self.qFile.type == 'monitorservice':
            dbag = self.process_monitorservice(self.db.getDataBag())
        elif self.qFile.type == 'vmdata':
            dbag = self.processVmData(self.db.getDataBag())
        elif self.qFile.type == 'dhcpentry':
            dbag = self.process_dhcp_entry(self.db.getDataBag())
        elif self.qFile.type == 'staticnatrules' or self.qFile.type == 'forwardrules':
            dbag = self.processForwardingRules(self.db.getDataBag())
        elif self.qFile.type == 'site2sitevpn':
            dbag = self.process_site2sitevpn(self.db.getDataBag())
        elif self.qFile.type == 'remoteaccessvpn':
            dbag = self.process_remoteaccessvpn(self.db.getDataBag())
        elif self.qFile.type == 'vpnuserlist':
            dbag = self.process_vpnusers(self.db.getDataBag())
        elif self.qFile.type == 'staticroutes':
            dbag = self.process_staticroutes(self.db.getDataBag())
        elif self.qFile.type == 'ipaliases':
            self.db.setKey('ips')
            self.db.load()
            dbag = self.process_ipaliases(self.db.getDataBag())
        elif self.qFile.type == 'dhcpconfig':
            logging.error("I don't think I need %s anymore", self.qFile.type)
            return
        elif self.qFile.type == 'privategateway':
            dbag = self.process_privategateway(self.db.getDataBag())
        elif self.qFile.type == 'virtualrouter':
            dbag = self.process_virtualrouter(self.db.getDataBag())
        else:
            logging.error("Error I do not know what to do with file of type %s", self.qFile.type)
            return
        self.db.save(dbag)

    def process_guestnetwork(self, dbag):
        d = self.update_dbag_contents()
        self.save_ip_dbag(d['d_ip_to_save'])
        return cs_guestnetwork.merge(dbag, d['d_to_merge'])

    def process_privategateway(self, dbag):
        d = self.update_dbag_contents()
        self.save_ip_dbag(d['d_ip_to_save'])
        return cs_privategateway.merge(dbag, d['d_to_merge'])

        # Networks of type Guest and Private are stored in their own json but in order for interfaces to work properly these need to be
        # stored in ips.json as well.
    def save_ip_dbag(self, dbag):
        qf = QueueFile()
        qf.load({'ip_address': [dbag], 'type': 'ips'})

    # Based on mac address, find the device we should use
    def validate_device_based_on_mac_address(self):
        d_to_merge = self.qFile.data

        mac_address_to_find_device_for = self.get_macaddress_from_databag(d_to_merge)
        if not mac_address_to_find_device_for:
            return d_to_merge
        if 'device' not in d_to_merge and 'nic_dev_id' in d_to_merge:
            d_to_merge['device'] = "eth" + str(d_to_merge['nic_dev_id'])
        elif 'device' not in d_to_merge:
            logging.warning("Unable to validate mac_address / device because we couldn't locate the device in the json (need 'device' or 'nic_dev_id' property.")
            return d_to_merge

        device_name = csHelper.get_device_from_mac_address(mac_address_to_find_device_for)
        if not device_name:
            return d_to_merge
        if device_name != d_to_merge['device']:
            log_message = "Found device %s based on macaddress %s so updating databag accordingly. " \
                          "Ignoring device %s sent by mgt server, as it is not the right one." \
                          % (device_name, mac_address_to_find_device_for, d_to_merge['device'])
            logging.warning(log_message)
            # Keep whatever was sent (for debug purposes)
            if 'device' in d_to_merge:
                d_to_merge['device_as_sent_by_mgtserver'] = d_to_merge['device']
            if 'dev_id' in d_to_merge:
                d_to_merge['dev_id_as_sent_by_mgtserver'] = d_to_merge['dev_id']
            # Use the device we found from now on
            d_to_merge['device'] = device_name
        return d_to_merge

    @staticmethod
    def get_macaddress_from_databag(d_to_merge):
        # Find mac address
        if 'mac_address' in d_to_merge:
            return d_to_merge['mac_address']
        elif 'vif_mac_address' in d_to_merge:
            return d_to_merge['vif_mac_address']
        logging.warning("Unable to validate mac_address / device because we couldn't locate the mac_address in the json (need 'mac_address' or 'vif_mac_address' property.")
        return False

    def update_dbag_contents(self):
        d_to_merge = self.validate_device_based_on_mac_address()
        d_ip_to_save = {}

        # Find mac address
        if 'mac_address' in d_to_merge:
            d_to_merge['vif_mac_address'] = d_to_merge['mac_address']
            d_ip_to_save['vif_mac_address'] = d_to_merge['mac_address']
        elif 'vif_mac_address' in d_to_merge:
            d_ip_to_save['vif_mac_address'] = d_to_merge['vif_mac_address']

        # device and device id
        d_ip_to_save['device'] = d_to_merge['device']
        d_to_merge['nic_dev_id'] = d_to_merge['device'].replace("eth", "")
        d_ip_to_save['nic_dev_id'] = d_to_merge['nic_dev_id']

        # public_ip
        if 'router_guest_ip' in d_to_merge:
            d_ip_to_save['public_ip'] = d_to_merge['router_guest_ip']
        elif 'type' in d_to_merge and d_to_merge['type'] == "guestnetwork":
            d_to_merge['public_ip'] = d_to_merge['ip_address']
            d_ip_to_save['public_ip'] = d_to_merge['ip_address']
        else:
            d_ip_to_save['ip_address'] = d_to_merge['ip_address']

        # netmask
        if 'router_guest_netmask' in d_to_merge:
            d_to_merge['netmask'] = d_to_merge['router_guest_netmask']
            d_ip_to_save['netmask'] = d_to_merge['router_guest_netmask']
        else:
            d_to_merge['netmask'] = d_to_merge['netmask']
            d_ip_to_save['netmask'] = d_to_merge['netmask']

        # sourcenat
        if 'source_nat' in d_to_merge:
            d_ip_to_save['source_nat'] = d_to_merge['source_nat']
        else:
            d_ip_to_save['source_nat'] = False

        # one-to-one nat
        if 'one_to_one_nat' in d_to_merge:
            d_to_merge['one_to_one_nat'] = d_to_merge['one_to_one_nat']
        else:
            d_ip_to_save['one_to_one_nat'] = False

        # gateway
        if 'router_guest_gateway' in d_to_merge:
            d_to_merge['gateway'] = d_to_merge['router_guest_gateway']
            d_ip_to_save['gateway'] = d_to_merge['router_guest_gateway']
        elif 'gateway' in d_to_merge:
            d_to_merge['gateway'] = d_to_merge['gateway']
            d_ip_to_save['gateway'] = d_to_merge['gateway']
        elif 'type' in d_to_merge and d_to_merge['type'] == "guestnetwork":
            d_to_merge['gateway'] = ''
            d_ip_to_save['gateway'] = ''

        # Network type
        if d_to_merge['type'] == "guestnetwork":
            d_ip_to_save['nw_type'] = 'guest'
        elif 'type' in d_to_merge:
            d_ip_to_save['nw_type'] = d_to_merge['type']

        # Set default domain
        if 'domain_name' not in d_to_merge.keys() or d_to_merge['domain_name'] == '':
            d_to_merge['domain_name'] = "cloudnine.internal"

        # Pass the add boolean
        d_ip_to_save['add'] = d_to_merge['add']

        return {'d_ip_to_save': d_ip_to_save, 'd_to_merge': d_to_merge}

    def process_dhcp_entry(self, dbag):
        return cs_dhcp.merge(dbag, self.qFile.data)

    def process_site2sitevpn(self, dbag):
        return cs_site2sitevpn.merge(dbag, self.qFile.data)

    def process_remoteaccessvpn(self, dbag):
        return cs_remoteaccessvpn.merge(dbag, self.qFile.data)

    def process_vpnusers(self, dbag):
        return cs_vpnusers.merge(dbag, self.qFile.data)

    def process_network_acl(self, dbag):
        d_to_merge = self.validate_device_based_on_mac_address()
        return cs_network_acl.merge(dbag, d_to_merge)

    def process_public_ip_acl(self, dbag):
        d_to_merge = self.validate_device_based_on_mac_address()
        return cs_public_ip_acl.merge(dbag, d_to_merge)

    def process_firewallrules(self, dbag):
        return cs_firewallrules.merge(dbag, self.qFile.data)

    def process_loadbalancer(self, dbag):
        return cs_loadbalancer.merge(dbag, self.qFile.data)

    def process_monitorservice(self, dbag):
        return cs_monitorservice.merge(dbag, self.qFile.data)

    def process_staticroutes(self, dbag):
        return cs_staticroutes.merge(dbag, self.qFile.data)

    def processForwardingRules(self, dbag):
        # to be used by both staticnat and portforwarding
        return cs_forwardingrules.merge(dbag, self.qFile.data)

    def process_virtualrouter(self, dbag):
        return cs_virtualrouter.merge(dbag, self.qFile.data)

    def process_ip(self, dbag):
        for ip in self.qFile.data["ip_address"]:
            # Find the right device we should use to configure the ip address
            # vif_mac_address is a mac address per ip-address, based on the mac address of the device
            # The original macaddress of the device is sent as device_mac_address, so we will check based
            # on that macaddress.
            if 'device_mac_address' in ip:
                device_name = csHelper.get_device_from_mac_address(ip['device_mac_address'])
                if not device_name:
                    log_message = "Cannot find device while looking for %s. Ignoring for now, it may arrive later.." \
                                  % ip['device_mac_address']
                    logging.warning(log_message)
                    print("Warning! " + log_message)
                else:
                    if ip['vif_mac_address'] != ip['device_mac_address']:
                        log_message = "Found device %s based on macaddress %s so updating databag accordingly. " \
                                      "Ignoring macaddress %s sent by mgt server, as it is not the right one." \
                                      % (device_name, ip['device_mac_address'], ip['vif_mac_address'])
                        ip['vif_mac_address_as_sent_by_mgt_server'] = ip['vif_mac_address']
                    else:
                        log_message = "The mac address as sent by the management server %s matches the one we found (%s) on device %s so that's good" \
                                      % (ip['vif_mac_address'], ip['device_mac_address'], device_name)
                        logging.info(log_message)

                    logging.warning(log_message)
                    print("[INFO] " + log_message)
                    ip['vif_mac_address'] = ip['device_mac_address']
                    ip['device'] = device_name
                    ip['nic_dev_id'] = device_name.replace("eth","")
            dbag = cs_ip.merge(dbag, ip)
        return dbag

    def processCL(self, dbag):
        # Convert the ip stuff to an ip object and pass that into cs_ip_merge
        # "eth0ip": "192.168.56.32",
        # "eth0mask": "255.255.255.0",
        self.newData = []
        if (self.qFile.data['cmd_line']['type'] == "router"):
            self.processCLItem('0', "guest")
            self.processCLItem('1', "control")
            self.processCLItem('2', "public")
        elif (self.qFile.data['cmd_line']['type'] == "vpcrouter"):
            self.processCLItem('0', "control")
        elif (self.qFile.data['cmd_line']['type'] == "dhcpsrvr"):
            self.processCLItem('0', "guest")
            self.processCLItem('1', "control")
        return cs_cmdline.merge(dbag, self.qFile.data)

    def processCLItem(self, num, nw_type):
        key = 'eth' + num + 'ip'
        dp = { }
        if (key in self.qFile.data['cmd_line']):
            dp['public_ip'] = self.qFile.data['cmd_line'][key]
            dp['netmask'] = self.qFile.data['cmd_line']['eth' + num + 'mask']
            dp['source_nat'] = False
            dp['add'] = True
            dp['one_to_one_nat'] = False
            if nw_type == "public":
                dp['gateway'] = self.qFile.data['cmd_line']['gateway']
            else:
                if('localgw' in self.qFile.data['cmd_line']):
                    dp['gateway'] = self.qFile.data['cmd_line']['localgw']
                else:
                    dp['gateway'] = 'None'
            dp['nic_dev_id'] = num
            dp['nw_type'] = nw_type
            qf = QueueFile()
            qf.load({ 'ip_address': [dp], 'type': 'ips' })

    def processVmData(self, dbag):
        cs_vmdata.merge(dbag, self.qFile.data)
        return dbag

    def process_ipaliases(self, dbag):
        nic_dev = None

        for interface, data in dbag.items():
            if interface == 'id':
                continue
            elif any([net['nw_type'] == 'guest' for net in data]):
                nic_dev = interface
                break
        assert nic_dev is not None, 'Unable to determine Guest interface'

        nic_dev_id = nic_dev[3:]

        for alias in self.qFile.data['aliases']:
            ip = {
                'add': not alias['revoke'],
                'nw_type': 'guest',
                'public_ip': alias['ip_address'],
                'netmask': alias['netmask'],
                'nic_dev_id': nic_dev_id
            }
            dbag = cs_ip.merge(dbag, ip)
        return dbag


class QueueFile:
    fileName = ''
    configCache = "/var/cache/cloud"
    keep = True
    do_merge = True
    data = { }

    def update_databag(self):
        if self.do_merge:
            logging.info("Merging because do_merge is %s" % self.do_merge)
            updateDataBag(self)
        else:
            logging.info("Not merging because do_merge is %s" % self.do_merge)

    def load(self, data):
        if data is not None:
            self.data = data
            self.type = self.data["type"]
            self.update_databag()
            return
        filename = '{cache_location}/{json_file}'.format(cache_location=self.configCache, json_file=self.fileName)
        try:
            handle = open(filename)
        except IOError as exception:
            error_message = ("Exception occurred with the following exception error '{error}'. Could not open '{file}'. "
                             "It seems that the file has already been moved.".format(error=exception, file=filename))
            logging.error(error_message)
        else:
            logging.info("Continuing with the processing of file '{file}'".format(file=filename))

            self.data = json.load(handle)
            self.type = self.data["type"]
            handle.close()
            if self.keep:
                self.__moveFile(filename, self.configCache + "/processed")
            else:
                os.remove(filename)
            self.update_databag()

    def setFile(self, name):
        self.fileName = name

    def getType(self):
        return self.type

    def getData(self):
        return self.data

    def setPath(self, path):
        self.configCache = path

    def __moveFile(self, origPath, path):
        if not os.path.exists(path):
            os.makedirs(path)

        originalName = os.path.basename(origPath)

        if originalName.count(".") == 1:
            originalName += "." + str(uuid.uuid4())

        zipped_file_name = path + "/" + originalName + ".gz"

        with open(origPath, 'rb') as f_in, gzip.open(zipped_file_name, 'wb') as f_out:
            shutil.copyfileobj(f_in, f_out)
        os.remove(origPath)

        logging.debug("Processed file written to %s", zipped_file_name)
