import factory
from marvin import cloudstackTestCase
from marvin.lib import utils
from marvin.lib.base import *


class AccountFactory(factory.Factory):
    FACTORY_FOR = createAccount.createAccountCmd

    firstname = 'firstname-' + random_gen()
    lastname = 'lastname-' + random_gen()
    email = factory.lazy_attribute(lambda e: '{0}.{1}@cloudstack.org'.format(e.firstname, e.lastname).lower())


class AdminAccountFactory(AccountFactory):
    accounttype = 1


class UserAccountFactory(AccountFactory):
    accounttype = 0


class TestCreateAccount(cloudstackTestCase):
    def setUp(self):
        self.apiClient = self.testClient.getApiClient()
        self.userApiClient = self.testClient.getUserApiClient(account='test' + utils.random_gen(), 'ROOT')

    def test_createAccountAsAdmin(self):
        """
        creates an account for a user as admin
        """
        Account.create(self.apiClient, services=None)
        self.assertEqual(True, False)

    def test_createAccountAsUser(self):
        """
        negative: create account as a user
        """
        self.assertEqual(True, False)

    def tearDown(self):
        self.apiClient.close()
        self.userApiClient.close()


if __name__ == '__main__':
    unittest.main()
