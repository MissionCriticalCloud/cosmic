#!/usr/bin/python


import gzip
import json
import logging
import os
import shutil
import uuid

import cs_cmdline
import cs_dhcp
import cs_firewallrules
import cs_forwardingrules
import cs_guestnetwork
import cs_ip
import cs_loadbalancer
import cs_monitorservice
import cs_network_acl
import cs_remoteaccessvpn
import cs_site2sitevpn
import cs_staticroutes
import cs_vmdata
import cs_vpnusers
import cs_privategateway


class DataBag:
    DPATH = "/etc/cloudstack"

    def __init__(self):
        self.bdata = { }

    def load(self):
        data = self.bdata
        if not os.path.exists(self.DPATH):
            os.makedirs(self.DPATH)
        self.fpath = self.DPATH + '/' + self.key + '.json'
        try:
            handle = open(self.fpath)
        except IOError:
            logging.debug("Creating data bag type %s", self.key)
            data.update({ "id": self.key })
        else:
            logging.debug("Loading data bag type %s", self.key)
            data = json.load(handle)
            handle.close()
        self.dbag = data

    def save(self, dbag):
        try:
            handle = open(self.fpath, 'w')
        except IOError:
            logging.error("Could not write data bag %s", self.key)
        else:
            logging.debug("Writing data bag type %s", self.key)
            logging.debug(dbag)
        jsono = json.dumps(dbag, indent=4, sort_keys=True)
        handle.write(jsono)

    def getDataBag(self):
        return self.dbag

    def setKey(self, key):
        self.key = key


class updateDataBag:
    DPATH = "/etc/cloudstack"

    def __init__(self, qFile):
        self.qFile = qFile
        self.fpath = ''
        self.bdata = { }
        self.process()

    def process(self):
        self.db = DataBag()
        if (self.qFile.type == "staticnatrules" or self.qFile.type == "forwardrules"):
            self.db.setKey("forwardingrules")
        else:
            self.db.setKey(self.qFile.type)
        dbag = self.db.load()
        logging.info("Command of type %s received", self.qFile.type)

        if self.qFile.type == 'ips':
            dbag = self.processIP(self.db.getDataBag())
        elif self.qFile.type == 'guestnetwork':
            dbag = self.processGuestNetwork(self.db.getDataBag())
        elif self.qFile.type == 'cmdline':
            dbag = self.processCL(self.db.getDataBag())
        elif self.qFile.type == 'networkacl':
            dbag = self.process_network_acl(self.db.getDataBag())
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
        elif self.qFile.type == 'privategateway':
            dbag = self.process_privategateway(self.db.getDataBag())
        else:
            logging.error("Error I do not know what to do with file of type %s", self.qFile.type)
            return
        self.db.save(dbag)

    def processGuestNetwork(self, dbag):
        d = self.qFile.data
        dp = { }
        dp['public_ip'] = d['router_guest_ip']
        dp['netmask'] = d['router_guest_netmask']
        dp['source_nat'] = False
        dp['add'] = d['add']
        dp['one_to_one_nat'] = False
        dp['gateway'] = d['router_guest_gateway']
        dp['nic_dev_id'] = d['device'][3]
        dp['nw_type'] = 'guest'
        dp['vif_mac_address'] = d['mac_address']
        qf = QueueFile()
        qf.load({ 'ip_address': [dp], 'type': 'ips' })
        if 'domain_name' not in d.keys() or d['domain_name'] == '':
            d['domain_name'] = "cloudnine.internal"

        return cs_guestnetwork.merge(dbag, d)

    def process_dhcp_entry(self, dbag):
        return cs_dhcp.merge(dbag, self.qFile.data)

    def process_site2sitevpn(self, dbag):
        return cs_site2sitevpn.merge(dbag, self.qFile.data)

    def process_remoteaccessvpn(self, dbag):
        return cs_remoteaccessvpn.merge(dbag, self.qFile.data)

    def process_vpnusers(self, dbag):
        return cs_vpnusers.merge(dbag, self.qFile.data)

    def process_network_acl(self, dbag):
        return cs_network_acl.merge(dbag, self.qFile.data)

    def process_firewallrules(self, dbag):
        return cs_firewallrules.merge(dbag, self.qFile.data)

    def process_loadbalancer(self, dbag):
        return cs_loadbalancer.merge(dbag, self.qFile.data)

    def process_monitorservice(self, dbag):
        return cs_monitorservice.merge(dbag, self.qFile.data)

    def process_staticroutes(self, dbag):
        return cs_staticroutes.merge(dbag, self.qFile.data)

    def process_privategateway(self, dbag):
        d = self.qFile.data
        dp = { }
        dp['public_ip'] = d['ip_address']
        dp['netmask'] = d['netmask']
        dp['source_nat'] = d['source_nat']
        dp['add'] = d['add']
        dp['one_to_one_nat'] = False
        dp['gateway'] = ''
        dp['nic_dev_id'] = d['nic_dev_id']
        dp['nw_type'] = d['type']
        dp['vif_mac_address'] = d['vif_mac_address']
        qf = QueueFile()
        qf.load({ 'ip_address': [dp], 'type': 'ips' })
        if 'domain_name' not in d.keys() or d['domain_name'] == '':
            d['domain_name'] = "cloudnine.internal"

        return cs_privategateway.merge(dbag, d)

    def processForwardingRules(self, dbag):
        # to be used by both staticnat and portforwarding
        return cs_forwardingrules.merge(dbag, self.qFile.data)

    def processIP(self, dbag):
        for ip in self.qFile.data["ip_address"]:
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
