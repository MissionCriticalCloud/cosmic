import time

from nose.plugins.attrib import attr

from marvin.cloudstackException import CloudstackAPIException
from marvin.cloudstackTestCase import cloudstackTestCase
from marvin.lib.base import (
    Account,
    LoadBalancerRule,
    NATRule,
    Network,
    User,
    VirtualMachine,
    VPC,

)
from marvin.lib.common import (
    list_routers,
    list_nat_rules,
    list_lb_rules,
    list_configurations,
    list_public_ip,
    get_template,
    get_zone,
    get_domain,
    get_default_network_offering,
    get_default_virtual_machine_offering,
    get_default_vpc_offering,
    get_network_acl
)
from marvin.lib.utils import cleanup_resources
from marvin.utils.MarvinLog import MarvinLog
from marvin.cloudstackAPI import (
    listApis,
    registerUserKeys
)

import traceback
from pprint import pprint

class TestReadOnlyAccount(cloudstackTestCase):
    def setUp(self):
        self.logger = MarvinLog(MarvinLog.LOGGER_TEST).get_logger()

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

        regusrkey_cmd = registerUserKeys.registerUserKeysCmd()
        regusrkey_cmd.id = self.account_admin.user[0].id
        self.user_admin = self.apiclient.registerUserKeys(regusrkey_cmd)

        regusrkey_cmd = registerUserKeys.registerUserKeysCmd()
        regusrkey_cmd.id = self.account_ro_admin.user[0].id
        self.user_ro_admin = self.apiclient.registerUserKeys(regusrkey_cmd)

        self.cleanup = [self.account_admin, self.account_ro_admin]
        return

    def tearDown(self):
        cleanup_resources(self.apiclient, self.cleanup)
        return

    @attr(tags=['advanced'])
    def test_01_checkApiCallsRoAdmin(self):
        """Test which API calls are available"""
        response = None

        apikey = self.apiclient.connection.apiKey
        secretkey = self.apiclient.connection.securityKey

        self.apiclient.connection.apiKey = self.user_admin.apikey
        self.apiclient.connection.securityKey = self.user_admin.secretkey
        try:
            apis_cmd = listApis.listApisCmd()
            response = self.apiclient.listApis(apis_cmd)
        except CloudstackAPIException:
            self.logger.debug("List APIs")
        except Exception as e:
            self.logger.debug("Exception %s raised listing API calls " % e)


        self.assertFalse(
            all(x.name.startswith(("list", "login", "logout")) for x in response),
            "List API's contains calls other then list/login/logout"
        )

        self.apiclient.connection.apiKey = apikey
        self.apiclient.connection.securityKey = secretkey
        return

    @attr(tags=['advanced'])
    def test_02_checkApiCallsAdmin(self):
        """Test which API RO calls are available"""
        response = None

        apikey = self.apiclient.connection.apiKey
        secretkey = self.apiclient.connection.securityKey

        self.apiclient.connection.apiKey = self.user_ro_admin.apikey
        self.apiclient.connection.securityKey = self.user_ro_admin.secretkey
        try:
            apis_cmd = listApis.listApisCmd()
            response = self.apiclient.listApis(apis_cmd)
        except CloudstackAPIException:
            self.logger.debug("List APIs")
        except Exception as e:
            self.logger.debug("Exception %s raised listing API calls " % e)


        self.assertTrue(
            all(x.name.startswith(("list", "login", "logout")) for x in response),
            "List API's contains calls other then list/login/logout"
        )

        self.apiclient.connection.apiKey = apikey
        self.apiclient.connection.securityKey = secretkey
        return
