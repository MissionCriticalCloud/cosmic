package com.cloud.hypervisor.kvm.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource.BridgeType;
import com.cloud.network.Networks.TrafficType;

import javax.naming.ConfigurationException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class LibvirtVifDriverTest {
    final String LibVirtVifDriver = "libvirt.vif.driver";
    final String FakeVifDriverClassName = "com.cloud.hypervisor.kvm.resource.FakeVifDriver";
    final String NonExistentVifDriverClassName = "com.cloud.hypervisor.kvm.resource.NonExistentVifDriver";
    private LibvirtComputingResource res;
    private Map<TrafficType, VifDriver> assertions;
    private VifDriver fakeVifDriver, bridgeVifDriver, ovsVifDriver;

    @Before
    public void setUp() {
        // Use a spy because we only want to override getVifDriverClass
        final LibvirtComputingResource resReal = new LibvirtComputingResource();
        res = spy(resReal);

        try {
            bridgeVifDriver = (VifDriver) Class.forName(
                    LibvirtComputingResource.DEFAULT_BRIDGE_VIF_DRIVER_CLASS).newInstance();
            ovsVifDriver = (VifDriver) Class.forName(
                    LibvirtComputingResource.DEFAULT_OVS_VIF_DRIVER_CLASS).newInstance();

            // Instantiating bridge vif driver again as the fake vif driver
            // is good enough, as this is a separate instance
            fakeVifDriver = (VifDriver) Class.forName(
                    LibvirtComputingResource.DEFAULT_BRIDGE_VIF_DRIVER_CLASS).newInstance();

            doReturn(bridgeVifDriver).when(res).getVifDriverClass(
                    eq(LibvirtComputingResource.DEFAULT_BRIDGE_VIF_DRIVER_CLASS), anyMap());
            doReturn(ovsVifDriver).when(res).getVifDriverClass(eq(LibvirtComputingResource.DEFAULT_OVS_VIF_DRIVER_CLASS),
                    anyMap());
            doReturn(fakeVifDriver).when(res).getVifDriverClass(eq(FakeVifDriverClassName), anyMap());
        } catch (final ConfigurationException ex) {
            fail("Unexpected ConfigurationException while configuring VIF drivers: " + ex.getMessage());
        } catch (final Exception ex) {
            fail("Unexpected Exception while configuring VIF drivers");
        }

        assertions = new HashMap<>();
    }

    @Test
    public void testDefaults() throws ConfigurationException {
        // If no special vif driver settings, all traffic types should
        // map to the default vif driver for the bridge type
        final Map<String, Object> params = new HashMap<>();

        res.bridgeType = BridgeType.NATIVE;
        configure(params);
        checkAllSame(bridgeVifDriver);

        res.bridgeType = BridgeType.OPENVSWITCH;
        configure(params);
        checkAllSame(ovsVifDriver);
    }

    // Helper function
    // Configure LibvirtComputingResource using params
    private void configure(final Map<String, Object> params) throws ConfigurationException {
        res.configureVifDrivers(params);
    }

    // Helper when all answers should be the same
    private void checkAllSame(final VifDriver vifDriver) throws ConfigurationException {

        for (final TrafficType trafficType : TrafficType.values()) {
            assertions.put(trafficType, vifDriver);
        }

        checkAssertions();
    }

    // Helper function
    private void checkAssertions() {
        // Check the defined assertions
        for (final Map.Entry<TrafficType, VifDriver> assertion : assertions.entrySet()) {
            assertEquals(res.getVifDriver(assertion.getKey()), assertion.getValue());
        }
    }

    @Test
    public void testDefaultsWhenExplicitlySet() throws ConfigurationException {

        final Map<String, Object> params = new HashMap<>();

        // Switch res' bridge type for test purposes
        params.put(LibVirtVifDriver, LibvirtComputingResource.DEFAULT_BRIDGE_VIF_DRIVER_CLASS);
        res.bridgeType = BridgeType.NATIVE;
        configure(params);
        checkAllSame(bridgeVifDriver);

        params.clear();
        params.put(LibVirtVifDriver, LibvirtComputingResource.DEFAULT_OVS_VIF_DRIVER_CLASS);
        res.bridgeType = BridgeType.OPENVSWITCH;
        configure(params);
        checkAllSame(ovsVifDriver);
    }

    @Test
    public void testWhenExplicitlySetDifferentDefault() throws ConfigurationException {

        // Tests when explicitly set vif driver to OVS when using regular bridges and vice versa
        final Map<String, Object> params = new HashMap<>();

        // Switch res' bridge type for test purposes
        params.put(LibVirtVifDriver, LibvirtComputingResource.DEFAULT_OVS_VIF_DRIVER_CLASS);
        res.bridgeType = BridgeType.NATIVE;
        configure(params);
        checkAllSame(ovsVifDriver);

        params.clear();
        params.put(LibVirtVifDriver, LibvirtComputingResource.DEFAULT_BRIDGE_VIF_DRIVER_CLASS);
        res.bridgeType = BridgeType.OPENVSWITCH;
        configure(params);
        checkAllSame(bridgeVifDriver);
    }

    @Test
    public void testOverrideSomeTrafficTypes() throws ConfigurationException {

        final Map<String, Object> params = new HashMap<>();
        params.put(LibVirtVifDriver + "." + "Public", FakeVifDriverClassName);
        params.put(LibVirtVifDriver + "." + "Guest", LibvirtComputingResource.DEFAULT_OVS_VIF_DRIVER_CLASS);
        res.bridgeType = BridgeType.NATIVE;
        configure(params);

        // Initially, set all traffic types to use default
        for (final TrafficType trafficType : TrafficType.values()) {
            assertions.put(trafficType, bridgeVifDriver);
        }

        assertions.put(TrafficType.Public, fakeVifDriver);
        assertions.put(TrafficType.Guest, ovsVifDriver);

        checkAssertions();
    }

    @Test
    public void testBadTrafficType() throws ConfigurationException {
        final Map<String, Object> params = new HashMap<>();
        params.put(LibVirtVifDriver + "." + "NonExistentTrafficType", FakeVifDriverClassName);
        res.bridgeType = BridgeType.NATIVE;
        configure(params);

        // Set all traffic types to use default, because bad traffic type should be ignored
        for (final TrafficType trafficType : TrafficType.values()) {
            assertions.put(trafficType, bridgeVifDriver);
        }

        checkAssertions();
    }

    @Test
    public void testEmptyTrafficType() throws ConfigurationException {
        final Map<String, Object> params = new HashMap<>();
        params.put(LibVirtVifDriver + ".", FakeVifDriverClassName);
        res.bridgeType = BridgeType.NATIVE;
        configure(params);

        // Set all traffic types to use default, because bad traffic type should be ignored
        for (final TrafficType trafficType : TrafficType.values()) {
            assertions.put(trafficType, bridgeVifDriver);
        }

        checkAssertions();
    }

    @Test(expected = ConfigurationException.class)
    public void testBadVifDriverClassName() throws ConfigurationException {
        final Map<String, Object> params = new HashMap<>();
        params.put(LibVirtVifDriver + "." + "Public", NonExistentVifDriverClassName);
        res.bridgeType = BridgeType.NATIVE;
        configure(params);
    }
}
