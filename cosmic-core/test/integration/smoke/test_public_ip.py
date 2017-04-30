import time

from nose.plugins.attrib import attr

from marvin.cloudstackTestCase import cloudstackTestCase
from marvin.lib.base import (
    PublicIPAddress,
    Network,
    Account
)
from marvin.lib.common import (
    list_public_ip,
    get_zone,
    get_domain,
    get_default_guest_network_offering
)
from marvin.lib.utils import cleanup_resources
from marvin.utils.MarvinLog import MarvinLog


class TestPublicIP(cloudstackTestCase):
    def setUp(self):
        self.api_client = self.testClient.getApiClient()

    @classmethod
    def setUpClass(cls):
        cls.logger = MarvinLog(MarvinLog.LOGGER_TEST).get_logger()

        testClient = super(TestPublicIP, cls).getClsTestClient()
        cls.api_client = testClient.getApiClient()
        cls.services = testClient.getParsedTestDataConfig()

        # Get Zone, Domain and templates
        cls.domain = get_domain(cls.api_client)
        cls.zone = get_zone(cls.api_client, testClient.getZoneForTests())
        cls.services['mode'] = cls.zone.networktype
        # Create Accounts & networks
        cls.account = Account.create(
            cls.api_client,
            cls.services["account"],
            admin=True,
            domainid=cls.domain.id
        )

        cls.user = Account.create(
            cls.api_client,
            cls.services["account"],
            domainid=cls.domain.id
        )
        cls.services["network"]["zoneid"] = cls.zone.id

        cls.network_offering = get_default_guest_network_offering(cls.api_client)

        cls.services["network"]["networkoffering"] = cls.network_offering.id

        cls.account_network = Network.create(
            cls.api_client,
            cls.services["network"],
            cls.account.name,
            cls.account.domainid
        )
        cls.user_network = Network.create(
            cls.api_client,
            cls.services["network"],
            cls.user.name,
            cls.user.domainid
        )

        # Create Source NAT IP addresses
        PublicIPAddress.create(
            cls.api_client,
            cls.account.name,
            cls.zone.id,
            cls.account.domainid
        )
        PublicIPAddress.create(
            cls.api_client,
            cls.user.name,
            cls.zone.id,
            cls.user.domainid
        )
        cls._cleanup = [
            cls.account_network,
            cls.user_network,
            cls.account,
            cls.user
        ]
        return

    @classmethod
    def tearDownClass(cls):
        try:
            # Cleanup resources used
            cleanup_resources(cls.api_client, cls._cleanup)
        except Exception as e:
            raise Exception("Warning: Exception during cleanup : %s" % e)
        return

    @attr(tags=['advanced'])
    def test_01_public_ip_admin_account(self):
        """Test for Associate/Disassociate public IP address for admin account"""

        # Validate the following:
        # 1. listPubliIpAddresses API returns the list of acquired addresses
        # 2. the returned list should contain our acquired IP address

        ip_address = PublicIPAddress.create(
            self.api_client,
            self.account.name,
            self.zone.id,
            self.account.domainid
        )
        list_pub_ip_addr_resp = list_public_ip(
            self.api_client,
            id=ip_address.ipaddress.id
        )
        self.assertEqual(
            isinstance(list_pub_ip_addr_resp, list),
            True,
            "Check list response returns a valid list"
        )
        # listPublicIpAddresses should return newly created public IP
        self.assertNotEqual(
            len(list_pub_ip_addr_resp),
            0,
            "Check if new IP Address is associated"
        )
        self.assertEqual(
            list_pub_ip_addr_resp[0].id,
            ip_address.ipaddress.id,
            "Check Correct IP Address is returned in the List Cacls"
        )

        ip_address.delete(self.api_client)
        time.sleep(30)

        # Validate the following:
        # 1.listPublicIpAddresses should no more return the released address
        list_pub_ip_addr_resp = list_public_ip(
            self.api_client,
            id=ip_address.ipaddress.id
        )
        if list_pub_ip_addr_resp is None:
            return
        if (list_pub_ip_addr_resp) and (
                isinstance(
                    list_pub_ip_addr_resp,
                    list)) and (
                    len(list_pub_ip_addr_resp) > 0):
            self.fail("list public ip response is not empty")
        return

    @attr(tags=['advanced'])
    def test_02_public_ip_user_account(self):
        """Test for Associate/Disassociate public IP address for user account"""

        # Validate the following:
        # 1. listPublicIpAddresses API returns the list of acquired addresses
        # 2. the returned list should contain our acquired IP address

        ip_address = PublicIPAddress.create(
            self.api_client,
            self.user.name,
            self.zone.id,
            self.user.domainid
        )

        # listPublicIpAddresses should return newly created public IP
        list_pub_ip_addr_resp = list_public_ip(
            self.api_client,
            id=ip_address.ipaddress.id
        )
        self.assertEqual(
            isinstance(list_pub_ip_addr_resp, list),
            True,
            "Check list response returns a valid list"
        )
        self.assertNotEqual(
            len(list_pub_ip_addr_resp),
            0,
            "Check if new IP Address is associated"
        )
        self.assertEqual(
            list_pub_ip_addr_resp[0].id,
            ip_address.ipaddress.id,
            "Check Correct IP Address is returned in the List Call"
        )

        ip_address.delete(self.api_client)

        list_pub_ip_addr_resp = list_public_ip(
            self.api_client,
            id=ip_address.ipaddress.id
        )

        self.assertEqual(
            list_pub_ip_addr_resp,
            None,
            "Check if disassociated IP Address is no longer available"
        )
        return
