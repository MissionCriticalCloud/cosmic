import time

from nose.plugins.attrib import attr

from marvin.cloudstackAPI import rebootRouter
from marvin.cloudstackTestCase import cloudstackTestCase
from marvin.codes import FAILED
from marvin.lib.base import (
    NATRule,
    LoadBalancerRule,
    FireWallRule,
    PublicIPAddress,
    VirtualMachine,
    Account
)
from marvin.lib.common import (
    list_virtual_machines,
    list_routers,
    list_public_ip,
    get_template,
    get_zone,
    get_domain,
    get_default_virtual_machine_offering
)
from marvin.lib.utils import cleanup_resources
from marvin.utils.MarvinLog import MarvinLog
from marvin.utils.SshClient import SshClient


class TestRebootRouter(cloudstackTestCase):
    def setUp(self):
        self.logger = MarvinLog(MarvinLog.LOGGER_TEST).get_logger()

        self.apiclient = self.testClient.getApiClient()
        self.services = self.testClient.getParsedTestDataConfig()

        # Get Zone, Domain and templates
        self.domain = get_domain(self.apiclient)
        self.zone = get_zone(self.apiclient, self.testClient.getZoneForTests())
        template = get_template(
            self.apiclient,
            self.zone.id
        )
        if template == FAILED:
            self.fail(
                "get_template() failed to return template with description %s" %
                self.services["ostype"])
        self.services["virtual_machine"]["zoneid"] = self.zone.id

        # Create an account, network, VM and IP addresses
        self.account = Account.create(
            self.apiclient,
            self.services["account"],
            admin=True,
            domainid=self.domain.id
        )
        self.service_offering = get_default_virtual_machine_offering(self.apiclient)
        self.vm_1 = VirtualMachine.create(
            self.apiclient,
            self.services["virtual_machine"],
            templateid=template.id,
            accountid=self.account.name,
            domainid=self.account.domainid,
            serviceofferingid=self.service_offering.id
        )

        src_nat_ip_addrs = list_public_ip(
            self.apiclient,
            account=self.account.name,
            domainid=self.account.domainid
        )
        try:
            src_nat_ip_addr = src_nat_ip_addrs[0]
        except Exception as e:
            raise Exception(
                "Warning: Exception during fetching source NAT: %s" %
                e)

        self.public_ip = PublicIPAddress.create(
            self.apiclient,
            self.vm_1.account,
            self.vm_1.zoneid,
            self.vm_1.domainid,
            self.services["virtual_machine"]
        )
        # Open up firewall port for SSH
        FireWallRule.create(
            self.apiclient,
            ipaddressid=self.public_ip.ipaddress.id,
            protocol=self.services["lbrule"]["protocol"],
            cidrlist=['0.0.0.0/0'],
            startport=self.services["lbrule"]["publicport"],
            endport=self.services["lbrule"]["publicport"]
        )

        lb_rule = LoadBalancerRule.create(
            self.apiclient,
            self.services["lbrule"],
            src_nat_ip_addr.id,
            self.account.name
        )
        lb_rule.assign(self.apiclient, [self.vm_1])
        self.nat_rule = NATRule.create(
            self.apiclient,
            self.vm_1,
            self.services["natrule"],
            ipaddressid=self.public_ip.ipaddress.id
        )
        self.cleanup = [self.nat_rule,
                        lb_rule,
                        self.vm_1,
                        self.account,
                        ]
        return

    @attr(tags=['advanced'])
    def test_01_reboot_router(self):
        """Test for reboot router"""

        # Before restart validate the VM can be accessed;
        #    if the router restarts while the VM has not finished starting,
        #    the VM will fail to get an IP from the router
        try:
            self.logger.debug("SSH into VM (ID : %s ) before reboot of router" % self.vm_1.id)

            SshClient(
                self.public_ip.ipaddress.ipaddress,
                self.services["natrule"]["publicport"],
                self.vm_1.username,
                self.vm_1.password,
                retries=20
            )
        except Exception as e:
            self.fail(
                "SSH Access failed for %s: %si, before reboot of Router" %
                (self.public_ip.ipaddress.ipaddress, e))

        # Validate the Following
        # 1. Post restart PF and LB rules should still function
        # 2. verify if the ssh into the virtual machine
        #   still works through the sourceNAT Ip

        # Retrieve router for the user account

        self.logger.debug("Public IP: %s" % self.vm_1.ssh_ip)
        self.logger.debug("Public IP: %s" % self.public_ip.ipaddress.ipaddress)
        routers = list_routers(
            self.apiclient,
            account=self.account.name,
            domainid=self.account.domainid
        )
        self.assertEqual(
            isinstance(routers, list),
            True,
            "Check list routers returns a valid list"
        )

        router = routers[0]

        self.logger.debug("Rebooting the router (ID: %s)" % router.id)

        cmd = rebootRouter.rebootRouterCmd()
        cmd.id = router.id
        self.apiclient.rebootRouter(cmd)

        # Poll listVM to ensure VM is stopped properly
        timeout = self.services["timeout"]

        while True:
            # Ensure that VM is in stopped state
            list_vm_response = list_virtual_machines(
                self.apiclient,
                id=self.vm_1.id
            )

            if isinstance(list_vm_response, list):

                vm = list_vm_response[0]
                if vm.state == 'Running':
                    self.logger.debug("VM state: %s" % vm.state)
                    break

            if timeout == 0:
                raise Exception(
                    "Failed to start VM (ID: %s) in change service offering" %
                    vm.id)

            time.sleep(self.services["sleep"])
            timeout = timeout - 1

        # we should be able to SSH after successful reboot
        try:
            self.logger.debug("SSH into VM (ID : %s ) after reboot" % self.vm_1.id)

            SshClient(
                self.public_ip.ipaddress.ipaddress,
                self.services["natrule"]["publicport"],
                self.vm_1.username,
                self.vm_1.password,
                retries=20
            )
        except Exception as e:
            self.fail(
                "SSH Access failed for %s: %s" %
                (self.public_ip.ipaddress.ipaddress, e))
        return

    def tearDown(self):
        cleanup_resources(self.apiclient, self.cleanup)
        return
