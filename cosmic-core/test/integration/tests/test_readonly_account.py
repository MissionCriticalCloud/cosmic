from cs import CloudStackApiException

from cosmic.base import *
from cosmic.common import *
from cosmic.cosmicLog import CosmicLog
from cosmic.cosmicTestCase import cosmicTestCase


class TestReadOnlyAccount(cosmicTestCase):
    def setUp(self):
        self.logger = CosmicLog(CosmicLog.LOGGER_TEST).get_logger()
        self.testClient = self.getClsTestClient()
        self.apiclient = self.testClient.getApiClient()
        self.services = self.testClient.getParsedTestDataConfig()

        # Get Zone, Domain and templates
        self.domain = get_domain(self.apiclient)
        self.zone = get_zone(self.apiclient, self.testClient.getZoneForTests())
        self.services["virtual_machine"]["zoneid"] = self.zone.id

        # Create an account with normal domain admin rights
        self.account_admin = Account.create(
            self.apiclient,
            self.services["account"],
            admin=True,
            domainid=self.domain.id
        )

        # Create an account with readonly domain admin rights
        self.services["account"]["accounttype"] = 4
        self.account_ro_admin = Account.create(
            self.apiclient,
            self.services["account"],
            admin=True,
            domainid=self.domain.id
        )

        self.user_admin = User.registerUserKeys(self.apiclient, self.account_admin.user[0].id)
        self.user_ro_admin = User.registerUserKeys(self.apiclient, self.account_ro_admin.user[0].id)

        self.cleanup = [self.account_admin, self.account_ro_admin]
        return

    def tearDown(self):
        cleanup_resources(self.apiclient, self.cleanup)
        return

    @attr(tags=['advanced'])
    def test_01_checkApiCallsRoAdmin(self):
        """Test which API calls are available"""
        response = None

        mgmt_details = self.testClient.getMgmtDetails()

        self.testClient.setMgmtDetails(api_key=self.user_admin.apikey, secret_key=self.user_admin.secretkey, port=8080)
        self.testClient.createTestClient()
        api_client = self.testClient.getApiClient()
        try:
            response = api_client.listApis()
        except CloudStackApiException:
            self.logger.debug("List APIs")
        except Exception as e:
            self.logger.debug("Exception %s raised listing API calls " % e)

        self.assertFalse(
            all(x['name'].startswith(("list", "login", "logout")) for x in response['api']),
            "Only API's calls containing list/login/logout are shown"
        )

        self.testClient.setMgmtDetails(api_key=mgmt_details.apiKey, secret_key=mgmt_details.secretKey)
        self.testClient.createTestClient()
        return

    @attr(tags=['advanced'])
    def test_02_checkApiCallsAdmin(self):
        """Test which API RO calls are available"""
        response = None

        mgmt_details = self.testClient.getMgmtDetails()

        self.testClient.setMgmtDetails(api_key=self.user_ro_admin.apikey, secret_key=self.user_ro_admin.secretkey, port=8080)
        self.testClient.createTestClient()
        api_client = self.testClient.getApiClient()
        try:
            response = api_client.listApis()
        except CloudStackApiException:
            self.logger.debug("List APIs")
        except Exception as e:
            self.logger.debug("Exception %s raised listing API calls " % e)

        self.assertTrue(
            all(x['name'].startswith(("list", "login", "logout")) for x in response['api']),
            "List API's contains calls other then list/login/logout"
        )

        self.testClient.setMgmtDetails(api_key=mgmt_details.apiKey, secret_key=mgmt_details.secretKey)
        self.testClient.createTestClient()
        return
