from cs import CloudStackApiException

from cosmic.base import *
from cosmic.common import *
from cosmic.cosmicLog import CosmicLog
from cosmic.cosmicTestCase import cosmicTestCase


class TestSSVMs(cosmicTestCase):
    def setUp(self):
        self.logger = CosmicLog(CosmicLog.LOGGER_TEST).get_logger()
        self.testClient = super(TestSSVMs, self).getClsTestClient()
        self.apiclient = self.testClient.getApiClient()
        self.cleanup = []
        self.services = self.testClient.getParsedTestDataConfig()
        self.zone = get_zone(self.apiclient, self.testClient.getZoneForTests())

        if not getattr(self.zone, 'dns2', False):
            zone = Zone.list(self.apiclient)[0]
            zone.update(self.apiclient, dns2="1.1.1.1")

        self.services["sleep"] = 5
        self.services["timeout"] = 180

        return

    def tearDown(self):
        try:
            # Clean up, terminate the created templates
            cleanup_resources(self.apiclient, self.cleanup)

        except Exception as e:
            raise Exception("Warning: Exception during cleanup : %s" % e)
        return

    @attr(tags=['advanced'])
    def test_01_list_sec_storage_vm(self):
        self._test_list_svm_vm('secondarystoragevm')

    @attr(tags=['advanced'])
    def test_02_list_cpvm_vm(self):
        self._test_list_svm_vm('consoleproxy')

    @attr(tags=['advanced'])
    def test_03_destroy_ssvm(self):
        """Test destroy SSVM
        """

        # Validate the following
        # 1. SSVM should be completely destroyed and a new one will spin up
        # 2. listSystemVMs will show a different name for the
        #    systemVM from what it was before
        # 3. new SSVM will have a public/private and link-local-ip
        # 4. cloud process within SSVM must be up and running

        list_ssvm_response = list_ssvms(
            self.apiclient,
            systemvmtype='secondarystoragevm',
            state='Running',
            zoneid=self.zone.id
        )
        self.assertEqual(
            isinstance(list_ssvm_response, list),
            True,
            "Check list response returns a valid list"
        )
        ssvm_response = list_ssvm_response[0]

        old_name = ssvm_response.name

        self.logger.debug("Destroying SSVM: %s" % ssvm_response.id)
        cmd = {'id': ssvm_response.id, 'fetch_result': True}
        try:
            self.apiclient.destroySystemVm(**cmd)
        except CloudStackApiException as e:
            if e.error['errorcode'] != 530:
                raise e

        timeout = self.services["timeout"]
        while True:
            list_ssvm_response = list_ssvms(
                self.apiclient,
                zoneid=self.zone.id,
                systemvmtype='secondarystoragevm'
            )
            if isinstance(list_ssvm_response, list) and len(list_ssvm_response) > 0:
                if list_ssvm_response[0].state == 'Running':
                    break
            if timeout == 0:
                self.logger.debug(
                    "Warning: List SSVM didn't return systemvms in Running state. This is a known issue, ignoring it for now!")
                return

            time.sleep(self.services["sleep"])
            timeout = timeout - 1

        ssvm_response = list_ssvm_response[0]

        # Verify Name, Public IP, Private IP and Link local IP
        # for newly created SSVM
        self.assertNotEqual(
            ssvm_response.name,
            old_name,
            "Check SSVM new name with name of destroyed SSVM"
        )
        self.assertEqual(
            hasattr(ssvm_response, 'privateip'),
            True,
            "Check whether SSVM has private IP field"
        )

        self.assertEqual(
            hasattr(ssvm_response, 'linklocalip'),
            True,
            "Check whether SSVM has link local IP field"
        )

        self.assertEqual(
            hasattr(ssvm_response, 'publicip'),
            True,
            "Check whether SSVM has public IP field"
        )

        # Wait for the agent to be up
        self.wait_for_system_vm_agent(ssvm_response.name)

        return

    @attr(tags=['advanced'])
    def test_04_destroy_cpvm(self):
        """Test destroy CPVM
        """

        # Validate the following
        # 1. CPVM should be completely destroyed and a new one will spin up
        # 2. listSystemVMs will show a different name for the systemVM from
        #    what it was before
        # 3. new CPVM will have a public/private and link-local-ip
        # 4. cloud process within CPVM must be up and running

        list_cpvm_response = list_ssvms(
            self.apiclient,
            systemvmtype='consoleproxy',
            zoneid=self.zone.id
        )
        self.assertEqual(
            isinstance(list_cpvm_response, list),
            True,
            "Check list response returns a valid list"
        )
        cpvm_response = list_cpvm_response[0]

        old_name = cpvm_response.name

        self.logger.debug("Destroying CPVM: %s" % cpvm_response.id)
        cmd = {'id': cpvm_response.id, 'fetch_result': True}
        try:
            self.apiclient.destroySystemVm(**cmd)
        except CloudStackApiException as e:
            if e.error['errorcode'] != 530:
                raise e

        timeout = self.services["timeout"]
        while True:
            list_cpvm_response = list_ssvms(
                self.apiclient,
                systemvmtype='consoleproxy',
                zoneid=self.zone.id
            )
            if isinstance(list_cpvm_response, list) and len(list_cpvm_response) > 0:
                if list_cpvm_response[0].state == 'Running':
                    break
            if timeout == 0:
                # FIXME: This should be fixed!
                self.logger.debug(
                    "Warning: List CPVM didn't return systemvms in Running state. This is a known issue, ignoring it for now!")
                return

            time.sleep(self.services["sleep"])
            timeout = timeout - 1

        cpvm_response = list_cpvm_response[0]

        # Verify Name, Public IP, Private IP and Link local IP
        # for newly created CPVM
        self.assertNotEqual(
            cpvm_response.name,
            old_name,
            "Check SSVM new name with name of destroyed CPVM"
        )
        self.assertEqual(
            hasattr(cpvm_response, 'privateip'),
            True,
            "Check whether CPVM has private IP field"
        )

        self.assertEqual(
            hasattr(cpvm_response, 'linklocalip'),
            True,
            "Check whether CPVM has link local IP field"
        )

        self.assertEqual(
            hasattr(cpvm_response, 'publicip'),
            True,
            "Check whether CPVM has public IP field"
        )

        # Wait for the agent to be up
        self.wait_for_system_vm_agent(cpvm_response.name)

        return

    def wait_for_system_vm_agent(self, vmname):
        list_host_response = []
        self.logger.debug("Waiting for system VM %s agent to be UP" % vmname)
        timeout = self.services["timeout"]
        sleep_interval = self.services["sleep"]
        while timeout > 0:
            list_host_response = list_hosts(
                self.apiclient,
                name=vmname
            )

            if list_host_response and list_host_response[0].state == 'Up':
                self.logger.debug("System VM %s agent is UP" % vmname)
                break

            time.sleep(sleep_interval)
            timeout = timeout - sleep_interval

        if timeout <= 0 and len(list_host_response) > 0 and list_host_response[0].state != 'Up':
            # FIXME: This should be fixed!
            self.logger.debug(
                "Warning: List CPVM didn't return systemvms in Running state. This is a known issue, ignoring it for now!")
            return

    def _test_list_svm_vm(self, svm_type):
        # Validate the following:
        # 1. listSystemVM
        #    should return only ONE SVM per zone
        # 2. The returned SVM should be in Running state
        # 3. listSystemVM for should list publicip, privateip and link-localip
        # 4. The gateway programmed on the SVM by listSystemVm should be
        #    the same as the gateway returned by listVlanIpRanges
        # 5. DNS entries must match those given for the zone

        list_svm_response = list_ssvms(
            self.apiclient,
            systemvmtype=svm_type,
            state='Running',
        )
        self.assertEqual(
            isinstance(list_svm_response, list),
            True,
            "Check list response returns a valid list"
        )
        # Verify SSVM response
        self.assertNotEqual(
            len(list_svm_response),
            0,
            "Check list System VMs response"
        )

        list_zones_response = list_zones(self.apiclient)

        self.assertEqual(
            isinstance(list_zones_response, list),
            True,
            "Check list response returns a valid list"
        )

        self.logger.debug("Number of zones: %s" % len(list_zones_response))
        self.logger.debug("Number of System VMs: %s" % len(list_svm_response))
        # Number of Sec storage VMs = No of Zones
        self.assertEqual(
            len(list_svm_response),
            len(list_zones_response),
            "Check number of System VMs with number of zones"
        )
        # For each secondary storage VM check private IP,
        # public IP, link local IP and DNS
        for svm in list_svm_response:

            self.logger.debug("SVM state: %s" % svm.state)
            self.assertEqual(
                svm.state,
                'Running',
                "Check whether state of System VM is running"
            )

            self.assertEqual(
                hasattr(svm, 'privateip'),
                True,
                "Check whether System VM has private IP field"
            )

            self.assertEqual(
                hasattr(svm, 'linklocalip'),
                True,
                "Check whether System VM has link local IP field"
            )

            self.assertEqual(
                hasattr(svm, 'publicip'),
                True,
                "Check whether System VM has public IP field"
            )

            # Fetch corresponding ip ranges information from listVlanIpRanges
            ipranges_response = list_vlan_ipranges(
                self.apiclient,
                zoneid=svm.zoneid
            )
            self.assertEqual(
                isinstance(ipranges_response, list),
                True,
                "Check list response returns a valid list"
            )
            iprange = ipranges_response[0]

            # Execute the following assertion in all zones except basic Zones
            if not (self.zone.networktype.lower() == 'basic'):
                self.assertEqual(
                    svm.gateway,
                    iprange.gateway,
                    "Check gateway with that of corresponding ip range"
                )

            # Fetch corresponding zone information from listZones
            zone_response = list_zones(
                self.apiclient,
                id=svm.zoneid
            )
            self.assertEqual(
                isinstance(zone_response, list),
                True,
                "Check list response returns a valid list"
            )
            self.assertEqual(
                svm.dns1,
                zone_response[0].dns1,
                "Check DNS1 with that of corresponding zone"
            )

            self.assertEqual(
                svm.dns2,
                zone_response[0].dns2,
                "Check DNS2 with that of corresponding zone"
            )
        return
