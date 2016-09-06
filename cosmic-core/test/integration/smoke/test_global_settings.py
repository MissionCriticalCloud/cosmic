""" P1 tests for updating the granular Configuration parameter with scope and resource id provided.
"""
# Import Local Modules
from marvin.cloudstackAPI import *
from marvin.cloudstackTestCase import *
from marvin.lib.base import *
from marvin.lib.common import *
from marvin.lib.utils import *
from nose.plugins.attrib import attr


# Import System modules

class TestUpdateConfigWithScope(cloudstackTestCase):
    """
    Test to update a configuration (global setting) at various scopes
    """

    def setUp(self):
        self.apiClient = self.testClient.getApiClient()

    @attr(tags=["basic", "advanced"], required_hardware="false")
    def test_UpdateConfigParamWithScope(self):
        """
        test update configuration setting at zone level scope
        @return:
        """
        updateConfigurationCmd = updateConfiguration.updateConfigurationCmd()
        updateConfigurationCmd.name = "use.external.dns"
        updateConfigurationCmd.value = "true"
        updateConfigurationCmd.scopename = "zone"
        updateConfigurationCmd.scopeid = 1

        updateConfigurationResponse = self.apiClient.updateConfiguration(updateConfigurationCmd)
        self.debug("updated the parameter %s with value %s" % (updateConfigurationResponse.name, updateConfigurationResponse.value))

        listConfigurationsCmd = listConfigurations.listConfigurationsCmd()
        listConfigurationsCmd.cfgName = updateConfigurationResponse.name
        listConfigurationsCmd.scopename = "zone"
        listConfigurationsCmd.scopeid = 1
        listConfigurationsResponse = self.apiClient.listConfigurations(listConfigurationsCmd)

        self.assertNotEqual(len(listConfigurationsResponse), 0, "Check if the list API \
                            returns a non-empty response")

        for item in listConfigurationsResponse:
            if item.name == updateConfigurationResponse.name:
                configParam = item

        self.assertEqual(configParam.value, updateConfigurationResponse.value, "Check if the update API returned \
                         is the same as the one we got in the list API")

    def tearDown(self):
        """
        Reset the configuration back to false
        @return:
        """
        updateConfigurationCmd = updateConfiguration.updateConfigurationCmd()
        updateConfigurationCmd.name = "use.external.dns"
        updateConfigurationCmd.value = "false"
        updateConfigurationCmd.scopename = "zone"
        updateConfigurationCmd.scopeid = 1
        self.apiClient.updateConfiguration(updateConfigurationCmd)
