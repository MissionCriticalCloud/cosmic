package com.cloud.host;

import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.resource.ResourceState;
import com.cloud.utils.fsm.StateObject;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

import java.util.Date;

/**
 * Host represents one particular host server.
 */
public interface Host extends StateObject<Status>, Identity, InternalIdentity {
    /**
     * @return name of the machine.
     */
    String getName();

    /**
     * @return the type of host.
     */
    Type getType();

    /**
     * @return the date the host first registered
     */
    Date getCreated();

    /**
     * @return current state of this machine.
     */
    Status getStatus();

    /**
     * @return the ip address of the host.
     */
    String getPrivateIpAddress();

    /**
     * @return the ip address of the host.
     */
    String getStorageUrl();

    /**
     * @return the ip address of the host attached to the storage network.
     */
    String getStorageIpAddress();

    /**
     * @return the mac address of the host.
     */
    String getGuid();

    /**
     * @return total amount of memory.
     */
    Long getTotalMemory();

    /**
     * @return # of cpu sockets in a machine.
     */
    Integer getCpuSockets();

    /**
     * @return # of cores in a machine.  Note two cpus with two cores each returns 4.
     */
    Integer getCpus();

    /**
     * @return speed of each cpu in mhz.
     */
    Long getSpeed();

    /**
     * @return the proxy port that is being listened at the agent host
     */
    Integer getProxyPort();

    /**
     * @return the pod.
     */
    Long getPodId();

    /**
     * @return availability zone.
     */
    long getDataCenterId();

    /**
     * @return parent path.  only used for storage server.
     */
    String getParent();

    /**
     * @return storage ip address.
     */
    String getStorageIpAddressDeux();

    /**
     * @return type of hypervisor
     */
    HypervisorType getHypervisorType();

    /**
     * @return disconnection date
     */
    Date getDisconnectedOn();

    /**
     * @return version
     */
    String getVersion();

    /*
     * @return total size
     */
    long getTotalSize();

    /*
     * @return capabilities
     */
    String getCapabilities();

    /*
     * @return last pinged time
     */
    long getLastPinged();

    /*
     * @return management server id
     */
    Long getManagementServerId();

    /*
     *@return removal date
     */
    Date getRemoved();

    Long getClusterId();

    String getPublicIpAddress();

    String getPublicNetmask();

    String getPrivateNetmask();

    String getStorageNetmask();

    String getStorageMacAddress();

    String getPublicMacAddress();

    String getPrivateMacAddress();

    String getStorageNetmaskDeux();

    String getStorageMacAddressDeux();

    String getHypervisorVersion();

    boolean isInMaintenanceStates();

    ResourceState getResourceState();

    enum Type {
        Storage(false), Routing(false), SecondaryStorage(false), SecondaryStorageCmdExecutor(false), ConsoleProxy(true), ExternalFirewall(false), ExternalLoadBalancer(
                false), ExternalVirtualSwitchSupervisor(false), TrafficMonitor(false),

        ExternalDhcp(false), SecondaryStorageVM(true), LocalSecondaryStorage(false), L2Networking(false);
        boolean _virtual;

        Type(final boolean virtual) {
            _virtual = virtual;
        }

        public static String[] toStrings(final Host.Type... types) {
            final String[] strs = new String[types.length];
            for (int i = 0; i < types.length; i++) {
                strs[i] = types[i].toString();
            }
            return strs;
        }

        public boolean isVirtual() {
            return _virtual;
        }
    }
}
