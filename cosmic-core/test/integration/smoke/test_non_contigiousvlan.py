# from marvin.cloudstackAPI import *
from marvin.cloudstackTestCase import cloudstackTestCase
from marvin.lib.base import PhysicalNetwork
from marvin.lib.common import setNonContiguousVlanIds, get_zone
from nose.plugins.attrib import attr


class TestUpdatePhysicalNetwork(cloudstackTestCase):
    """
    Test to extend physical network vlan range
    """

    def setUp(self):
        self.apiClient = self.testClient.getApiClient()
        self.zone = get_zone(self.apiClient, self.testClient.getZoneForTests())
        self.physicalnetwork, self.vlan = setNonContiguousVlanIds(self.apiClient, self.zone.id)

        self.physicalnetworkid = self.physicalnetwork.id
        self.existing_vlan = self.physicalnetwork.vlan

        if self.vlan is None:
            raise Exception("Failed to set non contiguous vlan ids to test. Free some ids from \
                        from existing physical networks at ends")

    @attr(tags=["advanced"], required_hardware="false")
    def test_extendPhysicalNetworkVlan(self):
        """
        Test to update a physical network and extend its vlan
        """
        phy_networks = PhysicalNetwork.list(self.apiClient)
        self.assertNotEqual(len(phy_networks), 0,
                            msg="There are no physical networks in the zone")

        self.network = phy_networks[0]
        self.networkid = phy_networks[0].id
        self.existing_vlan = phy_networks[0].vlan
        vlan1 = self.existing_vlan + "," + self.vlan["partial_range"][0]
        updatePhysicalNetworkResponse = self.network.update(self.apiClient, id=self.networkid, vlan=vlan1)
        self.assert_(updatePhysicalNetworkResponse is not None,
                     msg="couldn't extend the physical network with vlan %s" % vlan1)
        self.assert_(isinstance(self.network, PhysicalNetwork))

        vlan2 = vlan1 + "," + self.vlan["partial_range"][1]
        updatePhysicalNetworkResponse2 = self.network.update(self.apiClient, id=self.networkid, vlan=vlan2)
        self.assert_(updatePhysicalNetworkResponse2 is not None,
                     msg="couldn't extend the physical network with vlan %s" % vlan2)
        self.assert_(isinstance(self.network, PhysicalNetwork))

        vlanranges = updatePhysicalNetworkResponse2.vlan
        self.assert_(vlanranges is not None,
                     "No VLAN ranges found on the deployment")
        self.assert_(str(vlanranges) == vlan2, "vlan ranges are not extended")

    def tearDown(self):
        """
        Teardown to update a physical network and shrink its vlan
        @return:
        """
        phy_networks = PhysicalNetwork.list(self.apiClient)
        self.assertNotEqual(len(phy_networks), 0,
                            msg="There are no physical networks in the zone")
        self.network = phy_networks[0]
        self.networkid = phy_networks[0].id
        updateResponse = self.network.update(self.apiClient, id=self.networkid, vlan=self.existing_vlan)
        self.assert_(updateResponse.vlan.find(self.vlan["full_range"]) < 0,
                     "VLAN was not removed successfully")
