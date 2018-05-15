package com.cloud.agent.resource.kvm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import com.cloud.agent.resource.kvm.LibvirtComputingResource.BridgeType;
import com.cloud.agent.resource.kvm.vif.VifDriver;
import com.cloud.model.enumeration.TrafficType;

import javax.naming.ConfigurationException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class LibvirtVifDriverTest {
    private final String LibVirtVifDriver = "libvirt.vif.driver";
    private final String FakeVifDriverClassName = "com.cloud.agent.resource.kvm.FakeVifDriver";
    private LibvirtComputingResource res;
    private Map<TrafficType, VifDriver> assertions;
    private VifDriver fakeVifDriver, bridgeVifDriver, ovsVifDriver;

    @Before
    public void setUp() {
        // Use a spy because we only want to override getVifDriverClass
        final LibvirtComputingResource resReal = new LibvirtComputingResource();
        this.res = spy(resReal);

        try {
            this.bridgeVifDriver = (VifDriver) Class.forName(LibvirtComputingResource.DEFAULT_BRIDGE_VIF_DRIVER_CLASS).newInstance();
            this.ovsVifDriver = (VifDriver) Class.forName(LibvirtComputingResource.DEFAULT_OVS_VIF_DRIVER_CLASS).newInstance();

            // Instantiating bridge vif driver again as the fake vif driver is good enough, as this is a separate instance
            this.fakeVifDriver = (VifDriver) Class.forName(LibvirtComputingResource.DEFAULT_BRIDGE_VIF_DRIVER_CLASS).newInstance();

            doReturn(this.bridgeVifDriver).when(this.res).getVifDriverClass(eq(LibvirtComputingResource.DEFAULT_BRIDGE_VIF_DRIVER_CLASS), anyMap());
            doReturn(this.ovsVifDriver).when(this.res).getVifDriverClass(eq(LibvirtComputingResource.DEFAULT_OVS_VIF_DRIVER_CLASS), anyMap());
            doReturn(this.fakeVifDriver).when(this.res).getVifDriverClass(eq(this.FakeVifDriverClassName), anyMap());
        } catch (final ConfigurationException ex) {
            fail("Unexpected ConfigurationException while configuring VIF drivers: " + ex.getMessage());
        } catch (final Exception ex) {
            fail("Unexpected Exception while configuring VIF drivers" + ex.getMessage());
        }

        this.assertions = new HashMap<>();
    }

    @Test
    public void testDefaults() throws ConfigurationException {
        // If no special vif driver settings, all traffic types should
        // map to the default vif driver for the bridge type
        final Map<String, Object> params = new HashMap<>();

        this.res.setBridgeType(BridgeType.NATIVE);
        configure(params);
        checkAllSame(this.bridgeVifDriver);

        this.res.setBridgeType(BridgeType.OPENVSWITCH);
        configure(params);
        checkAllSame(this.ovsVifDriver);
    }

    // Helper function
    // Configure LibvirtComputingResource using params
    private void configure(final Map<String, Object> params) throws ConfigurationException {
        this.res.configureVifDrivers(params);
    }

    // Helper when all answers should be the same
    private void checkAllSame(final VifDriver vifDriver) throws ConfigurationException {

        for (final TrafficType trafficType : TrafficType.values()) {
            this.assertions.put(trafficType, vifDriver);
        }

        checkAssertions();
    }

    // Helper function
    private void checkAssertions() {
        // Check the defined assertions
        for (final Map.Entry<TrafficType, VifDriver> assertion : this.assertions.entrySet()) {
            assertEquals(this.res.getVifDriver(assertion.getKey()), assertion.getValue());
        }
    }

    @Test
    public void testDefaultsWhenExplicitlySet() throws ConfigurationException {

        final Map<String, Object> params = new HashMap<>();

        // Switch res' bridge type for test purposes
        params.put(this.LibVirtVifDriver, LibvirtComputingResource.DEFAULT_BRIDGE_VIF_DRIVER_CLASS);
        this.res.setBridgeType(BridgeType.NATIVE);
        configure(params);
        checkAllSame(this.bridgeVifDriver);

        params.clear();
        params.put(this.LibVirtVifDriver, LibvirtComputingResource.DEFAULT_OVS_VIF_DRIVER_CLASS);
        this.res.setBridgeType(BridgeType.OPENVSWITCH);
        configure(params);
        checkAllSame(this.ovsVifDriver);
    }

    @Test
    public void testWhenExplicitlySetDifferentDefault() throws ConfigurationException {

        // Tests when explicitly set vif driver to OVS when using regular bridges and vice versa
        final Map<String, Object> params = new HashMap<>();

        // Switch res' bridge type for test purposes
        params.put(this.LibVirtVifDriver, LibvirtComputingResource.DEFAULT_OVS_VIF_DRIVER_CLASS);
        this.res.setBridgeType(BridgeType.NATIVE);
        configure(params);
        checkAllSame(this.ovsVifDriver);

        params.clear();
        params.put(this.LibVirtVifDriver, LibvirtComputingResource.DEFAULT_BRIDGE_VIF_DRIVER_CLASS);
        this.res.setBridgeType(BridgeType.OPENVSWITCH);
        configure(params);
        checkAllSame(this.bridgeVifDriver);
    }

    @Test
    public void testOverrideSomeTrafficTypes() throws ConfigurationException {

        final Map<String, Object> params = new HashMap<>();
        params.put(this.LibVirtVifDriver + "." + "Public", this.FakeVifDriverClassName);
        params.put(this.LibVirtVifDriver + "." + "Guest", LibvirtComputingResource.DEFAULT_OVS_VIF_DRIVER_CLASS);
        this.res.setBridgeType(BridgeType.NATIVE);
        configure(params);

        // Initially, set all traffic types to use default
        for (final TrafficType trafficType : TrafficType.values()) {
            this.assertions.put(trafficType, this.bridgeVifDriver);
        }

        this.assertions.put(TrafficType.Public, this.fakeVifDriver);
        this.assertions.put(TrafficType.Guest, this.ovsVifDriver);

        checkAssertions();
    }

    @Test
    public void testBadTrafficType() throws ConfigurationException {
        final Map<String, Object> params = new HashMap<>();
        params.put(this.LibVirtVifDriver + "." + "NonExistentTrafficType", this.FakeVifDriverClassName);
        this.res.setBridgeType(BridgeType.NATIVE);
        configure(params);

        // Set all traffic types to use default, because bad traffic type should be ignored
        for (final TrafficType trafficType : TrafficType.values()) {
            this.assertions.put(trafficType, this.bridgeVifDriver);
        }

        checkAssertions();
    }

    @Test
    public void testEmptyTrafficType() throws ConfigurationException {
        final Map<String, Object> params = new HashMap<>();
        params.put(this.LibVirtVifDriver + ".", this.FakeVifDriverClassName);
        this.res.setBridgeType(BridgeType.NATIVE);
        configure(params);

        // Set all traffic types to use default, because bad traffic type should be ignored
        for (final TrafficType trafficType : TrafficType.values()) {
            this.assertions.put(trafficType, this.bridgeVifDriver);
        }

        checkAssertions();
    }

    @Test(expected = ConfigurationException.class)
    public void testBadVifDriverClassName() throws ConfigurationException {
        final Map<String, Object> params = new HashMap<>();
        final String nonExistentVifDriverClassName = "com.cloud.agent.resource.kvm.NonExistentVifDriver";
        params.put(this.LibVirtVifDriver + "." + "Public", nonExistentVifDriverClassName);
        this.res.setBridgeType(BridgeType.NATIVE);
        configure(params);
    }
}
