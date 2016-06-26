""" P1 tests for Scaling up Vm
"""
# Import Local Modules
from marvin.cloudstackAPI import *
from marvin.cloudstackTestCase import *
from marvin.lib.base import *
from marvin.lib.common import *
from marvin.lib.utils import *
from nose.plugins.attrib import attr

# Import System modules

_multiprocess_shared_ = True


class TestResourceDetail(cloudstackTestCase):
    @classmethod
    def setUpClass(cls):
        testClient = super(TestResourceDetail, cls).getClsTestClient()
        cls.apiclient = testClient.getApiClient()
        cls.services = testClient.getParsedTestDataConfig()

        # Get Zone, Domain and templates
        domain = get_domain(cls.apiclient)
        zone = get_zone(cls.apiclient, testClient.getZoneForTests())
        cls.services['mode'] = zone.networktype

        # Set Zones and disk offerings ??

        # Create account, service offerings, vm.
        cls.account = Account.create(
            cls.apiclient,
            cls.services["account"],
            domainid=domain.id
        )

        cls.disk_offering = DiskOffering.create(
            cls.apiclient,
            cls.services["disk_offering"]
        )

        # create a volume
        cls.volume = Volume.create(
            cls.apiclient,
            { "diskname": "ndm" },
            zoneid=zone.id,
            account=cls.account.name,
            domainid=cls.account.domainid,
            diskofferingid=cls.disk_offering.id
        )
        # how does it work ??
        cls._cleanup = [
            cls.volume,
            cls.account
        ]

    @classmethod
    def tearDownClass(cls):
        cls.apiclient = super(TestResourceDetail, cls).getClsTestClient().getApiClient()
        cleanup_resources(cls.apiclient, cls._cleanup)
        return

    def setUp(self):
        self.apiclient = self.testClient.getApiClient()
        self.dbclient = self.testClient.getDbConnection()
        self.cleanup = []

    def tearDown(self):
        # Clean up, terminate the created ISOs
        cleanup_resources(self.apiclient, self.cleanup)
        return

    @attr(tags=["advanced", "xenserver"], required_hardware="false")
    def test_01_updatevolumedetail(self):
        """Test volume detail
        """
        # Validate the following


        # remove detail
        self.debug("Testing REMOVE volume detail Volume-ID: %s " % (
            self.volume.id
        ))
        cmd = removeResourceDetail.removeResourceDetailCmd()
        cmd.resourcetype = "Volume"
        cmd.resourceid = self.volume.id
        self.apiclient.removeResourceDetail(cmd)

        listResourceDetailCmd = listResourceDetails.listResourceDetailsCmd()
        listResourceDetailCmd.resourceid = self.volume.id
        listResourceDetailCmd.resourcetype = "Volume"
        listResourceDetailResponse = self.apiclient.listResourceDetails(listResourceDetailCmd)

        self.assertEqual(listResourceDetailResponse, None, "Check if the list API \
                            returns an empty response")

        # TODO - add detail. Map as input

        return
