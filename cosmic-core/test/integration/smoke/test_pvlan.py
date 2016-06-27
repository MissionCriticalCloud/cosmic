""" test for private vlan isolation
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


class TestPVLAN(cloudstackTestCase):
    zoneId = 1
    networkOfferingId = 7
    vlan = 1234
    isolatedpvlan = 567

    def setUp(self):
        self.apiClient = self.testClient.getApiClient()

    @attr(tags=["advanced"], required_hardware="false")
    def test_create_pvlan_network(self):
        self.debug("Test create pvlan network")
        createNetworkCmd = createNetwork.createNetworkCmd()
        createNetworkCmd.name = "pvlan network"
        createNetworkCmd.displaytext = "pvlan network"
        createNetworkCmd.netmask = "255.255.255.0"
        createNetworkCmd.gateway = "10.10.10.1"
        createNetworkCmd.startip = "10.10.10.10"
        createNetworkCmd.gateway = "10.10.10.20"
        createNetworkCmd.vlan = "1234"
        createNetworkCmd.isolatedpvlan = "567"
        createNetworkCmd.zoneid = self.zoneId
        createNetworkCmd.networkofferingid = self.networkOfferingId
        createNetworkResponse = self.apiClient.createNetwork(createNetworkCmd)
        self.networkId = createNetworkResponse.id
        self.broadcasttype = createNetworkResponse.broadcastdomaintype
        self.broadcasturi = createNetworkResponse.broadcasturi

        self.assertIsNotNone(createNetworkResponse.id, "Network failed to create")
        self.assertTrue(createNetworkResponse.broadcastdomaintype, "Pvlan")
        self.assertTrue(createNetworkResponse.broadcasturi, "pvlan://1234-i567")

        self.debug("Clean up test pvlan network")
        deleteNetworkCmd = deleteNetwork.deleteNetworkCmd()
        deleteNetworkCmd.id = self.networkId;
        self.apiClient.deleteNetwork(deleteNetworkCmd)

        # Test invalid parameter

        # CLOUDSTACK-2392: Should not allow create pvlan with ipv6
        createNetworkCmd.ip6gateway = "fc00:1234::1"
        createNetworkCmd.ip6cidr = "fc00:1234::/64"
        createNetworkCmd.startipv6 = "fc00:1234::10"
        createNetworkCmd.endipv6 = "fc00:1234::20"
        err = 0
        with self.assertRaises(Exception):
            self.apiClient.createNetwork(createNetworkCmd)
