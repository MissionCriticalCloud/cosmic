""" Base class for all Cosmic resources
    -Virtual machine, Volume, Snapshot etc
"""

import base64
from abc import ABCMeta, abstractmethod

from .common import *
from .utils import *


class BaseAbstract:
    __metaclass__ = ABCMeta

    @abstractmethod
    def __init__(self, items, **kwargs):
        self.id = None
        if type(items) is dict:
            self.__dict__.update(items)
        self.__dict__.update(kwargs)

    def __getitem__(self, item):
        return self.__dict__[item]

    @classmethod
    def list(cls, items, **kwargs):
        new_items = []
        for item in items:
            new_items.append(cls(item, **kwargs))
        return new_items


class Domain(BaseAbstract):
    """ Domain Life Cycle """

    def __init__(self, items):
        super(Domain, self).__init__(items)

    @classmethod
    def create(cls, api_client, services=None, name=None, networkdomain=None,
               parentdomainid=None, randomizeID=True):
        """Creates an domain"""

        cmd = {}

        if "domainUUID" in services:
            cmd['domainid'] = ("-".join([services["domainUUID"], random_gen()]) if randomizeID else services["domainUUID"])

        if name:
            cmd['name'] = ("-".join([name, random_gen()]) if randomizeID else name)
        elif "name" in services:
            cmd['name'] = ("-".join([services["name"], random_gen()]) if randomizeID else services["name"])

        if networkdomain:
            cmd['networkdomain'] = networkdomain
        elif "networkdomain" in services:
            cmd['networkdomain'] = services["networkdomain"]

        if parentdomainid:
            cmd['parentdomainid'] = parentdomainid
        elif "parentdomainid" in services:
            cmd['parentdomainid'] = services["parentdomainid"]
        try:
            domain = api_client.createDomain(**cmd)
            if domain is not None:
                return Domain(domain['domain'])
        except Exception as e:
            raise e

    def delete(self, api_client, cleanup=None):
        """Delete an domain"""
        cmd = {'id': self.id}
        if cleanup:
            cmd['cleanup'] = cleanup
        api_client.deleteDomain(**cmd)

    @classmethod
    def list(cls, api_client, **kwargs):
        """Lists domains"""
        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return super(Domain, cls).list(api_client.listDomains(**cmd)['domain'])


class Account(BaseAbstract):
    """ Account Life Cycle """

    def __init__(self, items, api_client=None):
        super(Account, self).__init__(items)
        if 'user' in self.__dict__:
            self.__dict__['user'] = super(Account, self).list(self.__dict__['user'])

    @classmethod
    def create(cls, api_client, services, admin=False, domainid=None, randomizeID=True):
        """Creates an account"""
        cmd = {}

        if "accounttype" in services:
            cmd['accounttype'] = services["accounttype"]
        else:
            # 0 - User, 1 - Root Admin, 2 - Domain Admin
            cmd['accounttype'] = 2 if (admin and domainid) else int(admin)

        cmd['email'] = services["email"]
        cmd['firstname'] = services["firstname"]
        cmd['lastname'] = services["lastname"]

        cmd['password'] = services["password"]
        username = services["username"]
        if randomizeID:
            # Limit account username to 99 chars to avoid failure
            # 6 chars start string + 85 chars api_clientid + 6 chars random string + 2 chars joining hyphen string = 99
            username = username[:6]
            api_clientid = api_client.id[-85:] if len(api_client.id) > 85 else api_client.id
            cmd['username'] = "-".join([username, random_gen(uuid=api_clientid, size=6)])
        else:
            cmd['username'] = username

        if "accountUUID" in services:
            cmd['accountid'] = ("-".join([services["accountUUID"], random_gen()]) if randomizeID else services["accountUUID"])

        if "userUUID" in services:
            cmd['userid'] = ("-".join([services["userUUID"], random_gen()]) if randomizeID else services["userUUID"])

        if domainid:
            cmd['domainid'] = domainid
        account = api_client.createAccount(**cmd)

        return Account(account['account'])

    def delete(self, api_client):
        """Delete an account"""
        cmd = {'id': self.id}
        api_client.deleteAccount(**cmd)

    @classmethod
    def list(cls, api_client, **kwargs):
        """Lists accounts and provides detailed account information for
        listed accounts"""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return super(Account, cls).list(api_client.listAccounts(**cmd)['account'])

    def disable(self, api_client, lock=False):
        """Disable an account"""
        cmd = {'id': self.id, 'lock': lock}
        api_client.disableAccount(**cmd)


class User(BaseAbstract):
    """ User Life Cycle """

    def __init__(self, items):
        super(User, self).__init__(items)

    @classmethod
    def create(cls, api_client, services, account, domainid, randomizeID=True):
        """Creates an user"""
        cmd = {'account': account, 'domainid': domainid, 'email': services['email'], 'firstname': services['firstname'], 'lastname': services['lastname']}

        if "userUUID" in services:
            cmd['userid'] = ("-".join([services["userUUID"], random_gen()]) if randomizeID else services["userUUID"])

        cmd['password'] = services["password"]
        cmd['username'] = ("-".join([services["username"], random_gen()]) if randomizeID else services["username"])
        user = api_client.createUser(**cmd)

        return User(user.__dict__)

    def delete(self, api_client):
        """Delete an account"""
        cmd = {'id': self.id}
        api_client.deleteUser(**cmd)

    @classmethod
    def list(cls, api_client, **kwargs):
        """Lists users and provides detailed account information for
        listed users"""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return super(User, cls).list(api_client.listUsers(**cmd)['user'])

    @classmethod
    def registerUserKeys(cls, api_client, userid):
        cmd = {'id': userid}
        return User(api_client.registerUserKeys(**cmd)['userkeys'])

    def update(self, api_client, **kwargs):
        """Updates the user details"""

        cmd = {'id': self.id}
        cmd.update(kwargs)
        return api_client.updateUser(**cmd)

    @classmethod
    def update(cls, api_client, id, **kwargs):
        """Updates the user details (class method)"""

        cmd = {'id': id}
        cmd.update(kwargs)
        return api_client.updateUser(**cmd)

    @classmethod
    def login(cls, api_client, username, password, domain=None, domainid=None):
        """Logins to the CloudStack"""

        cmd = {}
        if domain:
            cmd['domain'] = domain
        if domainid:
            cmd['domainId'] = domainid
        return api_client.login(**cmd)


class VirtualMachine(BaseAbstract):
    """Manage virtual machine lifecycle"""

    '''Class level variables'''
    __ssh_ip = None
    __public_ip = None

    # Variables denoting VM state - end

    def __init__(self, items, services=None, api_client=None):
        super(VirtualMachine, self).__init__(items=items, api_client=api_client)
        if services is None:
            services = {}
        if "username" in services:
            self.username = services["username"]
        else:
            self.username = 'root'
        if "password" in services:
            self.password = services["password"]
        else:
            self.password = 'password'
        if "ssh_port" in services:
            self.ssh_port = services["ssh_port"]
        else:
            self.ssh_port = 22
        self.ssh_client = None
        if 'nic' in self.__dict__ and type(self.__dict__['nic'][0]) is dict:
            # extract out the ipaddress
            self.ipaddress = items['nic'][0]['ipaddress']
            self.nic = super(VirtualMachine, self).list(items['nic'])

    @property
    def ssh_ip(self):
        return self.__ssh_ip

    @property
    def public_ip(self):
        return self.__public_ip

    @public_ip.setter
    def public_ip(self, ip):
        self.__public_ip = ip

    @classmethod
    def ssh_access_group(cls, api_client, cmd):
        """
        Programs the security group with SSH
         access before deploying virtualmachine
        @return:
        """
        zone_list = Zone.list(
            api_client,
            id=cmd.zoneid if cmd.zoneid else None,
            domainid=cmd.domainid if cmd.domainid else None
        )
        zone = zone_list[0]

    @classmethod
    def access_ssh_over_nat(
            cls, api_client, services, virtual_machine, allow_egress=False,
            networkid=None):
        """
        Program NAT and PF rules to open up ssh access to deployed guest
        @return:
        """

        vpcid = None
        network = None

        if networkid is not None:
            network = Network.list(
                api_client=api_client,
                accountid=virtual_machine.account,
                domainid=virtual_machine.domainid,
                id=networkid
            )
            if network is not None:
                network = network['network'][0]
                vpcid = network['vpcid']

        public_ip = PublicIPAddress.create(
            api_client=api_client,
            accountid=virtual_machine.account,
            zoneid=virtual_machine.zoneid,
            domainid=virtual_machine.domainid,
            services=services,
            networkid=networkid,
            vpcid=vpcid
        )

        if vpcid is None:
            FireWallRule.create(
                api_client=api_client,
                ipaddressid=public_ip.id,
                protocol='TCP',
                cidrlist=['0.0.0.0/0'],
                startport=22,
                endport=22
            )
        elif network is not None:
            acl_list = NetworkACLList.list(
                api_client=api_client,
                accountid=virtual_machine.account,
                domainid=virtual_machine.domainid,
                id=network['aclid']
            )

            acl_name = acl_list[0].name
            if (acl_name != "default_allow") and (acl_name != "default_deny"):
                target_acl = NetworkACL.list(
                    api_client=api_client,
                    accountid=virtual_machine.account,
                    domainid=virtual_machine.domainid,
                    aclid=acl_list[0].id
                )

                services_acl = {"protocol": services["protocol"] if "protocol" in services else 'TCP',
                                "startport": services["publicport"] if "publicport" in services else 22,
                                "endport": services["publicport"] if "publicport" in services else 22,
                                "cidrlist": ['0.0.0.0/0'], "action": 'Allow', "traffictype": 'Ingress'}

                ace_number = 1
                for ace in target_acl:
                    ace_number = max(ace.number, ace_number)
                ace_number += 1

                services_acl["number"] = ace_number

                NetworkACL.create(
                    api_client=api_client,
                    services=services_acl,
                    networkid=networkid,
                    aclid=network['aclid'],
                )

        nat_rule = NATRule.create(
            api_client=api_client,
            virtual_machine=virtual_machine,
            services=services,
            ipaddressid=public_ip.id,
            networkid=networkid
        )
        if allow_egress:
            if vpcid is None:
                try:
                    EgressFireWallRule.create(
                        api_client=api_client,
                        networkid=virtual_machine.nic[0].networkid,
                        protocol='All',
                        cidrlist='0.0.0.0/0'
                    )
                except Exception as e:
                    # This could fail because we've already set up the same rule
                    if not "There is already a firewall rule specified".lower() in e.errorMsg.lower():
                        raise

        virtual_machine.__ssh_ip = nat_rule.ipaddress
        virtual_machine.public_ip = nat_rule.ipaddress

    @classmethod
    def create(cls, api_client, services=None, templateid=None, accountid=None, domainid=None, zoneid=None,
               networkids=None, serviceofferingid=None, projectid=None, startvm=None,
               diskofferingid=None, affinitygroupnames=None, affinitygroupids=None, group=None, hostid=None,
               keypair=None, ipaddress=None, mode='default', method='GET', hypervisor=None, customcpunumber=None,
               customcpuspeed=None, custommemory=None, rootdisksize=None, zone=None, networks=None, account=None,
               network_and_ip_list=None, data=None):
        """Create the instance"""
        if data:
            services = data
        cmd = {}
        allow_egress = False

        if serviceofferingid:
            cmd['serviceofferingid'] = serviceofferingid
        elif "serviceoffering" in services:
            cmd['serviceofferingid'] = services["serviceoffering"]
        elif "serviceofferingname" in services:
            serviceoffering = get_virtual_machine_offering(api_client, services["serviceofferingname"])
            cmd['serviceofferingid'] = serviceoffering.id

        if zoneid:
            cmd['zoneid'] = zoneid
        elif "zoneid" in services:
            cmd['zoneid'] = services["zoneid"]
        elif zone:
            cmd['zoneid'] = zone.id

        if hypervisor:
            cmd['hypervisor'] = hypervisor

        if "displayname" in services:
            cmd['displayname'] = services["displayname"]

        if "name" in services:
            cmd['name'] = services["name"]

        if accountid:
            cmd['account'] = accountid
        elif "account" in services:
            cmd['account'] = services["account"]
        elif account:
            cmd['account'] = account.name
        if domainid:
            cmd['domainid'] = domainid
        elif "domainid" in services:
            cmd['domainid'] = services["domainid"]
        elif account:
            cmd['domainid'] = account.domainid

        if networkids:
            cmd['networkids'] = networkids
        elif "networkids" in services:
            cmd['networkids'] = services["networkids"]
        elif networks:
            cmd['networkids'] = []
            for network in networks:
                cmd.networkids.append(network.id)
        elif network_and_ip_list:
            cmd['iptonetworklist'] = network_and_ip_list
        else:
            # When no networkids are passed, network
            # is created using the "defaultOfferingWithSourceNAT"
            # which has an egress policy of DENY. But guests in tests
            # need access to test network connectivity
            allow_egress = True

        if templateid:
            cmd['templateid'] = templateid
        elif "template" in services:
            cmd['templateid'] = services["template"]
        elif "templatename" in services:
            template = get_template(api_client, template_name=services["templatename"])
            cmd['templateid'] = template.id

        if diskofferingid:
            cmd['diskofferingid'] = diskofferingid
        elif "diskoffering" in services:
            cmd['diskofferingid'] = services["diskoffering"]

        if keypair:
            cmd['keypair'] = keypair
        elif "keypair" in services:
            cmd['keypair'] = services["keypair"]

        if ipaddress:
            cmd['ipaddress'] = ipaddress
        elif ipaddress in services:
            cmd['ipaddress'] = services["ipaddress"]

        if "affinitygroupnames" in services:
            cmd['affinitygroupnames'] = services["affinitygroupnames"]
        elif affinitygroupnames:
            cmd['affinitygroupnames'] = affinitygroupnames

        if affinitygroupids:
            cmd['affinitygroupids'] = affinitygroupids

        if projectid:
            cmd['projectid'] = projectid

        if startvm is not None:
            cmd['startvm'] = startvm

        if hostid:
            cmd['hostid'] = hostid

        if "userdata" in services:
            cmd['userdata'] = base64.urlsafe_b64encode(services["userdata"])

        cmd['details'] = [{}]

        if customcpunumber:
            # TODO: This is probably not correct, fix it
            cmd['details[0]["cpuNumber"]'] = customcpunumber

        if customcpuspeed:
            # TODO: This is probably not correct, fix it
            cmd['details[0]["cpuSpeed"]'] = customcpuspeed

        if custommemory:
            # TODO: This is probably not correct, fix it
            cmd['details[0]["memory"]'] = custommemory

        if rootdisksize and rootdisksize >= 0:
            cmd['details[0]["rootdisksize"]'] = rootdisksize

        if group:
            cmd['group'] = group

        # program default access to ssh
        if mode.lower() == 'basic':
            cls.ssh_access_group(api_client, cmd)

        virtual_machine = VirtualMachine(api_client.deployVirtualMachine(method=method, **cmd), services, api_client=api_client)
        waitforjob(api_client, virtual_machine.jobid)

        virtual_machine = cls.list(api_client=api_client, method=method, id=virtual_machine.id)[0]
        # virtual_machine['ssh_ip'] = virtual_machine['nic'][0]['ipaddress']
        cls.__ssh_ip = virtual_machine.nic[0].ipaddress
        if startvm is False:
            # virtual_machine['public_ip'] = virtual_machine['nic'][0]['ipaddress']
            cls.__public_ip = virtual_machine.nic[0].ipaddress
            # return VirtualMachine(virtual_machine, services)
            return virtual_machine

        # program ssh access over NAT via PF
        if mode.lower() == 'advanced':
            cls.access_ssh_over_nat(
                api_client,
                services,
                virtual_machine,
                allow_egress=allow_egress,
                networkid=cmd['networkids'][0] if cmd['networkids'] else None)
        elif mode.lower() == 'basic':
            if cls.__publicip is not None:
                vm_ssh_ip = virtual_machine.publicip
            else:
                # regular basic zone with security group
                vm_ssh_ip = virtual_machine.nic[0].ipaddress
            # virtual_machine['ssh_ip'] = vm_ssh_ip
            cls.__ssh_ip = vm_ssh_ip
            # virtual_machine['public_ip'] = vm_ssh_ip
            cls.__public_ip = vm_ssh_ip

        # return VirtualMachine(virtual_machine, services)
        return virtual_machine

    def start(self, api_client):
        """Start the instance"""
        cmd = {'id': self.id}
        api_client.startVirtualMachine(**cmd)
        response = self.validateState(api_client, RUNNING)
        if response[0] == FAIL:
            raise Exception(response[1])
        return

    def stop(self, api_client, forced=None):
        """Stop the instance"""
        cmd = {'id': self.id}
        if forced:
            cmd['forced'] = forced
        api_client.stopVirtualMachine(**cmd)
        response = self.validateState(api_client, STOPPED)
        if response[0] == FAIL:
            raise Exception(response[1])
        return

    def reboot(self, api_client):
        """Reboot the instance"""
        cmd = {'id': self.id}
        api_client.rebootVirtualMachine(**cmd)

        response = self.validateState(api_client, RUNNING)
        if response[0] == FAIL:
            raise Exception(response[1])

    def recover(self, api_client):
        """Recover the instance"""
        cmd = {'id': self.id}
        api_client.recoverVirtualMachine(**cmd)

        response = self.validateState(api_client, STOPPED)
        if response[0] == FAIL:
            raise Exception(response[1])

    def restore(self, api_client, templateid=None):
        """Restore the instance"""
        cmd = {'virtualmachineid': self.id}
        if templateid:
            cmd['templateid'] = templateid
        return api_client.restoreVirtualMachine(**cmd)

    def get_ssh_client(
            self, ipaddress=None, reconnect=False, port=None,
            keyPairFileLocation=None, retries=20, retryinterv=30, timeout=10.0):
        """Get SSH object of VM"""

        # If NAT Rules are not created while VM deployment in Advanced mode
        # then, IP address must be passed
        if ipaddress is not None:
            self.__ssh_ip = ipaddress
        if port:
            self.ssh_port = port

        if keyPairFileLocation is not None:
            self.password = None

        if reconnect:
            self.ssh_client = is_server_ssh_ready(
                self.__ssh_ip,
                self.ssh_port,
                self.username,
                self.password,
                retries=retries,
                retry_interval=retryinterv,
                timeout=timeout,
                key_pair_file_location=keyPairFileLocation
            )
        self.ssh_client = self.ssh_client or is_server_ssh_ready(
            self.__ssh_ip,
            self.ssh_port,
            self.username,
            self.password,
            retries=retries,
            retry_interval=retryinterv,
            timeout=timeout,
            key_pair_file_location=keyPairFileLocation
        )
        return self.ssh_client

    def test_ssh_connectivity(self, retries=2, expect_connection=True, retryinterv=None, timeout=None):

        got_connection = False

        try:
            self.get_ssh_client(reconnect=True, retries=retries, retryinterv=retryinterv, timeout=timeout)
            got_connection = True

        except Exception as e:
            if expect_connection:
                raise Exception("Exception: %s" % e)

        return expect_connection == got_connection

    def validateState(self, api_client, state, timeout=600, interval=5):
        """List VM and check if its state is as expected
        @returnValue - List[Result, Reason]
                       1) Result - FAIL if there is any exception
                       in the operation or VM state does not change
                       to expected state in given time else PASS
                       2) Reason - Reason for failure"""
        return validate_state(api_client, self, state, timeout, interval)

    @staticmethod
    def state_check_function(objects, state):
        return str(objects[0].state).lower() == str(state).lower()

    def resetSshKey(self, api_client, **kwargs):
        """Resets SSH key"""

        cmd = {'id': self.id}
        cmd.update(kwargs)
        return api_client.resetSSHKeyForVirtualMachine(**cmd)

    def update(self, api_client, **kwargs):
        """Updates the VM data"""

        cmd = {'id': self.id}
        cmd.update(kwargs)
        return api_client.updateVirtualMachine(**cmd)

    def delete(self, api_client, expunge=True, **kwargs):
        """Destroy an Instance"""
        cmd = {'id': self.id, 'expunge': expunge, 'fetch_result': True}
        cmd.update(kwargs)
        api_client.destroyVirtualMachine(**cmd)

    def expunge(self, api_client):
        """Expunge an Instance"""
        cmd = {'id': self.id}
        api_client.expungeVirtualMachine(**cmd)

    def migrate(self, api_client, hostid=None):
        """migrate an Instance"""
        cmd = {'virtualmachineid': self.id, 'fetch_result': True}
        if hostid:
            cmd['hostid'] = hostid
        return VirtualMachine(api_client.migrateVirtualMachine(**cmd).get('virtualmachine'))

    def migrate_vm_with_volume(self, api_client, hostid=None, migrateto=None):
        """migrate an Instance and its volumes"""
        cmd = {'virtualmachineid': self.id}
        if hostid:
            cmd['hostid'] = hostid
        if migrateto:
            migrateto = []
            for volume, pool in migrateto.items():
                cmd.migrateto.append({
                    'volume': volume,
                    'pool': pool
                })
        api_client.migrateVirtualMachineWithVolume(**cmd)

    def attach_volume(self, api_client, volume, deviceid=None):
        """Attach volume to instance"""
        cmd = {'id': volume.id, 'virtualmachineid': self.id}

        if deviceid is not None:
            cmd['deviceid'] = deviceid

        return api_client.attachVolume(**cmd)

    @staticmethod
    def detach_volume(api_client, volume):
        """Detach volume to instance"""
        cmd = {'id': volume.id}
        return api_client.detachVolume(**cmd)

    def add_nic(self, api_client, networkId, ipaddress=None):
        """Add a NIC to a VM"""
        cmd = {'virtualmachineid': self.id, 'networkid': networkId}

        if ipaddress:
            cmd['ipaddress'] = ipaddress

        return api_client.addNicToVirtualMachine(**cmd)

    def remove_nic(self, api_client, nicId):
        """Remove a NIC to a VM"""
        cmd = {'nicid': nicId, 'virtualmachineid': self.id}
        return api_client.removeNicFromVirtualMachine(**cmd)

    def update_default_nic(self, api_client, nicId):
        """Set a NIC to be the default network adapter for a VM"""
        cmd = {'nicid': nicId, 'virtualmachineid': self.id}
        return api_client.updateDefaultNicForVirtualMachine(**cmd)

    def attach_iso(self, api_client, iso):
        """Attach ISO to instance"""
        cmd = {'id': iso.id, 'virtualmachineid': self.id}
        return api_client.attachIso(**cmd)

    def detach_iso(self, api_client):
        """Detach ISO to instance"""
        cmd = {'virtualmachineid': self.id}
        return api_client.detachIso(**cmd)

    def scale_virtualmachine(self, api_client, serviceOfferingId):
        """ Scale up of service offering for the Instance"""
        cmd = {'id': self.id, 'serviceofferingid': serviceOfferingId}
        return api_client.scaleVirtualMachine(**cmd)

    def change_service_offering(self, api_client, serviceOfferingId):
        """Change service offering of the instance"""
        cmd = {'id': self.id, 'serviceofferingid': serviceOfferingId}
        return api_client.changeServiceForVirtualMachine(**cmd)

    @classmethod
    def list(cls, api_client, **kwargs):
        """List all VMs matching criteria"""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return super(VirtualMachine, cls).list(api_client.listVirtualMachines(**cmd).get('virtualmachine'), api_client=api_client)

    def resetPassword(self, api_client):
        """Resets VM password if VM created using password enabled template"""

        cmd = {'id': self.id}
        try:
            response = api_client.resetPasswordForVirtualMachine(**cmd)
        except Exception as e:
            raise Exception("Reset Password failed! - %s" % e)
        if response is not None:
            return response.password

    def assign_virtual_machine(self, api_client, account, domainid):
        """Move a user VM to another user under same domain."""

        cmd = {'virtualmachineid': self.id, 'account': account, 'domainid': domainid}
        try:
            response = api_client.assignVirtualMachine(**cmd)
            return response
        except Exception as e:
            raise Exception("assignVirtualMachine failed - %s" % e)

    def update_affinity_group(self, api_client, affinitygroupids=None,
                              affinitygroupnames=None):
        """Update affinity group of a VM"""
        cmd = {'id': self.id}

        if affinitygroupids:
            cmd['affinitygroupids'] = affinitygroupids

        if affinitygroupnames:
            cmd['affinitygroupnames'] = affinitygroupnames

        return api_client.updateVMAffinityGroup(**cmd)

    def scale(self, api_client, serviceOfferingId,
              customcpunumber=None, customcpuspeed=None, custommemory=None):
        """Change service offering of the instance"""
        cmd = {'id': self.id, 'serviceofferingid': serviceOfferingId, 'details': [{"cpuNumber": "", "cpuSpeed": "", "memory": ""}]}
        if customcpunumber:
            cmd['details[0]["cpuNumber"]'] = customcpunumber
        if customcpuspeed:
            cmd['details[0]["cpuSpeed"]'] = customcpuspeed
        if custommemory:
            cmd['details[0]["memory"]'] = custommemory
        return api_client.scaleVirtualMachine(**cmd)


class Volume:
    """Manage Volume Life cycle
    """

    def __init__(self, items):
        self.__dict__.update(items)

    @classmethod
    def create(cls, api_client, services, zoneid=None, account=None,
               domainid=None, diskofferingid=None, projectid=None, size=None):
        """Create Volume"""
        cmd = {'name': "-".join([services["diskname"], random_gen()])}

        if diskofferingid:
            cmd['diskofferingid'] = diskofferingid
        elif "diskofferingid" in services:
            cmd['diskofferingid'] = services["diskofferingid"]

        if zoneid:
            cmd['zoneid'] = zoneid
        elif "zoneid" in services:
            cmd['zoneid'] = services["zoneid"]

        if account:
            cmd['account'] = account
        elif "account" in services:
            cmd['account'] = services["account"]

        if domainid:
            cmd['domainid'] = domainid
        elif "domainid" in services:
            cmd['domainid'] = services["domainid"]

        if projectid:
            cmd['projectid'] = projectid

        if size:
            cmd['size'] = size

        return Volume(api_client.createVolume(**cmd))

    @classmethod
    def create_custom_disk(cls, api_client, services, account=None,
                           domainid=None, diskofferingid=None):
        """Create Volume from Custom disk offering"""
        cmd = {'name': services["diskname"]}

        if diskofferingid:
            cmd['diskofferingid'] = diskofferingid
        elif "customdiskofferingid" in services:
            cmd['diskofferingid'] = services["customdiskofferingid"]

        cmd['size'] = services["customdisksize"]
        cmd['zoneid'] = services["zoneid"]

        if account:
            cmd['account'] = account
        else:
            cmd['account'] = services["account"]

        if domainid:
            cmd['domainid'] = domainid
        else:
            cmd['domainid'] = services["domainid"]

        return Volume(api_client.createVolume(**cmd))

    @classmethod
    def create_from_snapshot(cls, api_client, snapshot_id, services,
                             account=None, domainid=None):
        """Create Volume from snapshot"""
        cmd = {'name': "-".join([services["diskname"], random_gen()]), 'snapshotid': snapshot_id, 'zoneid': services["zoneid"], 'size': services["size"]}
        if services["ispublic"]:
            cmd['ispublic'] = services["ispublic"]
        else:
            cmd['ispublic'] = False
        if account:
            cmd['account'] = account
        else:
            cmd['account'] = services["account"]
        if domainid:
            cmd['domainid'] = domainid
        else:
            cmd['domainid'] = services["domainid"]
        return Volume(api_client.createVolume(**cmd))

    def delete(self, api_client):
        """Delete Volume"""
        cmd = {'id': self.id}
        api_client.deleteVolume(**cmd)

    @classmethod
    def list(cls, api_client, **kwargs):
        """List all volumes matching criteria"""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return api_client.listVolumes(**cmd)

    def resize(self, api_client, **kwargs):
        """Resize a volume"""
        cmd = {'id': self.id}
        cmd.update(kwargs)
        return api_client.resizeVolume(**cmd)

    @classmethod
    def upload(cls, api_client, services, zoneid=None,
               account=None, domainid=None, url=None, **kwargs):
        """Uploads the volume to specified account"""

        cmd = {}
        if zoneid:
            cmd['zoneid'] = zoneid
        if account:
            cmd['account'] = account
        if domainid:
            cmd['domainid'] = domainid
        cmd['format'] = services["format"]
        cmd['name'] = services["diskname"]
        if url:
            cmd['url'] = url
        else:
            cmd['url'] = services["url"]
        cmd.update(kwargs)
        return Volume(api_client.uploadVolume(**cmd))

    def wait_for_upload(self, api_client, timeout=10, interval=60):
        """Wait for upload"""
        # Sleep to ensure template is in proper state before download
        time.sleep(interval)

        while True:
            volume_response = Volume.list(
                api_client,
                id=self.id,
                zoneid=self.zoneid,
            )
            if isinstance(volume_response, list):

                volume = volume_response[0]
                # If volume is ready,
                # volume.state = Allocated
                if volume.state == 'Uploaded':
                    break

                elif 'Uploading' in volume.state:
                    time.sleep(interval)

                elif 'Installing' not in volume.state:
                    raise Exception(
                        "Error in uploading volume: status - %s" %
                        volume.state)
            elif timeout == 0:
                break

            else:
                time.sleep(interval)
                timeout = timeout - interval
        return

    @classmethod
    def extract(cls, api_client, volume_id, zoneid, mode):
        """Extracts the volume"""

        cmd = {'id': volume_id, 'zoneid': zoneid, 'mode': mode}
        return Volume(api_client.extractVolume(**cmd))

    @classmethod
    def migrate(cls, api_client, **kwargs):
        """Migrate a volume"""
        cmd = {}
        cmd.update(kwargs)
        return api_client.migrateVolume(**cmd)


class Snapshot:
    """Manage Snapshot Lifecycle
    """
    '''Class level variables'''
    # Variables denoting possible Snapshot states - start
    BACKED_UP = BACKED_UP
    BACKING_UP = BACKING_UP

    # Variables denoting possible Snapshot states - end

    def __init__(self, items):
        self.__dict__.update(items)

    @classmethod
    def create(cls, api_client, volume_id, account=None,
               domainid=None, projectid=None):
        """Create Snapshot"""
        cmd = {'volumeid': volume_id}
        if account:
            cmd['account'] = account
        if domainid:
            cmd['domainid'] = domainid
        if projectid:
            cmd['projectid'] = projectid
        return Snapshot(api_client.createSnapshot(**cmd))

    def delete(self, api_client):
        """Delete Snapshot"""
        cmd = {'id': self.id}
        api_client.deleteSnapshot(**cmd)

    @classmethod
    def list(cls, api_client, **kwargs):
        """List all snapshots matching criteria"""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return api_client.listSnapshots(**cmd)

    def validateState(self, api_client, state, timeout=600, interval=5):
        """Check if snapshot is in required state
           returnValue: List[Result, Reason]
                 @Result: PASS if snapshot is in required state,
                          else FAIL
                 @Reason: Reason for failure in case Result is FAIL
        """
        return validate_state(api_client, self, state, timeout, interval)

    @staticmethod
    def state_check_function(objects, state):
        return str(objects[0].state).lower().decode("string_escape") == str(state).lower()


class Template:
    """Manage template life cycle"""

    def __init__(self, items):
        self.__dict__.update(items)

    @classmethod
    def create(cls, api_client, services, volumeid=None,
               account=None, domainid=None, projectid=None):
        """Create template from Volume"""
        # Create template from Virtual machine and Volume ID
        cmd = {'displaytext': services["displaytext"], 'name': "-".join([services["name"], random_gen()])}
        if "ostypeid" in services:
            cmd['ostypeid'] = services["ostypeid"]
        elif "ostype" in services:
            # Find OSTypeId from Os type
            sub_cmd = {'description': services["ostype"]}
            ostypes = api_client.listOsTypes(**sub_cmd)

            if not isinstance(ostypes, list):
                raise Exception(
                    "Unable to find Ostype id with desc: %s" %
                    services["ostype"])
            cmd['ostypeid'] = ostypes[0].id
        else:
            raise Exception(
                "Unable to find Ostype is required for creating template")

        cmd['isfeatured'] = services[
            "isfeatured"] if "isfeatured" in services else False
        cmd['ispublic'] = services[
            "ispublic"] if "ispublic" in services else False
        cmd['isextractable'] = services[
            "isextractable"] if "isextractable" in services else False
        cmd['passwordenabled'] = services[
            "passwordenabled"] if "passwordenabled" in services else False

        if volumeid:
            cmd['volumeid'] = volumeid

        if account:
            cmd['account'] = account

        if domainid:
            cmd['domainid'] = domainid

        if projectid:
            cmd['projectid'] = projectid
        return Template(api_client.createTemplate(**cmd))

    @classmethod
    def register(cls, api_client, services, zoneid=None,
                 account=None, domainid=None, hypervisor=None,
                 projectid=None, details=None):
        """Create template from URL"""

        # Create template from Virtual machine and Volume ID
        cmd = {'displaytext': services["displaytext"], 'name': "-".join([services["name"], random_gen()]), 'format': services["format"]}
        if hypervisor:
            cmd['hypervisor'] = hypervisor
        elif "hypervisor" in services:
            cmd['hypervisor'] = services["hypervisor"]

        if "ostypeid" in services:
            cmd['ostypeid'] = services["ostypeid"]
        elif "ostype" in services:
            # Find OSTypeId from Os type
            sub_cmd = {'description': services["ostype"]}
            ostypes = api_client.listOsTypes(**sub_cmd)

            if not isinstance(ostypes, list):
                raise Exception(
                    "Unable to find Ostype id with desc: %s" %
                    services["ostype"])
            cmd['ostypeid'] = ostypes[0].id
        else:
            raise Exception(
                "Unable to find Ostype is required for registering template")

        cmd['url'] = services["url"]

        if zoneid:
            cmd['zoneid'] = zoneid
        else:
            cmd['zoneid'] = services["zoneid"]

        cmd['isfeatured'] = services[
            "isfeatured"] if "isfeatured" in services else False
        cmd['ispublic'] = services[
            "ispublic"] if "ispublic" in services else False
        cmd['isextractable'] = services[
            "isextractable"] if "isextractable" in services else False
        cmd['passwordenabled'] = services[
            "passwordenabled"] if "passwordenabled" in services else False

        if account:
            cmd['account'] = account

        if domainid:
            cmd['domainid'] = domainid

        if projectid:
            cmd['projectid'] = projectid
        elif "projectid" in services:
            cmd['projectid'] = services["projectid"]

        if details:
            cmd['details'] = details

        # Register Template
        template = api_client.registerTemplate(**cmd)

        if isinstance(template, list):
            return Template(template[0].__dict__)

    @classmethod
    def extract(cls, api_client, id, mode, zoneid=None):
        """Extract template """

        cmd = {'id': id, 'mode': mode, 'zoneid': zoneid}

        return api_client.extractTemplate(**cmd)

    @classmethod
    def create_from_snapshot(cls, api_client, snapshot, services,
                             random_name=True):
        """Create Template from snapshot"""
        # Create template from Virtual machine and Snapshot ID
        cmd = {'displaytext': services["displaytext"], 'name': "-".join([
            services["name"],
            random_gen()
        ]) if random_name else services["name"]}

        if "ispublic" in services:
            cmd['ispublic'] = services["ispublic"]

        if "ostypeid" in services:
            cmd['ostypeid'] = services["ostypeid"]
        elif "ostype" in services:
            # Find OSTypeId from Os type
            sub_cmd = {'description': services["ostype"]}
            ostypes = api_client.listOsTypes(**sub_cmd)

            if not isinstance(ostypes, list):
                raise Exception(
                    "Unable to find Ostype id with desc: %s" %
                    services["ostype"])
            cmd['ostypeid'] = ostypes[0].id
        else:
            raise Exception(
                "Unable to find Ostype is required for creating template")

        cmd['snapshotid'] = snapshot.id
        return Template(api_client.createTemplate(**cmd))

    def delete(self, api_client, zoneid=None):
        """Delete Template"""

        cmd = {'id': self.id}
        if zoneid:
            cmd['zoneid'] = zoneid
        api_client.deleteTemplate(**cmd)

    def download(self, api_client, timeout=300, interval=60):
        """Download Template"""
        # Sleep to ensure template is in proper state before download
        time.sleep(interval)

        while True:
            template_response = Template.list(
                api_client,
                id=self.id,
                zoneid=self.zoneid,
                templatefilter='self'
            )
            if isinstance(template_response, list):

                template = template_response[0]
                if not template.isready:
                    continue

                if template.status == 'Download Complete':
                    break

                elif 'Downloaded' in template.status:
                    time.sleep(interval)

                elif not template.status.strip():  # status is empty string
                    time.sleep(interval)

                elif 'Installing' not in template.status:
                    raise Exception("Error in downloading template: status - %s" % template.status)

            elif timeout == 0:
                break

            else:
                time.sleep(interval)
                timeout -= interval
        return

    def updatePermissions(self, api_client, **kwargs):
        """Updates the template permissions"""

        cmd = {'id': self.id}
        cmd.update(kwargs)
        return api_client.updateTemplatePermissions(**cmd)

    def update(self, api_client, **kwargs):
        """Updates the template details"""

        cmd = {'id': self.id}
        cmd.update(kwargs)
        return api_client.updateTemplate(**cmd)

    def copy(self, api_client, sourcezoneid, destzoneid):
        """Copy Template from source Zone to Destination Zone"""

        cmd = {'id': self.id, 'sourcezoneid': sourcezoneid, 'destzoneid': destzoneid}

        return api_client.copyTemplate(**cmd)

    @classmethod
    def list(cls, api_client, **kwargs):
        """List all templates matching criteria"""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return api_client.listTemplates(**cmd)


class Iso:
    """Manage ISO life cycle"""

    def __init__(self, items):
        self.__dict__.update(items)

    @classmethod
    def create(cls, api_client, services, account=None, domainid=None,
               projectid=None, zoneid=None):
        """Create an ISO"""
        # Create ISO from URL
        cmd = {'displaytext': services["displaytext"], 'name': "-".join([services["name"], random_gen()])}
        if "ostypeid" in services:
            cmd['ostypeid'] = services["ostypeid"]
        elif "ostype" in services:
            # Find OSTypeId from Os type
            sub_cmd = {'description': services["ostype"]}
            ostypes = api_client.listOsTypes(**sub_cmd)

            if not isinstance(ostypes, list):
                raise Exception(
                    "Unable to find Ostype id with desc: %s" %
                    services["ostype"])
            cmd['ostypeid'] = ostypes[0].id
        else:
            raise Exception(
                "Unable to find Ostype is required for creating ISO")

        cmd['url'] = services["url"]

        if zoneid:
            cmd['zoneid'] = zoneid
        else:
            cmd['zoneid'] = services["zoneid"]

        if "isextractable" in services:
            cmd['isextractable'] = services["isextractable"]
        if "isfeatured" in services:
            cmd['isfeatured'] = services["isfeatured"]
        if "ispublic" in services:
            cmd['ispublic'] = services["ispublic"]

        if account:
            cmd['account'] = account
        if domainid:
            cmd['domainid'] = domainid
        if projectid:
            cmd['projectid'] = projectid
        # Register ISO
        iso = api_client.registerIso(**cmd)

        if iso:
            return Iso(iso[0].__dict__)

    def delete(self, api_client):
        """Delete an ISO"""
        cmd = {'id': self.id}
        api_client.deleteIso(**cmd)
        return

    def download(self, api_client, timeout=5, interval=60):
        """Download an ISO"""
        # Ensuring ISO is successfully downloaded
        retry = 1
        while True:
            time.sleep(interval)

            cmd = {'id': self.id}
            iso_response = api_client.listIsos(**cmd)

            if isinstance(iso_response, list):
                response = iso_response[0]
                # Again initialize timeout to avoid listISO failure
                timeout = 5
                # Check whether download is in progress(for Ex:10% Downloaded)
                # or ISO is 'Successfully Installed'
                if response.status == 'Successfully Installed':
                    return
                elif 'Downloaded' not in response.status and 'Installing' not in response.status:
                    if retry == 1:
                        retry = retry - 1
                        continue
                    raise Exception(
                        "Error In Downloading ISO: ISO Status - %s" %
                        response.status)

            elif timeout == 0:
                raise Exception("ISO download Timeout Exception")
            else:
                timeout = timeout - 1

    @classmethod
    def extract(cls, api_client, id, mode, zoneid=None):
        """Extract ISO """

        cmd = {'id': id, 'mode': mode, 'zoneid': zoneid}

        return api_client.extractIso(**cmd)

    def update(self, api_client, **kwargs):
        """Updates the ISO details"""

        cmd = {'id': self.id}
        cmd.update(kwargs)
        return api_client.updateIso(**cmd)

    @classmethod
    def copy(cls, api_client, id, sourcezoneid, destzoneid):
        """Copy ISO from source Zone to Destination Zone"""

        cmd = {'id': id, 'sourcezoneid': sourcezoneid, 'destzoneid': destzoneid}

        return api_client.copyIso(**cmd)

    @classmethod
    def list(cls, api_client, **kwargs):
        """Lists all available ISO files."""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return api_client.listIsos(**cmd)


class PublicIPAddress(BaseAbstract):
    """Manage Public IP Addresses"""

    def __init__(self, items, **kwargs):
        super(PublicIPAddress, self).__init__(items, **kwargs)

    @classmethod
    def create(cls, api_client, accountid=None, zoneid=None, domainid=None,
               services=None, networkid=None, projectid=None, vpcid=None,
               vpc=None, data=None, network=None):
        """Associate Public IP address"""
        if data:
            services = data
        cmd = {}

        if accountid:
            cmd['account'] = accountid
        elif services and "account" in services:
            cmd['account'] = services["account"]

        if zoneid:
            cmd['zoneid'] = zoneid
        elif "zoneid" in services:
            cmd['zoneid'] = services["zoneid"]

        if domainid:
            cmd['domainid'] = domainid
        elif services and "domainid" in services:
            cmd['domainid'] = services["domainid"]

        if networkid:
            cmd['networkid'] = networkid
        elif network:
            cmd['networkid'] = network.id

        if projectid:
            cmd['projectid'] = projectid

        if vpcid:
            cmd['vpcid'] = vpcid
        elif vpc:
            cmd['vpcid'] = vpc.id

        pubip_response = PublicIPAddress(api_client.associateIpAddress(**cmd))
        waitforjob(api_client, pubip_response.jobid)
        return cls.list(api_client, id=pubip_response.id)[0]

    def delete(self, api_client):
        """Dissociate Public IP address"""
        cmd = {'id': self.id}
        api_client.disassociateIpAddress(**cmd)
        return

    @classmethod
    def list(cls, api_client, **kwargs):
        """List all Public IPs matching criteria"""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return super(PublicIPAddress, cls).list(api_client.listPublicIpAddresses(**cmd)['publicipaddress'], api_client=api_client)


class NATRule(BaseAbstract):
    """Manage port forwarding rule"""

    def __init__(self, items):
        super(NATRule, self).__init__(items)

    @classmethod
    def create(cls, api_client, virtual_machine, services=None, ipaddressid=None, projectid=None, openfirewall=False,
               networkid=None, network=None, vpcid=None, vpc=None, vmguestip=None, ipaddress=None, data=None):
        """Create Port forwarding rule"""
        if data:
            services = data

        cmd = {}

        if ipaddressid:
            cmd['ipaddressid'] = ipaddressid
        elif "ipaddressid" in services:
            cmd['ipaddressid'] = services["ipaddressid"]
        elif ipaddress:
            cmd['ipaddressid'] = ipaddress.ipaddress.id

        cmd['privateport'] = services["privateport"]
        cmd['publicport'] = services["publicport"]
        if "privateendport" in services:
            cmd['privateendport'] = services["privateendport"]
        if "publicendport" in services:
            cmd['publicendport'] = services["publicendport"]
        cmd['protocol'] = services["protocol"]
        cmd['virtualmachineid'] = virtual_machine.id

        if projectid:
            cmd['projectid'] = projectid

        if 'openfirewall' in services:
            cmd['openfirewall'] = services['openfirewall']

        if openfirewall:
            # FIXME: it should be `cmd['openfirewall'] = openfirewall`, not changed for backwards compatibility
            cmd['openfirewall'] = True

        if networkid:
            cmd['networkid'] = networkid
        elif network:
            cmd['networkid'] = network.id

        if vpcid:
            cmd['vpcid'] = vpcid
        elif vpc:
            cmd['vpcid'] = vpc.id

        if vmguestip:
            cmd['vmguestip'] = vmguestip
        natrule_response = NATRule(api_client.createPortForwardingRule(**cmd))
        waitforjob(api_client, natrule_response.jobid)
        return cls.list(api_client, id=natrule_response.id)[0]

    def delete(self, api_client):
        """Delete port forwarding"""
        cmd = {'id': self.id}
        api_client.deletePortForwardingRule(**cmd)
        return

    @classmethod
    def list(cls, api_client, **kwargs):
        """List all NAT rules matching criteria"""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return super(NATRule, cls).list(api_client.listPortForwardingRules(**cmd)['portforwardingrule'])


class StaticNATRule:
    """Manage Static NAT rule"""

    def __init__(self, items):
        self.__dict__.update(items)

    @classmethod
    def create(cls, api_client, services, ipaddressid=None,
               networkid=None, vpcid=None):
        """Creates static ip forwarding rule"""

        cmd = {'protocol': services["protocol"], 'startport': services["startport"]}

        if "endport" in services:
            cmd['endport'] = services["endport"]

        if "cidrlist" in services:
            cmd['cidrlist'] = services["cidrlist"]

        if ipaddressid:
            cmd['ipaddressid'] = ipaddressid
        elif "ipaddressid" in services:
            cmd['ipaddressid'] = services["ipaddressid"]

        if networkid:
            cmd['networkid'] = networkid

        if vpcid:
            cmd['vpcid'] = vpcid
        return StaticNATRule(api_client.createFirewallRule(**cmd))

    @classmethod
    def createIpForwardingRule(cls, api_client, startport, endport, protocol, ipaddressid, openfirewall):
        """Creates static ip forwarding rule"""

        cmd = {'startport': startport, 'endport': endport, 'protocol': protocol, 'openfirewall': openfirewall, 'ipaddressid': ipaddressid}
        return StaticNATRule(api_client.createIpForwardingRule(**cmd))

    def delete(self, api_client):
        """Delete IP forwarding rule"""
        cmd = {'id': self.id}
        api_client.deleteIpForwardingRule(**cmd)
        return

    @classmethod
    def list(cls, api_client, **kwargs):
        """List all IP forwarding rules matching criteria"""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return api_client.listIpForwardingRules(**cmd)

    @classmethod
    def enable(cls, api_client, ipaddressid, virtualmachineid, networkid=None,
               vmguestip=None):
        """Enables Static NAT rule"""

        cmd = {'ipaddressid': ipaddressid, 'virtualmachineid': virtualmachineid}
        if networkid:
            cmd['networkid'] = networkid

        if vmguestip:
            cmd['vmguestip'] = vmguestip
        api_client.enableStaticNat(**cmd)
        return

    @classmethod
    def disable(cls, api_client, ipaddressid):
        """Disables Static NAT rule"""

        cmd = {'ipaddressid': ipaddressid}
        api_client.disableStaticNat(**cmd)
        return


class EgressFireWallRule:
    """Manage Egress Firewall rule"""

    def __init__(self, items):
        self.__dict__.update(items)

    @classmethod
    def create(cls, api_client, networkid=None, protocol=None, cidrlist=None,
               startport=None, endport=None, type=None, code=None, network=None, data=None):
        """Create Egress Firewall Rule"""
        cmd = {}
        if networkid:
            cmd['networkid'] = networkid
        elif network:
            cmd['networkid'] = network.id
        if protocol:
            cmd['protocol'] = protocol
        elif 'protocol' in data:
            cmd['protocol'] = data['protocol']
        if cidrlist:
            cmd['cidrlist'] = cidrlist
        elif 'cidrlist' in data:
            cmd['cidrlist'] = data['cidrlist']
        if startport:
            cmd['startport'] = startport
        elif 'startport' in data:
            cmd['startport'] = data['startport']
        if endport:
            cmd['endport'] = endport
        elif 'endport' in data:
            cmd['endport'] = data['endport']
        if type:
            cmd['type'] = type
        if code:
            cmd['code'] = code

        return EgressFireWallRule(
            api_client.createEgressFirewallRule(**cmd))

    def delete(self, api_client):
        """Delete Egress Firewall rule"""
        cmd = {'id': self.id}
        api_client.deleteEgressFirewallRule(**cmd)
        return

    @classmethod
    def list(cls, api_client, **kwargs):
        """List all Egress Firewall Rules matching criteria"""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return api_client.listEgressFirewallRules(**cmd)


class FireWallRule:
    """Manage Firewall rule"""

    def __init__(self, items):
        self.__dict__.update(items)

    @classmethod
    def create(cls, api_client, ipaddressid=None, protocol=None, cidrlist=None,
               startport=None, endport=None, projectid=None, vpcid=None, data=None, ipaddress=None):
        """Create Firewall Rule"""
        cmd = {}
        if ipaddressid:
            cmd['ipaddressid'] = ipaddressid
        elif ipaddress:
            cmd['ipaddressid'] = ipaddress.ipaddress.id
        if protocol:
            cmd['protocol'] = protocol
        elif 'protocol' in data:
            cmd['protocol'] = data['protocol']
        if cidrlist:
            cmd['cidrlist'] = cidrlist
        elif 'cidrlist' in data:
            cmd['cidrlist'] = data['cidrlist']
        if startport:
            cmd['startport'] = startport
        elif 'startport' in data:
            cmd['startport'] = data['startport']
        if endport:
            cmd['endport'] = endport
        elif 'endport' in data:
            cmd['endport'] = data['endport']

        if projectid:
            cmd['projectid'] = projectid

        if vpcid:
            cmd['vpcid'] = vpcid

        return FireWallRule(api_client.createFirewallRule(**cmd))

    def delete(self, api_client):
        """Delete Firewall rule"""
        cmd = {'id': self.id}
        api_client.deleteFirewallRule(**cmd)
        return

    @classmethod
    def list(cls, api_client, **kwargs):
        """List all Firewall Rules matching criteria"""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return api_client.listFirewallRules(**cmd)


class ServiceOffering:
    """Manage service offerings cycle"""

    def __init__(self, items):
        self.__dict__.update(items)

    @classmethod
    def create(cls, api_client, services, tags=None, domainid=None, **kwargs):
        """Create Service offering"""
        cmd = {'cpunumber': services["cpunumber"], 'cpuspeed': services["cpuspeed"], 'displaytext': services["displaytext"], 'memory': services["memory"], 'name': services["name"]}
        if "storagetype" in services:
            cmd['storagetype'] = services["storagetype"]

        if "systemvmtype" in services:
            cmd['systemvmtype'] = services['systemvmtype']

        if "issystem" in services:
            cmd['issystem'] = services['issystem']

        if "hosttags" in services:
            cmd['hosttags'] = services["hosttags"]

        if "deploymentplanner" in services:
            cmd['deploymentplanner'] = services["deploymentplanner"]

        if "serviceofferingdetails" in services:
            count = 1
            for i in services["serviceofferingdetails"]:
                for key, value in i.items():
                    setattr(cmd, "serviceofferingdetails[%d].key" % count, key)
                    setattr(cmd, "serviceofferingdetails[%d].value" % count, value)
                count = count + 1

        if "isvolatile" in services:
            cmd['isvolatile'] = services["isvolatile"]

        if "customizediops" in services:
            cmd['customizediops'] = services["customizediops"]

        if "miniops" in services:
            cmd['miniops'] = services["miniops"]

        if "maxiops" in services:
            cmd['maxiops'] = services["maxiops"]

        if "hypervisorsnapshotreserve" in services:
            cmd['hypervisorsnapshotreserve'] = services["hypervisorsnapshotreserve"]

        if "offerha" in services:
            cmd['offerha'] = services["offerha"]

        # Service Offering private to that domain
        if domainid:
            cmd['domainid'] = domainid

        if tags:
            cmd['tags'] = tags
        elif "tags" in services:
            cmd['tags'] = services["tags"]

        cmd.update(kwargs)
        return ServiceOffering(api_client.createServiceOffering(**cmd))

    def delete(self, api_client):
        """Delete Service offering"""
        cmd = {'id': self.id}
        api_client.deleteServiceOffering(**cmd)
        return

    @classmethod
    def list(cls, api_client, **kwargs):
        """Lists all available service offerings."""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return api_client.listServiceOfferings(**cmd)


class DiskOffering(BaseAbstract):
    """Manage disk offerings cycle"""

    def __init__(self, items):
        super(DiskOffering, self).__init__(items)

    @classmethod
    def create(cls, api_client, services, tags=None, custom=False, domainid=None):
        """Create Disk offering"""
        cmd = {'displaytext': services["displaytext"], 'name': services["name"]}
        if custom:
            cmd['customized'] = True
        else:
            cmd['disksize'] = services["disksize"]

        if domainid:
            cmd['domainid'] = domainid

        if tags:
            cmd['tags'] = tags
        elif "tags" in services:
            cmd['tags'] = services["tags"]

        if "storagetype" in services:
            cmd['storagetype'] = services["storagetype"]

        if "customizediops" in services:
            cmd['customizediops'] = services["customizediops"]

        if "maxiops" in services:
            cmd['maxiops'] = services["maxiops"]

        if "miniops" in services:
            cmd['miniops'] = services["miniops"]

        if "hypervisorsnapshotreserve" in services:
            cmd['hypervisorsnapshotreserve'] = services["hypervisorsnapshotreserve"]

        if "provisioningtype" in services:
            cmd['provisioningtype'] = services["provisioningtype"]

        return DiskOffering(api_client.createDiskOffering(**cmd).get('diskoffering'))

    def delete(self, api_client):
        """Delete Disk offering"""
        cmd = {'id': self.id}
        api_client.deleteDiskOffering(**cmd)
        return

    @classmethod
    def list(cls, api_client, **kwargs):
        """Lists all available disk offerings."""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return super(DiskOffering, cls).list(api_client.listDiskOfferings(**cmd).get('diskoffering'))


class NetworkOffering:
    """Manage network offerings cycle"""

    def __init__(self, items):
        self.__dict__.update(items)

    @classmethod
    def create(cls, api_client, services, **kwargs):
        """Create network offering"""

        cmd = {'displaytext': "-".join([services["displaytext"], random_gen()]), 'name': "-".join([services["name"], random_gen()]), 'guestiptype': services["guestiptype"],
               'supportedservices': ''}
        if "supportedservices" in services:
            cmd['supportedservices'] = services["supportedservices"]
        cmd['traffictype'] = services["traffictype"]

        if "useVpc" in services:
            cmd['useVpc'] = services["useVpc"]
        cmd['serviceproviderlist'] = []
        if "serviceProviderList" in services:
            for service, provider in services["serviceProviderList"].items():
                cmd.serviceproviderlist.append({
                    'service': service,
                    'provider': provider
                })
        if "serviceCapabilityList" in services:
            cmd['servicecapabilitylist'] = []
            for service, capability in services["serviceCapabilityList"]. \
                    items():
                for ctype, value in capability.items():
                    cmd.servicecapabilitylist.append({
                        'service': service,
                        'capabilitytype': ctype,
                        'capabilityvalue': value
                    })
        if "specifyVlan" in services:
            cmd['specifyVlan'] = services["specifyVlan"]
        if "specifyIpRanges" in services:
            cmd['specifyIpRanges'] = services["specifyIpRanges"]
        if "ispersistent" in services:
            cmd['ispersistent'] = services["ispersistent"]
        if "egress_policy" in services:
            cmd['egressdefaultpolicy'] = services["egress_policy"]

        cmd['availability'] = 'Optional'

        cmd.update(kwargs)

        return NetworkOffering(api_client.createNetworkOffering(**cmd))

    def delete(self, api_client):
        """Delete network offering"""
        cmd = {'id': self.id}
        api_client.deleteNetworkOffering(**cmd)
        return

    def update(self, api_client, **kwargs):
        """Lists all available network offerings."""

        cmd = {'id': self.id}
        cmd.update(kwargs)
        return api_client.updateNetworkOffering(**cmd)

    @classmethod
    def list(cls, api_client, **kwargs):
        """Lists all available network offerings."""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return api_client.listNetworkOfferings(**cmd)


class Hypervisor:
    """Manage Hypervisor"""

    def __init__(self, items):
        self.__dict__.update(items)

    @classmethod
    def list(cls, api_client, **kwargs):
        """Lists hypervisors"""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return api_client.listHypervisors(**cmd)


class LoadBalancerRule(BaseAbstract):
    """Manage Load Balancer rule"""

    def __init__(self, items):
        super(LoadBalancerRule, self).__init__(items)

    @classmethod
    def create(cls, api_client, services, ipaddressid=None, accountid=None,
               networkid=None, vpcid=None, projectid=None, domainid=None):
        """Create Load balancing Rule"""

        cmd = {}

        if ipaddressid:
            cmd['publicipid'] = ipaddressid
        elif "ipaddressid" in services:
            cmd['publicipid'] = services["ipaddressid"]

        if accountid:
            cmd['account'] = accountid
        elif "account" in services:
            cmd['account'] = services["account"]

        if domainid:
            cmd['domainid'] = domainid

        if vpcid:
            cmd['vpcid'] = vpcid
        cmd['name'] = services["name"]
        cmd['algorithm'] = services["alg"]
        cmd['privateport'] = services["privateport"]
        cmd['publicport'] = services["publicport"]

        if "openfirewall" in services:
            cmd['openfirewall'] = services["openfirewall"]

        if projectid:
            cmd['projectid'] = projectid

        if networkid:
            cmd['networkid'] = networkid
        lb_resp = LoadBalancerRule(api_client.createLoadBalancerRule(**cmd))
        waitforjob(api_client, lb_resp.jobid)
        return cls.list(api_client, id=lb_resp.id)[0]

    def delete(self, api_client):
        """Delete load balancing rule"""
        cmd = {'id': self.id}
        api_client.deleteLoadBalancerRule(**cmd)
        return

    def assign(self, api_client, vms=None, vmidipmap=None):
        """Assign virtual machines to load balancing rule"""
        cmd = {'id': self.id, 'fetch_result': True}
        if vmidipmap:
            cmd['vmidipmap'] = vmidipmap
        if vms:
            cmd['virtualmachineids'] = [str(vm.id) for vm in vms]
        _results = api_client.assignToLoadBalancerRule(**cmd)
        return

    def remove(self, api_client, vms=None, vmidipmap=None):
        """Remove virtual machines from load balancing rule"""
        cmd = {'id': self.id, 'fetch_result': True}
        if vms:
            cmd['virtualmachineids'] = [str(vm.id) for vm in vms]
        if vmidipmap:
            cmd['vmidipmap'] = vmidipmap
        _results = api_client.removeFromLoadBalancerRule(**cmd)
        return

    def update(self, api_client, algorithm=None,
               description=None, name=None, **kwargs):
        """Updates the load balancing rule"""
        cmd = {'id': self.id}
        if algorithm:
            cmd['algorithm'] = algorithm
        if description:
            cmd['description'] = description
        if name:
            cmd['name'] = name

        cmd.update(kwargs)
        return api_client.updateLoadBalancerRule(**cmd)

    def createSticky(
            self, api_client, methodname, name, description=None, param=None):
        """Creates a sticky policy for the LB rule"""

        cmd = {'lbruleid': self.id, 'methodname': methodname, 'name': name}
        if description:
            cmd['description'] = description
        if param:
            cmd['param'] = []
            for name, value in param.items():
                cmd.param.append({'name': name, 'value': value})
        return api_client.createLBStickinessPolicy(**cmd)

    @staticmethod
    def deleteSticky(api_client, id):
        """Deletes stickyness policy"""

        cmd = {'id': id}
        return api_client.deleteLBStickinessPolicy(**cmd)

    @classmethod
    def listStickyPolicies(cls, api_client, lbruleid, **kwargs):
        """Lists stickiness policies for load balancing rule"""

        cmd = {'lbruleid': lbruleid}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return api_client.listLBStickinessPolicies(**cmd)

    @classmethod
    def list(cls, api_client, **kwargs):
        """List all Load balancing rules matching criteria"""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return super(LoadBalancerRule, cls).list(api_client.listLoadBalancerRules(**cmd).get('loadbalancerrule', []))

    @classmethod
    def listLoadBalancerRuleInstances(cls, api_client, id, lbvmips=False, applied=None, **kwargs):
        """Lists load balancing rule Instances"""

        cmd = {'id': id}
        if applied:
            cmd['applied'] = applied
        cmd['lbvmips'] = lbvmips

        cmd.update(kwargs)
        return api_client.listLoadBalancerRuleInstances(**cmd)


class Cluster(BaseAbstract):
    """Manage Cluster life cycle"""

    def __init__(self, items):
        super(Cluster, self).__init__(items)

    @classmethod
    def create(cls, api_client, services, zoneid=None, podid=None, hypervisor=None):
        """Create Cluster"""
        cmd = {'clustertype': services["clustertype"], 'hypervisor': hypervisor}

        if zoneid:
            cmd['zoneid'] = zoneid
        else:
            cmd['zoneid'] = services["zoneid"]

        if podid:
            cmd['podid'] = podid
        else:
            cmd['podid'] = services["podid"]

        if "username" in services:
            cmd['username'] = services["username"]
        if "password" in services:
            cmd['password'] = services["password"]
        if "url" in services:
            cmd['url'] = services["url"]
        if "clustername" in services:
            cmd['clustername'] = services["clustername"]

        cluster_resp = Cluster(api_client.addCluster(**cmd).get('cluster'))
        return cluster_resp[0] if cluster_resp else None

    def delete(self, api_client):
        """Delete Cluster"""
        cmd = {'id': self.id}
        api_client.deleteCluster(**cmd)
        return

    @classmethod
    def list(cls, api_client, **kwargs):
        """List all Clusters matching criteria"""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return super(Cluster, cls).list(api_client.listClusters(**cmd).get('cluster'))

    @classmethod
    def update(cls, api_client, **kwargs):
        """Update cluster information"""

        cmd = {}
        cmd.update(kwargs)
        return api_client.updateCluster(**cmd)


class Host(BaseAbstract):
    """Manage Host life cycle"""

    def __init__(self, items):
        super(Host, self).__init__(items)

    @classmethod
    def create(cls, api_client, cluster, services, zoneid=None, podid=None, hypervisor=None):
        """
        1. Creates the host based upon the information provided.
        2. Verifies the output of the adding host and its state post addition
           Returns FAILED in case of an issue, else an instance of Host
        """
        try:
            cmd = {'hypervisor': hypervisor, 'url': services["url"], 'clusterid': cluster.id}

            if zoneid:
                cmd['zoneid'] = zoneid
            else:
                cmd['zoneid'] = services["zoneid"]

            if podid:
                cmd['podid'] = podid
            else:
                cmd['podid'] = services["podid"]

            if "clustertype" in services:
                cmd['clustertype'] = services["clustertype"]
            if "username" in services:
                cmd['username'] = services["username"]
            if "password" in services:
                cmd['password'] = services["password"]

            '''
            Adds a Host,
            If response is valid and host is up return
            an instance of Host.
            If response is invalid, returns FAILED.
            If host state is not up, verify through listHosts call
            till host status is up and return accordingly. Max 3 retries
            '''
            host = Host(api_client.addHost(**cmd))
            # TODO: check if "hosts" is the correct key
            ret = validate_list(host, "hosts")
            if ret[0] == PASS:
                if str(host[0].state).lower() == 'up':
                    return host[0]
                retries = 3
                while retries:
                    lh_resp = Host(api_client.listHosts(host[0].id))
                    #  TODO: check if "hosts" is the correct key
                    ret = validate_list(lh_resp, "hosts")
                    if (ret[0] == PASS) and \
                            (str(ret[1].state).lower() == 'up'):
                        return host[0]
                    retries += -1
            return FAILED
        except Exception as e:
            printException(e)
            return FAILED

    def delete(self, api_client):
        """Delete Host"""
        # Host must be in maintenance mode before deletion
        cmd = {'id': self.id}
        api_client.prepareHostForMaintenance(**cmd)
        time.sleep(30)

        cmd = {'id': self.id}
        api_client.deleteHost(**cmd)
        return

    def enableMaintenance(self, api_client):
        """enables maintenance mode Host"""

        cmd = {'id': self.id}
        return api_client.prepareHostForMaintenance(**cmd)

    @classmethod
    def enableMaintenance(cls, api_client, id):
        """enables maintenance mode Host"""

        cmd = {'id': id}
        return api_client.prepareHostForMaintenance(**cmd)

    def cancelMaintenance(self, api_client):
        """Cancels maintenance mode Host"""

        cmd = {'id': self.id}
        return api_client.cancelHostMaintenance(**cmd)

    @classmethod
    def cancelMaintenance(cls, api_client, id):
        """Cancels maintenance mode Host"""

        cmd = {'id': id}
        return api_client.cancelHostMaintenance(**cmd)

    @classmethod
    def list(cls, api_client, **kwargs):
        """List all Hosts matching criteria"""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return super(Host, cls).list(api_client.listHosts(**cmd).get('host'))

    @classmethod
    def listForMigration(cls, api_client, **kwargs):
        """List all Hosts for migration matching criteria"""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return api_client.findHostsForMigration(**cmd)

    @classmethod
    def update(cls, api_client, **kwargs):
        """Update host information"""

        cmd = {}
        cmd.update(kwargs)
        return api_client.updateHost(**cmd)

    @classmethod
    def reconnect(cls, api_client, **kwargs):
        """Reconnect the Host"""

        cmd = {}
        cmd.update(kwargs)
        return api_client.reconnectHost(**cmd)

    def validateState(self, api_client, states, timeout=600, interval=5):
        """List Host and check if its resource state is as expected
        @returnValue - List[Result, Reason]
                       1) Result - FAIL if there is any exception
                       in the operation or Host state does not change
                       to expected state in given time else PASS
                       2) Reason - Reason for failure"""
        return validate_state(api_client, self, states, timeout, interval)

    @staticmethod
    def state_check_function(objects, states):
        return str(objects[0].state).lower().decode('string_escape') == str(states[0]).lower() and str(
            objects[0].resourcestate).lower().decode('string_escape') == str(states[1]).lower()


class StoragePool:
    """Manage Storage pools (Primary Storage)"""

    def __init__(self, items):
        self.__dict__.update(items)

    @classmethod
    def create(cls, api_client, services, scope=None, clusterid=None,
               zoneid=None, podid=None, provider=None, tags=None,
               capacityiops=None, capacitybytes=None, hypervisor=None):
        """Create Storage pool (Primary Storage)"""

        cmd = {'name': services["name"]}

        if podid:
            cmd['podid'] = podid
        elif "podid" in services:
            cmd['podid'] = services["podid"]

        cmd['url'] = services["url"]
        if clusterid:
            cmd['clusterid'] = clusterid
        elif "clusterid" in services:
            cmd['clusterid'] = services["clusterid"]

        if zoneid:
            cmd['zoneid'] = zoneid
        else:
            cmd['zoneid'] = services["zoneid"]

        if scope:
            cmd['scope'] = scope
        elif "scope" in services:
            cmd['scope'] = services["scope"]

        if provider:
            cmd['provider'] = provider
        elif "provider" in services:
            cmd['provider'] = services["provider"]

        if tags:
            cmd['tags'] = tags
        elif "tags" in services:
            cmd['tags'] = services["tags"]

        if capacityiops:
            cmd['capacityiops'] = capacityiops
        elif "capacityiops" in services:
            cmd['capacityiops'] = services["capacityiops"]

        if capacitybytes:
            cmd['capacitybytes'] = capacitybytes
        elif "capacitybytes" in services:
            cmd['capacitybytes'] = services["capacitybytes"]

        if hypervisor:
            cmd['hypervisor'] = hypervisor
        elif "hypervisor" in services:
            cmd['hypervisor'] = services["hypervisor"]

        return StoragePool(api_client.createStoragePool(**cmd))

    def delete(self, api_client):
        """Delete Storage pool (Primary Storage)"""

        # Storage pool must be in maintenance mode before deletion
        cmd = {'id': self.id}
        api_client.enableStorageMaintenance(**cmd)
        time.sleep(30)
        cmd = {'id': self.id}
        api_client.deleteStoragePool(**cmd)
        return

    def enableMaintenance(self, api_client):
        """enables maintenance mode Storage pool"""

        cmd = {'id': self.id}
        return api_client.enableStorageMaintenance(**cmd)

    @classmethod
    def enableMaintenance(cls, api_client, id):
        """enables maintenance mode Storage pool"""

        cmd = {'id': id}
        return api_client.enableStorageMaintenance(**cmd)

    @classmethod
    def cancelMaintenance(cls, api_client, id):
        """Cancels maintenance mode Host"""

        cmd = {'id': id}
        return api_client.cancelStorageMaintenance(**cmd)

    @classmethod
    def list(cls, api_client, **kwargs):
        """List all storage pools matching criteria"""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return api_client.listStoragePools(**cmd)

    @classmethod
    def listForMigration(cls, api_client, **kwargs):
        """List all storage pools for migration matching criteria"""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return api_client.findStoragePoolsForMigration(**cmd)

    @classmethod
    def update(cls, api_client, **kwargs):
        """Update storage pool"""
        cmd = {}
        cmd.update(kwargs)
        return api_client.updateStoragePool(**cmd)

    def validateState(self, api_client, state, timeout=600, interval=5):
        """List StoragePools and check if its  state is as expected
        @returnValue - List[Result, Reason]
                       1) Result - FAIL if there is any exception
                       in the operation or pool state does not change
                       to expected state in given time else PASS
                       2) Reason - Reason for failure"""

        return validate_state(api_client, self, state, timeout, interval)

    @staticmethod
    def state_check_function(objects, state):
        return str(objects[0].state).lower().decode("string_escape") == str(state).lower()


class Network(BaseAbstract):
    """Manage Network pools"""

    def __init__(self, items):
        super(Network, self).__init__(items)

    @classmethod
    def create(cls, api_client, services=None, accountid=None, domainid=None, networkofferingid=None, projectid=None,
               subdomainaccess=None, zoneid=None, gateway=None, netmask=None, cidr=None, vpcid=None, aclid=None,
               vlan=None, ipexclusionlist=None, domain=None, account=None, vpc=None, zone=None, acl=None, data=None):
        """Create Network for account"""
        if data:
            services = data

        cmd = {'name': services["name"], 'displaytext': services["displaytext"]}

        if networkofferingid:
            cmd['networkofferingid'] = networkofferingid
        elif "networkoffering" in services:
            cmd['networkofferingid'] = services["networkoffering"]
        elif "networkofferingname" in services:
            networkoffering = get_network_offering(api_client, services["networkofferingname"])
            cmd['networkofferingid'] = networkoffering.id

        if zoneid:
            cmd['zoneid'] = zoneid
        elif "zoneid" in services:
            cmd['zoneid'] = services["zoneid"]
        elif zone:
            cmd['zoneid'] = zone.id

        if ipexclusionlist:
            cmd['ipexclusionlist'] = ipexclusionlist
        elif "ipexclusionlist" in services:
            cmd['ipexclusionlist'] = services["ipexclusionlist"]

        if subdomainaccess is not None:
            cmd['subdomainaccess'] = subdomainaccess

        if gateway:
            cmd['gateway'] = gateway
        elif "gateway" in services:
            cmd['gateway'] = services["gateway"]
        if netmask:
            cmd['netmask'] = netmask
        elif "netmask" in services:
            cmd['netmask'] = services["netmask"]
        if cidr:
            cmd['cidr'] = cidr
        elif "cidr" in services:
            cmd['cidr'] = services["cidr"]
        if "startip" in services:
            cmd['startip'] = services["startip"]
        if "endip" in services:
            cmd['endip'] = services["endip"]
        if vlan:
            cmd['vlan'] = vlan
        elif "vlan" in services:
            cmd['vlan'] = services["vlan"]
        if "acltype" in services:
            cmd['acltype'] = services["acltype"]

        if accountid:
            cmd['account'] = accountid
        elif account:
            cmd['account'] = account.name
        elif vpc:
            cmd['account'] = vpc.account
        elif account:
            cmd['account'] = account.name
        if domainid:
            cmd['domainid'] = domainid
        elif domain:
            cmd['domainid'] = domain.id
        elif vpc:
            cmd['domainid'] = vpc.domainid
        elif account:
            cmd['domainid'] = account.domainid
        if projectid:
            cmd['projectid'] = projectid
        if vpcid:
            cmd['vpcid'] = vpcid
        elif vpc:
            cmd['vpcid'] = vpc.id
        if aclid:
            cmd['aclid'] = aclid
        elif acl:
            cmd['aclid'] = acl.id
        elif "aclname" in services:
            acl = get_network_acl(api_client, services['aclname'])
            cmd['aclid'] = acl.id
        network_resp = api_client.createNetwork(**cmd)
        return Network(network_resp['network'])

    def delete(self, api_client):
        """Delete Account"""

        cmd = {'id': self.id, 'fetch_result': True}
        api_client.deleteNetwork(**cmd)

    def update(self, api_client, **kwargs):
        """Updates network with parameters passed"""

        cmd = {'id': self.id}
        cmd.update(kwargs)
        return api_client.updateNetwork(**cmd)

    def restart(self, api_client, cleanup=None):
        """Restarts the network"""

        cmd = {'id': self.id}
        if cleanup:
            cmd['cleanup'] = cleanup
        return api_client.restartNetwork(**cmd)

    @classmethod
    def list(cls, api_client, **kwargs):
        """List all Networks matching criteria"""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return api_client.listNetworks(**cmd)


class NetworkACL(BaseAbstract):
    """Manage Network ACL lifecycle"""

    def __init__(self, items):
        super(NetworkACL, self).__init__(items)

    @classmethod
    def create(cls, api_client, services=None, networkid=None, protocol=None, number=None, aclid=None, action='Allow',
               traffictype=None, cidrlist=None, acl=None, data=None):
        """Create network ACL rules(Ingress/Egress)"""
        if data:
            services = data

        if cidrlist is None:
            cidrlist = []
        cmd = {}
        if "networkid" in services:
            cmd['networkid'] = services["networkid"]
        elif networkid:
            cmd['networkid'] = networkid

        if "protocol" in services:
            cmd['protocol'] = services["protocol"]
            if services["protocol"] == 'ICMP':
                cmd['icmptype'] = -1
                cmd['icmpcode'] = -1
        elif protocol:
            cmd['protocol'] = protocol

        if "startport" in services:
            cmd['startport'] = services["startport"]
        if "endport" in services:
            cmd['endport'] = services["endport"]

        if "cidrlist" in services:
            cmd['cidrlist'] = services["cidrlist"]
        elif cidrlist:
            cmd['cidrlist'] = cidrlist

        if "traffictype" in services:
            cmd['traffictype'] = services["traffictype"]
        elif traffictype:
            cmd['traffictype'] = traffictype

        if "action" in services:
            cmd['action'] = services["action"]
        elif action:
            cmd['action'] = action

        if "number" in services:
            cmd['number'] = services["number"]
        elif number:
            cmd['number'] = number

        if "aclid" in services:
            cmd['aclid'] = services["aclid"]
        elif aclid:
            cmd['aclid'] = aclid
        elif acl:
            cmd['aclid'] = acl.id

        # Defaulted to Ingress
        nwacl_resp = NetworkACL(api_client.createNetworkACL(**cmd))
        waitforjob(api_client, nwacl_resp.jobid)
        return cls.list(api_client, id=nwacl_resp.id)[0]

    def delete(self, api_client):
        """Delete network acl"""

        cmd = {'id': self.id}
        return api_client.deleteNetworkACL(**cmd)

    @classmethod
    def list(cls, api_client, **kwargs):
        """List Network ACLs"""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return super(NetworkACL, cls).list(api_client.listNetworkACLs(**cmd)['networkacl'])


class NetworkACLList(BaseAbstract):
    """Manage Network ACL lists lifecycle"""

    def __init__(self, items):
        super(NetworkACLList, self).__init__(items)

    @classmethod
    def create(
            cls, api_client, services=None, name=None, description=None, vpcid=None, vpc=None, data=None):
        """Create network ACL container list"""
        if data:
            services = data

        cmd = {}
        if "name" in services:
            cmd['name'] = services["name"]
        elif name:
            cmd['name'] = name

        if "description" in services:
            cmd['description'] = services["description"]
        elif description:
            cmd['description'] = description

        if "vpcid" in services:
            cmd['vpcid'] = services["vpcid"]
        elif vpcid:
            cmd['vpcid'] = vpcid
        elif vpc:
            cmd['vpcid'] = vpc.id

        response = NetworkACLList(api_client.createNetworkACLList(**cmd))
        waitforjob(api_client, response.jobid)
        return cls.list(api_client, id=response.id)[0]

    def delete(self, api_client):
        """Delete network acl list"""

        cmd = {'id': self.id}
        return api_client.deleteNetworkACLList(**cmd)

    @classmethod
    def list(cls, api_client, **kwargs):
        """List Network ACL lists"""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return super(NetworkACLList, cls).list(api_client.listNetworkACLLists(**cmd)['networkacllist'])

    def attach(self, api_client, network=None):
        cmd = {'aclid': self.id}

        if network:
            cmd['networkid'] = network.id

        return api_client.replaceNetworkACLList(**cmd)


class Vpn(BaseAbstract):
    """Manage VPN life cycle"""

    def __init__(self, items):
        super(Vpn, self).__init__(items)

    @classmethod
    def create(cls, api_client, publicipid, account=None, domainid=None,
               projectid=None, networkid=None, vpcid=None, openfirewall=None, iprange=None, fordisplay=False):
        """Create VPN for Public IP address"""
        cmd = {'publicipid': publicipid}
        if account:
            cmd['account'] = account
        if domainid:
            cmd['domainid'] = domainid
        if projectid:
            cmd['projectid'] = projectid
        if networkid:
            cmd['networkid'] = networkid
        if vpcid:
            cmd['vpcid'] = vpcid
        if iprange:
            cmd['iprange'] = iprange
        if openfirewall:
            cmd['openfirewall'] = openfirewall

        cmd['fordisplay'] = fordisplay
        cmd['fetch_result'] = True
        return Vpn(api_client.createRemoteAccessVpn(**cmd).get('remoteaccessvpn'))

    @classmethod
    def createVpnGateway(cls, api_client, vpcid=None, vpc=None):
        """Create VPN Gateway """
        cmd = {}
        if vpcid:
            cmd['vpcid'] = vpcid
        elif vpc:
            cmd['vpcid'] = vpc.id
        cmd['fetch_result'] = True
        return Vpn(api_client.createVpnGateway(**cmd).get('vpngateway'))

    @classmethod
    def createVpnConnection(cls, api_client, s2scustomergatewayid, s2svpngatewayid, passive=False):
        """Create VPN Connection """
        cmd = {'s2scustomergatewayid': s2scustomergatewayid, 's2svpngatewayid': s2svpngatewayid, 'fetch_result': True}
        if passive:
            cmd['passive'] = passive
        return api_client.createVpnConnection(**cmd).get('vpnconnection')

    @classmethod
    def resetVpnConnection(cls, api_client, id):
        """Reset VPN Connection """
        cmd = {'id': id}
        cmd['fetch_result'] = True
        return api_client.resetVpnConnection(**cmd)

    @classmethod
    def deleteVpnConnection(cls, api_client, id):
        """Delete VPN Connection """
        cmd = {'id': id}
        cmd['fetch_result'] = True
        return api_client.deleteVpnConnection(**cmd)

    @classmethod
    def listVpnGateway(cls, api_client, **kwargs):
        """List all VPN Gateways matching criteria"""
        cmd = {}
        cmd.update(kwargs)
        return super(Vpn, cls).list(api_client.listVpnGateways(**cmd))

    @classmethod
    def listVpnConnection(cls, api_client, **kwargs):
        """List all VPN Connections matching criteria"""
        cmd = {}
        cmd.update(kwargs)
        return super(Vpn, cls).list(api_client.listVpnConnections(**cmd).get('vpnconnection', []))

    def delete(self, api_client):
        """Delete remote VPN access"""

        cmd = {'publicipid': self.publicipid}
        api_client.deleteRemoteAccessVpn(**cmd)

    @classmethod
    def list(cls, api_client, **kwargs):
        """List all VPN matching criteria"""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return super(Vpn, cls).list(api_client.listRemoteAccessVpns(**cmd).get('remoteaccessvpn'))


class VpnUser(BaseAbstract):
    """Manage VPN user"""

    def __init__(self, items):
        super(VpnUser, self).__init__(items)

    @classmethod
    def create(cls, api_client, username, password, account=None, domainid=None,
               projectid=None, rand_name=True):
        """Create VPN user"""
        cmd = {'username': "-".join([username,
                                     random_gen()]) if rand_name else username, 'password': password}

        if account:
            cmd['account'] = account
        if domainid:
            cmd['domainid'] = domainid
        if projectid:
            cmd['projectid'] = projectid
        cmd['fetch_result'] = True
        return VpnUser(api_client.addVpnUser(**cmd).get('vpnuser'))

    def delete(self, api_client, projectid=None):
        """Remove VPN user"""

        cmd = {'username': self.username}
        if projectid:
            cmd['projectid'] = projectid
        else:
            cmd['account'] = self.account
            cmd['domainid'] = self.domainid
        api_client.removeVpnUser(**cmd)

    @classmethod
    def list(cls, api_client, **kwargs):
        """List all VPN Users matching criteria"""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return super(VpnUser, cls).list(api_client.listVpnUsers(**cmd).get('vpnuser'))


class Zone(BaseAbstract):
    """Manage Zone"""

    def __init__(self, items):
        super(Zone, self).__init__(items)

    @classmethod
    def create(cls, api_client, services, domainid=None):
        """Create zone"""
        cmd = {'dns1': services["dns1"], 'internaldns1': services["internaldns1"], 'name': services["name"], 'networktype': services["networktype"]}

        if "dns2" in services:
            cmd['dns2'] = services["dns2"]
        if "internaldns2" in services:
            cmd['internaldns2'] = services["internaldns2"]
        if domainid:
            cmd['domainid'] = domainid
        return Zone(api_client.createZone(**cmd).get('zone'))

    def delete(self, api_client):
        """Delete Zone"""

        cmd = {'id': self.id}
        api_client.deleteZone(**cmd)

    def update(self, api_client, **kwargs):
        """Update the zone"""

        cmd = {'id': self.id}
        cmd.update(kwargs)
        return api_client.updateZone(**cmd)

    @classmethod
    def list(cls, api_client, **kwargs):
        """List all Zones matching criteria"""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return super(Zone, cls).list(api_client.listZones(**cmd).get('zone'))


class Pod:
    """Manage Pod"""

    def __init__(self, items):
        self.__dict__.update(items)

    @classmethod
    def create(cls, api_client, services):
        """Create Pod"""
        cmd = {'gateway': services["gateway"], 'netmask': services["netmask"], 'name': services["name"], 'startip': services["startip"], 'endip': services["endip"],
               'zoneid': services["zoneid"]}

        return Pod(api_client.createPod(**cmd))

    def delete(self, api_client):
        """Delete Pod"""

        cmd = {'id': self.id}
        api_client.deletePod(**cmd)

    @classmethod
    def list(cls, api_client, **kwargs):
        """Returns a default pod for specified zone"""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return api_client.listPods(**cmd)

    @classmethod
    def update(cls, api_client, **kwargs):
        """Update the pod"""

        cmd = {}
        cmd.update(kwargs)
        return api_client.updatePod(**cmd)


class PublicIpRange:
    """Manage VlanIpRange"""

    def __init__(self, items):
        self.__dict__.update(items)

    @classmethod
    def create(cls, api_client, services, account=None, domainid=None):
        """Create VlanIpRange"""

        cmd = {'gateway': services["gateway"], 'netmask': services["netmask"], 'forvirtualnetwork': services["forvirtualnetwork"], 'startip': services["startip"],
               'endip': services["endip"], 'zoneid': services["zoneid"]}
        if "podid" in services:
            cmd['podid'] = services["podid"]
        cmd['vlan'] = services["vlan"]

        if account:
            cmd['account'] = account
        if domainid:
            cmd['domainid'] = domainid

        return PublicIpRange(api_client.createVlanIpRange(**cmd))

    def delete(self, api_client):
        """Delete VlanIpRange"""

        cmd = {'id': self.vlan.id}
        api_client.deleteVlanIpRange(**cmd)

    @classmethod
    def list(cls, api_client, **kwargs):
        """Lists all VLAN IP ranges."""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return api_client.listVlanIpRanges(**cmd)

    @classmethod
    def dedicate(
            cls, api_client, id, account=None, domainid=None, projectid=None):
        """Dedicate VLAN IP range"""

        cmd = {'id': id, 'account': account, 'domainid': domainid, 'projectid': projectid}
        return PublicIpRange(api_client.dedicatePublicIpRange(**cmd))

    def release(self, api_client):
        """Release VLAN IP range"""

        cmd = {'id': self.vlan.id}
        return api_client.releasePublicIpRange(**cmd)


class SecondaryStagingStore:
    """Manage Staging Store"""

    def __init__(self, items):
        self.__dict__.update(items)

    @classmethod
    def create(cls, api_client, url, provider, services=None):
        """Create Staging Storage"""
        cmd = {'url': url, 'provider': provider}
        if services:
            if "zoneid" in services:
                cmd['zoneid'] = services["zoneid"]
            if "details" in services:
                cmd['details'] = services["details"]
            if "scope" in services:
                cmd['scope'] = services["scope"]

        return SecondaryStagingStore(api_client.createSecondaryStagingStore(**cmd))

    def delete(self, api_client):
        """Delete Staging Storage"""
        cmd = {'id': self.id}
        api_client.deleteSecondaryStagingStore(**cmd)

    @classmethod
    def list(cls, api_client, **kwargs):
        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return api_client.listSecondaryStagingStores(**cmd)


class ImageStore:
    """Manage image stores"""

    def __init__(self, items):
        self.__dict__.update(items)

    @classmethod
    def create(cls, api_client, url, provider, services=None):
        """Add Image Store"""
        cmd = {'url': url, 'provider': provider}
        if services:
            if "zoneid" in services:
                cmd['zoneid'] = services["zoneid"]
            if "details" in services:
                cmd['details'] = services["details"]
            if "scope" in services:
                cmd['scope'] = services["scope"]

        return ImageStore(api_client.addImageStore(**cmd))

    def delete(self, api_client):
        """Delete Image Store"""
        cmd = {'id': self.id}
        api_client.deleteImageStore(**cmd)

    @classmethod
    def list(cls, api_client, **kwargs):
        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return api_client.listImageStores(**cmd)


class PhysicalNetwork:
    """Manage physical network storage"""

    def __init__(self, items):
        self.__dict__.update(items)

    @classmethod
    def create(cls, api_client, services, zoneid, domainid=None):
        """Create physical network"""
        cmd = {'name': services["name"], 'zoneid': zoneid}

        if domainid:
            cmd['domainid'] = domainid
        return PhysicalNetwork(api_client.createPhysicalNetwork(**cmd))

    def delete(self, api_client):
        """Delete Physical Network"""

        cmd = {'id': self.id}
        api_client.deletePhysicalNetwork(**cmd)

    def update(self, api_client, **kwargs):
        """Update Physical network state"""

        cmd = {'id': self.id}
        cmd.update(kwargs)
        return api_client.updatePhysicalNetwork(**cmd)

    def addTrafficType(self, api_client, type):
        """Add Traffic type to Physical network"""

        cmd = {'physicalnetworkid': self.id, 'traffictype': type}
        return api_client.addTrafficType(**cmd)

    @classmethod
    def dedicate(cls, api_client, vlanrange, physicalnetworkid,
                 account=None, domainid=None, projectid=None):
        """Dedicate guest vlan range"""

        cmd = {'vlanrange': vlanrange, 'physicalnetworkid': physicalnetworkid, 'account': account, 'domainid': domainid, 'projectid': projectid}
        return PhysicalNetwork(api_client.dedicateGuestVlanRange(**cmd))

    def release(self, api_client):
        """Release guest vlan range"""

        cmd = {'id': self.id}
        return api_client.releaseDedicatedGuestVlanRange(**cmd)

    @classmethod
    def listDedicated(cls, api_client, **kwargs):
        """Lists all dedicated guest vlan ranges"""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return api_client.listDedicatedGuestVlanRanges(**cmd)

    @classmethod
    def list(cls, api_client, **kwargs):
        """Lists all physical networks"""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return map(lambda pn: PhysicalNetwork(
            pn.__dict__), api_client.listPhysicalNetworks(**cmd))


class VpnCustomerGateway(BaseAbstract):
    """Manage VPN Customer Gateway"""

    def __init__(self, items):
        super(VpnCustomerGateway, self).__init__(items)

    @classmethod
    def create(cls, api_client, services=None, name=None, gateway=None, cidrlist=None, account=None, domainid=None,
               presharedkey=None, ikepolicy=None, esppolicy=None):
        """Create VPN Customer Gateway"""
        cmd = {'name': name, 'gateway': gateway, 'cidrlist': cidrlist}

        if not services:
            services = {}
        if "ipsecpsk" in services:
            cmd['ipsecpsk'] = services["ipsecpsk"]
        elif presharedkey:
            cmd['ipsecpsk'] = presharedkey

        if "ikepolicy" in services:
            cmd['ikepolicy'] = services["ikepolicy"]
        elif ikepolicy:
            cmd['ikepolicy'] = ikepolicy

        if "ikelifetime" in services:
            cmd['ikelifetime'] = services["ikelifetime"]

        if "esppolicy" in services:
            cmd['esppolicy'] = services["esppolicy"]
        elif esppolicy:
            cmd['esppolicy'] = esppolicy

        if "esplifetime" in services:
            cmd['esplifetime'] = services["esplifetime"]

        if "dpd" in services:
            cmd['dpd'] = services["dpd"]

        if "forceencap" in services:
            cmd['forceencap'] = services["forceencap"]

        if account:
            cmd['account'] = account
        if domainid:
            cmd['domainid'] = domainid
        cmd['fetch_result'] = True
        return VpnCustomerGateway(api_client.createVpnCustomerGateway(**cmd).get('vpncustomergateway'))

    def update(self, api_client, services, name, gateway, cidrlist):
        """Updates VPN Customer Gateway"""

        cmd = {'id': self.id, 'name': name, 'gateway': gateway, 'cidrlist': cidrlist}
        if "ipsecpsk" in services:
            cmd['ipsecpsk'] = services["ipsecpsk"]
        if "ikepolicy" in services:
            cmd['ikepolicy'] = services["ikepolicy"]
        if "ikelifetime" in services:
            cmd['ikelifetime'] = services["ikelifetime"]
        if "esppolicy" in services:
            cmd['esppolicy'] = services["esppolicy"]
        if "esplifetime" in services:
            cmd['esplifetime'] = services["esplifetime"]
        if "dpd" in services:
            cmd['dpd'] = services["dpd"]
        if "forceencap" in services:
            cmd['forceencap'] = services["forceencap"]
        return api_client.updateVpnCustomerGateway(**cmd)

    def delete(self, api_client):
        """Delete VPN Customer Gateway"""

        cmd = {'id': self.id}
        api_client.deleteVpnCustomerGateway(**cmd)

    @classmethod
    def list(cls, api_client, **kwargs):
        """List all VPN customer Gateway"""

        cmd = {}
        # cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return super(VpnCustomerGateway, cls).list(api_client.listVpnCustomerGateways(**cmd).get('vpncustomergateway'))


class Project:
    """Manage Project life cycle"""

    def __init__(self, items):
        self.__dict__.update(items)

    @classmethod
    def create(cls, api_client, services, account=None, domainid=None):
        """Create project"""

        cmd = {'displaytext': services["displaytext"], 'name': "-".join([services["name"], random_gen()])}
        if account:
            cmd['account'] = account
        if domainid:
            cmd['domainid'] = domainid

        return Project(api_client.createProject(**cmd))

    def delete(self, api_client):
        """Delete Project"""

        cmd = {'id': self.id}
        api_client.deleteProject(**cmd)

    def update(self, api_client, **kwargs):
        """Updates the project"""

        cmd = {'id': self.id}
        # cmd.update(kwargs)
        return api_client.updateProject(**cmd)

    def activate(self, api_client):
        """Activates the suspended project"""

        cmd = {'id': self.id}
        return api_client.activateProject(**cmd)

    def suspend(self, api_client):
        """Suspend the active project"""

        cmd = {'id': self.id}
        return api_client.suspendProject(**cmd)

    def addAccount(self, api_client, account=None, email=None):
        """Add account to project"""

        cmd = {'projectid': self.id}
        if account:
            cmd['account'] = account
        if email:
            cmd['email'] = email
        return api_client.addAccountToProject(**cmd)

    def deleteAccount(self, api_client, account):
        """Delete account from project"""

        cmd = {'projectid': self.id, 'account': account}
        return api_client.deleteAccountFromProject(**cmd)

    @classmethod
    def listAccounts(cls, api_client, **kwargs):
        """Lists all accounts associated with projects."""

        cmd = {}
        # cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return api_client.listProjectAccounts(**cmd)

    @classmethod
    def list(cls, api_client, **kwargs):
        """Lists all projects."""

        cmd = {}
        # cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return api_client.listProjects(**cmd)


class ProjectInvitation:
    """Manage project invitations"""

    def __init__(self, items):
        self.__dict__.update(items)

    @classmethod
    def update(cls, api_client, projectid, accept, account=None, token=None):
        """Updates the project invitation for that account"""

        cmd = {'projectid': projectid, 'accept': accept}
        if account:
            cmd['account'] = account
        if token:
            cmd['token'] = token

        return api_client.updateProjectInvitation(**cmd)

    @staticmethod
    def delete(api_client, id):
        """Deletes the project invitation"""

        cmd = {'id': id}
        return api_client.deleteProjectInvitation(**cmd)

    @classmethod
    def list(cls, api_client, **kwargs):
        """Lists project invitations"""

        cmd = {}
        # cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return api_client.listProjectInvitations(**cmd)


class Configurations(BaseAbstract):
    """Manage Configuration"""

    def __init__(self, items=None):
        if items:
            super(Configurations, self).__init__(items)

    @classmethod
    def update(cls, api_client, name, value=None, zoneid=None):
        """Updates the specified configuration"""

        cmd = {'name': name, 'value': value}
        if zoneid:
            cmd['zoneid'] = zoneid
        api_client.updateConfiguration(**cmd)

    @classmethod
    def list(cls, api_client, **kwargs):
        """Lists configurations"""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return super(Configurations, cls).list(api_client.listConfigurations(**cmd).get('configuration', []))

    @classmethod
    def listCapabilities(cls, api_client, **kwargs):
        """Lists capabilities"""
        cmd = {}
        # cmd.update(kwargs)
        return api_client.listCapabilities(**cmd)


class NiciraNvp:
    def __init__(self, items):
        self.__dict__.update(items)

    @classmethod
    def add(cls, api_client, services, physicalnetworkid,
            hostname=None, username=None, password=None, transportzoneuuid=None):
        cmd = {'physicalnetworkid': physicalnetworkid}
        if hostname:
            cmd['hostname'] = hostname
        else:
            cmd['hostname'] = services['hostname']

        if username:
            cmd['username'] = username
        else:
            cmd['username'] = services['username']

        if password:
            cmd['password'] = password
        else:
            cmd['password'] = services['password']

        if transportzoneuuid:
            cmd['transportzoneuuid'] = transportzoneuuid
        else:
            cmd['transportzoneuuid'] = services['transportZoneUuid']

        return NiciraNvp(api_client.addNiciraNvpDevice(**cmd))

    def delete(self, api_client):
        cmd = {'nvpdeviceid': self.nvpdeviceid}
        api_client.deleteNiciraNvpDevice(**cmd)
        return

    @classmethod
    def list(cls, api_client, **kwargs):
        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return api_client.listNiciraNvpDevices(**cmd)


class NetworkServiceProvider:
    """Manage network serivce providers for CloudStack"""

    def __init__(self, items):
        self.__dict__.update(items)

    @classmethod
    def add(cls, api_client, name, physicalnetworkid, servicelist):
        """Adds network service provider"""

        cmd = {'name': name, 'physicalnetworkid': physicalnetworkid, 'servicelist': servicelist}
        return NetworkServiceProvider(
            api_client.addNetworkServiceProvider(**cmd))

    def delete(self, api_client):
        """Deletes network service provider"""

        cmd = {'id': self.id}
        return api_client.deleteNetworkServiceProvider(**cmd)

    def update(self, api_client, **kwargs):
        """Updates network service provider"""

        cmd = {'id': self.id}
        cmd.update(kwargs)
        return api_client.updateNetworkServiceProvider(**cmd)

    @classmethod
    def update(cls, api_client, id, **kwargs):
        """Updates network service provider"""

        cmd = {'id': id}
        cmd.update(kwargs)
        return api_client.updateNetworkServiceProvider(**cmd)

    @classmethod
    def list(cls, api_client, **kwargs):
        """List network service providers"""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return api_client.listNetworkServiceProviders(**cmd)


class Router:
    """Manage router life cycle"""

    def __init__(self, items):
        self.__dict__.update(items)

    @classmethod
    def start(cls, api_client, id):
        """Starts the router"""
        cmd = {'id': id}
        return api_client.startRouter(**cmd)

    @classmethod
    def stop(cls, api_client, id, forced=None):
        """Stops the router"""
        cmd = {'id': id}
        if forced:
            cmd['forced'] = forced
        return api_client.stopRouter(**cmd)

    @classmethod
    def reboot(cls, api_client, id):
        """Reboots the router"""
        cmd = {'id': id}
        return api_client.rebootRouter(**cmd)

    @classmethod
    def destroy(cls, api_client, id):
        """Destroy the router"""
        cmd = {'id': id}
        return api_client.destroyRouter(**cmd)

    @classmethod
    def change_service_offering(cls, api_client, id, serviceofferingid):
        """Change service offering of the router"""
        cmd = {'id': id, 'serviceofferingid': serviceofferingid}
        return api_client.changeServiceForRouter(**cmd)

    @classmethod
    def list(cls, api_client, **kwargs):
        """List routers"""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return api_client.listRouters(**cmd)


class Tag:
    """Manage tags"""

    def __init__(self, items):
        self.__dict__.update(items)

    @classmethod
    def create(cls, api_client, resourceIds, resourceType, tags):
        """Create tags"""

        cmd = {'resourceIds': resourceIds, 'resourcetype': resourceType, 'tags': []}
        for tag in tags:
            cmd.tags.append(tag)
        return Tag(api_client.createTags(**cmd))

    @staticmethod
    def delete(api_client, resourceIds, resourceType, tags):
        """Delete tags"""

        cmd = {'resourceIds': resourceIds, 'resourcetype': resourceType, 'tags': []}
        for key, value in tags.items():
            cmd.tags.append({
                'key': key,
                'value': value
            })
        api_client.deleteTags(**cmd)

    @classmethod
    def list(cls, api_client, **kwargs):
        """List all tags matching the criteria"""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return api_client.listTags(**cmd)


class VpcOffering:
    """Manage VPC offerings"""

    def __init__(self, items):
        self.__dict__.update(items)

    @classmethod
    def create(cls, api_client, services):
        """Create vpc offering"""

        cmd = {'name': "-".join([services["name"], random_gen()]), 'displaytext': services["displaytext"], 'supportedServices': services["supportedservices"]}
        if "serviceProviderList" in services:
            for service, provider in services["serviceProviderList"].items():
                providers = provider
                if isinstance(provider, str):
                    providers = [provider]

                for provider_item in providers:
                    cmd.serviceproviderlist.append({
                        'service': service,
                        'provider': provider_item
                    })

        if "serviceCapabilityList" in services:
            cmd['servicecapabilitylist'] = []
            for service, capability in \
                    services["serviceCapabilityList"].items():
                for ctype, value in capability.items():
                    cmd.servicecapabilitylist.append({
                        'service': service,
                        'capabilitytype': ctype,
                        'capabilityvalue': value
                    })
        return VpcOffering(api_client.createVPCOffering(**cmd))

    def update(self, api_client, name=None, displaytext=None, state=None):
        """Updates existing VPC offering"""

        cmd = {'id': self.id}
        if name:
            cmd['name'] = name
        if displaytext:
            cmd['displaytext'] = displaytext
        if state:
            cmd['state'] = state
        return api_client.updateVPCOffering(**cmd)

    @classmethod
    def list(cls, api_client, **kwargs):
        """List the VPC offerings based on criteria specified"""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return api_client.listVPCOfferings(**cmd)

    def delete(self, api_client):
        """Deletes existing VPC offering"""

        cmd = {'id': self.id}
        return api_client.deleteVPCOffering(**cmd)


class VPC(BaseAbstract):
    """Manage Virtual Private Connection"""

    def __init__(self, items, api_client=None):
        super(VPC, self).__init__(items, api_client=api_client)

    @classmethod
    def create(cls, api_client, services=None, vpcofferingid=None, zoneid=None, networkDomain=None, account=None,
               domainid=None, zone=None, data=None, randomizeID=True, **kwargs):
        """Creates the virtual private connection (VPC)"""
        if data:
            services = data

        cmd = {}

        random_name = ("-".join([services["name"], random_gen()]) if randomizeID else services["name"])
        cmd['name'] = random_name

        if "displaytext" in services:
            random_displaytext = ("-".join([services["displaytext"], random_gen()]) if randomizeID else services["displaytext"])
        else:
            random_displaytext = random_name
        cmd['displaytext'] = random_displaytext

        if vpcofferingid:
            cmd['vpcofferingid'] = vpcofferingid
        elif "vpcofferingname" in services:
            vpcoffering = get_vpc_offering(api_client, services["vpcofferingname"])
            cmd['vpcofferingid'] = vpcoffering.id

        if zoneid:
            cmd['zoneid'] = zoneid
        elif zone:
            cmd['zoneid'] = zone.id

        if "cidr" in services:
            cmd['cidr'] = services["cidr"]
        if account:
            if isinstance(account, str):
                cmd['account'] = account
            else:
                cmd['account'] = account.name
        if domainid:
            cmd['domainid'] = domainid
        elif account and not type(account) is str:
            cmd['domainid'] = account.domainid
        if networkDomain:
            cmd['networkDomain'] = networkDomain

        cmd['fetch_result'] = True
        cmd.update(kwargs)
        return VPC(api_client.createVPC(**cmd).get('vpc'), api_client)

    def update(self, api_client, name=None, displaytext=None):
        """Updates VPC configurations"""

        cmd = {'id': self.id}
        if name:
            cmd['name'] = name
        if displaytext:
            cmd['displaytext'] = displaytext
        return api_client.updateVPC(**cmd)

    def delete(self, api_client):
        """Delete VPC network"""

        cmd = {'id': self.id, 'fetch_result': True}
        return api_client.deleteVPC(**cmd)

    def restart(self, api_client=None, cleanup=False):
        """Restarts the VPC connections"""
        if api_client:
            self.api_client = api_client

        cmd = {'id': self.id, 'cleanup': cleanup, 'fetch_result': True}
        return self.api_client.restartVPC(**cmd)

    @classmethod
    def list(cls, api_client, **kwargs):
        """List VPCs"""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return super(VPC, cls).list(api_client.listVPCs(**cmd)['vpc'], api_client=api_client)

    def get_routers(self):
        return list_routers(api_client=self.api_client, domainid=self.domainid, account=self.account, vpcid=self.id)

    def stop_master_router(self):
        routers = self.get_routers()

        for router in routers:
            if router.redundantstate == 'MASTER':
                cmd = {}
                self.api_client.stopRouter(**cmd)
                break

    def is_master_backup(self):
        master = backup = None
        routers = self.get_routers()

        if len(routers) != 2:
            return False

        for router in routers:
            if router.state == 'Running':
                if router.redundantstate == 'MASTER':
                    master = router.hostid
                elif router.redundantstate == 'BACKUP':
                    backup = router.hostid

        if master and backup and master != backup:
            return True

        return False

    def is_state_ok(self):
        master = backup = None
        routers = self.get_routers()

        for router in routers:
            if router.state == 'Running':
                if router.redundantstate == 'MASTER':
                    master = True
                elif router.redundantstate == 'BACKUP':
                    backup = True

        if len(routers) == 1:
            return master
        elif len(routers) == 2:
            return master and backup
        return False


class PrivateGateway(BaseAbstract):
    """Manage private gateway lifecycle"""

    def __init__(self, items):
        super(PrivateGateway, self).__init__(items)

    @classmethod
    def create(cls, api_client, ipaddress=None, networkid=None, vpcid=None, sourcenatsupported=None, aclid=None,
               vpc=None, data=None):
        """Create private gateway"""

        cmd = {}
        if ipaddress:
            cmd['ipaddress'] = ipaddress
        elif "ip" in data:
            cmd['ipaddress'] = data["ip"]

        if networkid:
            cmd['networkid'] = networkid
        elif "privatenetworkname" in data:
            network = get_network(api_client, data["privatenetworkname"])
            cmd['networkid'] = network.id

        if vpcid:
            cmd['vpcid'] = vpcid
        elif vpc:
            cmd['vpcid'] = vpc.id

        if sourcenatsupported:
            cmd['sourcenatsupported'] = sourcenatsupported

        if aclid:
            cmd['aclid'] = aclid
        elif "aclname" in data:
            acl = get_network_acl(api_client, data["aclname"])
            cmd['aclid'] = acl.id

        pgw_resp = PrivateGateway(api_client.createPrivateGateway(**cmd))
        waitforjob(api_client, pgw_resp.jobid)
        return cls.list(api_client, id=pgw_resp.id)[0]

    def delete(self, api_client):
        """Delete private gateway"""

        cmd = {'id': self.id}
        return api_client.deletePrivateGateway(**cmd)

    @classmethod
    def list(cls, api_client, **kwargs):
        """List private gateways"""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return super(PrivateGateway, cls).list(api_client.listPrivateGateways(**cmd)['privategateway'])


class AffinityGroup(BaseAbstract):
    def __init__(self, items):
        super(AffinityGroup, self).__init__(items)

    @classmethod
    def create(cls, api_client, aff_grp, account=None, domainid=None, projectid=None):
        cmd = {'name': aff_grp['name'], 'displayText': aff_grp['name'], 'type': aff_grp['type']}
        if account:
            cmd['account'] = account
        if domainid:
            cmd['domainid'] = domainid
        if projectid:
            cmd['projectid'] = projectid
        return AffinityGroup(api_client.createAffinityGroup(**cmd))

    def update(self, api_client):
        pass

    def delete(self, api_client):
        cmd = {'id': self.id}
        return api_client.deleteAffinityGroup(**cmd)

    @classmethod
    def list(cls, api_client, **kwargs):
        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return api_client.listAffinityGroups(**cmd)


class StaticRoute(BaseAbstract):
    """Manage static route lifecycle"""

    def __init__(self, items):
        super(StaticRoute, self).__init__(items)

    @classmethod
    def create(cls, api_client, services=None, vpcid=None, cidr=None, nexthop=None, vpc=None, data=None):
        """Create static route"""

        cmd = {}
        if data:
            services = data

        if "cidr" in services:
            cmd['cidr'] = services["cidr"]
        elif cidr:
            cmd['cidr'] = cidr

        if "nexthop" in services:
            cmd['nexthop'] = services["nexthop"]
        elif nexthop:
            cmd['nexthop'] = nexthop

        if vpcid:
            cmd['vpcid'] = vpcid
        elif vpc:
            cmd['vpcid'] = vpc.id

        sr_resp = StaticRoute(api_client.createStaticRoute(**cmd))
        waitforjob(api_client, sr_resp.jobid)
        return cls.list(api_client, id=sr_resp.id)[0]

    def delete(self, api_client):
        """Delete static route"""

        cmd = {'id': self.id}
        return api_client.deleteStaticRoute(**cmd)

    @classmethod
    def list(cls, api_client, **kwargs):
        """List static route"""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return super(StaticRoute, cls).list(api_client.listStaticRoutes(**cmd).get('staticroute', []))


class VNMC:
    """Manage VNMC lifecycle"""

    def __init__(self, items):
        self.__dict__.update(items)

    @staticmethod
    def create(api_client, hostname, username, password,
               physicalnetworkid):
        """Registers VNMC appliance"""

        cmd = {'hostname': hostname, 'username': username, 'password': password, 'physicalnetworkid': physicalnetworkid}
        return VNMC(api_client.addCiscoVnmcResource(**cmd))

    def delete(self, api_client):
        """Removes VNMC appliance"""

        cmd = {'resourceid': self.resourceid}
        return api_client.deleteCiscoVnmcResource(**cmd)

    @classmethod
    def list(cls, api_client, **kwargs):
        """List VNMC appliances"""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return api_client.listCiscoVnmcResources(**cmd)


class SSHKeyPair:
    """Manage SSH Key pairs"""

    def __init__(self, items):
        self.__dict__.update(items)

    @classmethod
    def create(cls, api_client, name=None, account=None,
               domainid=None, projectid=None):
        """Creates SSH keypair"""
        cmd = {'name': name}
        if account is not None:
            cmd['account'] = account
        if domainid is not None:
            cmd['domainid'] = domainid
        if projectid is not None:
            cmd['projectid'] = projectid
        return api_client.createSSHKeyPair(**cmd)

    @classmethod
    def register(cls, api_client, name, publickey):
        """Registers SSH keypair"""
        cmd = {'name': name, 'publickey': publickey}
        return api_client.registerSSHKeyPair(**cmd)

    def delete(self, api_client):
        """Delete SSH key pair"""
        cmd = {'name': self.name}
        api_client.deleteSSHKeyPair(**cmd)

    @classmethod
    def list(cls, api_client, **kwargs):
        """List all SSH key pairs"""
        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return api_client.listSSHKeyPairs(**cmd)


class Capacities(BaseAbstract):
    """Manage Capacities"""

    def __init__(self, items=None):
        if items:
            super(Capacities, self).__init__(items)

    @classmethod
    def list(cls, api_client, **kwargs):
        """Lists capacities"""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return super(Capacities, cls).list(api_client.listCapacity(**cmd).get('capacity'))


class Alert(BaseAbstract):
    """Manage alerts"""

    def __init__(self, items=None):
        if items:
            super(Alert, self).__init__(items)

    @classmethod
    def list(cls, api_client, **kwargs):
        """Lists alerts"""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return super(Alert, cls).list(api_client.listAlerts(**cmd).get('alert'))


class InstanceGroup(BaseAbstract):
    """Manage VM instance groups"""

    def __init__(self, items):
        super(InstanceGroup, self).__init__(items)

    @classmethod
    def create(cls, api_client, name=None, account=None, domainid=None,
               projectid=None, networkid=None, rand_name=True):
        """Creates instance groups"""

        cmd = {'name': "-".join([name, random_gen()]) if rand_name else name}
        if account is not None:
            cmd['account'] = account
        if domainid is not None:
            cmd['domainid'] = domainid
        if projectid is not None:
            cmd['projectid'] = projectid
        if networkid is not None:
            cmd['networkid'] = networkid
        return InstanceGroup(api_client.createInstanceGroup(**cmd).get('instancegroup'))

    def delete(self, api_client):
        """Delete instance group"""
        cmd = {'id': self.id}
        api_client.deleteInstanceGroup(**cmd)

    def update(self, api_client, **kwargs):
        """Updates the instance groups"""
        cmd = {'id': self.id}
        cmd.update(kwargs)
        return api_client.updateInstanceGroup(**cmd)

    @classmethod
    def list(cls, api_client, **kwargs):
        """List all instance groups"""
        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return api_client.listInstanceGroups(**cmd)

    def startInstances(self, api_client):
        """Starts all instances in a VM tier"""

        cmd = {'group': self.id}
        return api_client.startVirtualMachine(**cmd)

    def stopInstances(self, api_client):
        """Stops all instances in a VM tier"""

        cmd = {'group': self.id}
        return api_client.stopVirtualMachine(**cmd)

    def rebootInstances(self, api_client):
        """Reboot all instances in a VM tier"""

        cmd = {'group': self.id}
        return api_client.rebootVirtualMachine(**cmd)

    def deleteInstances(self, api_client):
        """Stops all instances in a VM tier"""

        cmd = {'group': self.id}
        return api_client.destroyVirtualMachine(**cmd)

    def changeServiceOffering(self, api_client, serviceOfferingId):
        """Change service offering of the vm tier"""

        cmd = {'group': self.id, 'serviceofferingid': serviceOfferingId}
        return api_client.changeServiceForVirtualMachine(**cmd)

    def recoverInstances(self, api_client):
        """Recover the instances from vm tier"""
        cmd = {'group': self.id}
        api_client.recoverVirtualMachine(**cmd)


class VmSnapshot:
    """Manage VM Snapshot life cycle"""

    def __init__(self, items):
        self.__dict__.update(items)

    @classmethod
    def create(cls, api_client, vmid, snapshotmemory="false",
               name=None, description=None):
        cmd = {'virtualmachineid': vmid}

        if snapshotmemory:
            cmd['snapshotmemory'] = snapshotmemory
        if name:
            cmd['name'] = name
        if description:
            cmd['description'] = description
        return VmSnapshot(api_client.createVMSnapshot(**cmd))

    @classmethod
    def list(cls, api_client, **kwargs):
        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return api_client.listVMSnapshot(**cmd)

    @classmethod
    def revertToSnapshot(cls, api_client, vmsnapshotid):
        cmd = {'vmsnapshotid': vmsnapshotid}
        return api_client.revertToVMSnapshot(**cmd)

    @classmethod
    def deleteVMSnapshot(cls, api_client, vmsnapshotid):
        cmd = {'vmsnapshotid': vmsnapshotid}
        return api_client.deleteVMSnapshot(**cmd)


class Region:
    """ Regions related Api """

    def __init__(self, items):
        self.__dict__.update(items)

    @classmethod
    def create(cls, api_client, services):
        cmd = {'id': services["regionid"], 'endpoint': services["regionendpoint"], 'name': services["regionname"]}
        try:
            region = api_client.addRegion(**cmd)
            if region is not None:
                return Region(region.__dict__)
        except Exception as e:
            raise e

    @classmethod
    def list(cls, api_client, **kwargs):
        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        region = api_client.listRegions(**cmd)
        return region

    def update(self, api_client, services):
        cmd = {'id': self.id}
        if services["regionendpoint"]:
            cmd['endpoint'] = services["regionendpoint"]
        if services["regionname"]:
            cmd['name'] = services["regionname"]
        region = api_client.updateRegion(**cmd)
        return region

    def delete(self, api_client):
        cmd = {'id': self.id}
        region = api_client.removeRegion(**cmd)
        return region


class Resources:
    """Manage resource limits"""

    def __init__(self, items):
        self.__dict__.update(items)

    @classmethod
    def list(cls, api_client, **kwargs):
        """Lists resource limits"""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return api_client.listResourceLimits(**cmd)

    @classmethod
    def updateLimit(cls, api_client, **kwargs):
        """Updates resource limits"""

        cmd = {}
        cmd.update(kwargs)
        return api_client.updateResourceLimit(**cmd)

    @classmethod
    def updateCount(cls, api_client, **kwargs):
        """Updates resource count"""

        cmd = {}
        cmd.update(kwargs)
        return api_client.updateResourceCount(**cmd)


class NIC:
    """NIC related API"""

    def __init__(self, items):
        self.__dict__.update(items)

    @classmethod
    def addIp(cls, api_client, id, ipaddress=None):
        """Add Ip (secondary) to NIC"""
        cmd = {'nicid': id}
        if ipaddress:
            cmd['ipaddress'] = ipaddress
        return api_client.addIpToNic(**cmd)

    @classmethod
    def removeIp(cls, api_client, ipaddressid):
        """Remove secondary Ip from NIC"""
        cmd = {'id': ipaddressid}
        return api_client.addIpToNic(**cmd)

    @classmethod
    def list(cls, api_client, **kwargs):
        """List NICs belonging to a virtual machine"""

        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return api_client.listNics(**cmd)


class Usage:
    """Manage Usage Generation"""

    def __init__(self, items):
        self.__dict__.update(items)

    @classmethod
    def listRecords(cls, api_client, **kwargs):
        """Lists domains"""
        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return api_client.listUsageRecords(**cmd)

    @classmethod
    def listTypes(cls, api_client, **kwargs):
        """Lists domains"""
        cmd = {}
        cmd.update(kwargs)
        if 'account' in kwargs.keys() and 'domainid' in kwargs.keys():
            cmd['listall'] = True
        return api_client.listUsageTypes(**cmd)

    @classmethod
    def generateRecords(cls, api_client, **kwargs):
        """Lists domains"""
        cmd = {}
        cmd.update(kwargs)
        return api_client.generateUsageRecords(**cmd)


class TrafficType:
    """Manage different traffic types in the setup"""

    def __init__(self, items):
        self.__dict__.update(items)

    @classmethod
    def list(cls, api_client, **kwargs):
        """Lists traffic types"""

        cmd = {}
        cmd.update(kwargs)
        return api_client.listTrafficTypes(**cmd)


class StorageNetworkIpRange:
    """Manage Storage Network Ip Range"""

    def __init__(self, items):
        self.__dict__.update(items)

    @classmethod
    def list(cls, api_client, **kwargs):
        """Lists Storage Network IP Ranges"""

        cmd = {}
        cmd.update(kwargs)
        return api_client.listStorageNetworkIpRange(**cmd)
