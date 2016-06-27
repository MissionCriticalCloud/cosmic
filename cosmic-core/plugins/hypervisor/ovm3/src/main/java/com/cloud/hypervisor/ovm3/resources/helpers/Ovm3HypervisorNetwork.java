package com.cloud.hypervisor.ovm3.resources.helpers;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.CheckNetworkAnswer;
import com.cloud.agent.api.CheckNetworkCommand;
import com.cloud.agent.api.PingTestCommand;
import com.cloud.agent.api.to.NicTO;
import com.cloud.hypervisor.ovm3.objects.CloudstackPlugin;
import com.cloud.hypervisor.ovm3.objects.Connection;
import com.cloud.hypervisor.ovm3.objects.Network;
import com.cloud.hypervisor.ovm3.objects.Ovm3ResourceException;
import com.cloud.network.Networks.BroadcastDomainType;
import com.cloud.network.Networks.TrafficType;
import com.cloud.network.PhysicalNetworkSetupInfo;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.net.NetUtils;

import javax.naming.ConfigurationException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ovm3HypervisorNetwork {
    private static final Logger LOGGER = LoggerFactory.getLogger(Ovm3HypervisorNetwork.class);
    private final Connection connection;
    private final Ovm3Configuration config;

    public Ovm3HypervisorNetwork(final Connection conn, final Ovm3Configuration ovm3config) {
        connection = conn;
        config = ovm3config;
    }

    public void configureNetworking() throws ConfigurationException {
    /*
     * TODO: setup meta tags for the management interface (probably required with multiple interfaces)?
     */
        try {
            final Network net = new Network(connection);
            final String controlIface = config.getAgentControlNetworkName();
            if (controlIface != null
                    && net.getInterfaceByName(controlIface) == null) {
                LOGGER.debug("starting " + controlIface);
                net.startOvsLocalConfig(controlIface);
        /* ovs replies too "fast" so the bridge can be "busy" */
                int contCount = 0;
                while (net.getInterfaceByName(controlIface) == null) {
                    LOGGER.debug("waiting for " + controlIface);
                    Thread.sleep(1 * 1000);
                    if (contCount > 9) {
                        throw new ConfigurationException("Unable to configure "
                                + controlIface + " on host "
                                + config.getAgentHostname());
                    }
                    contCount++;
                }
            } else {
                LOGGER.debug("already have " + controlIface);
            }
      /*
       * The bridge is remembered upon reboot, but not the IP or the config. Zeroconf also adds the route again by
       * default.
       */
            net.ovsIpConfig(controlIface, "static",
                    NetUtils.getLinkLocalGateway(),
                    NetUtils.getLinkLocalNetMask());
            final CloudstackPlugin cSp = new CloudstackPlugin(connection);
            cSp.ovsControlInterface(controlIface,
                    NetUtils.getLinkLocalCIDR());
        } catch (final InterruptedException e) {
            LOGGER.error("interrupted?", e);
        } catch (final Ovm3ResourceException e) {
            final String msg = "Basic configuration failed on " + config.getAgentHostname();
            LOGGER.error(msg, e);
            throw new ConfigurationException(msg + ", " + e.getMessage());
        }
    }

    /* this might have to change in the future, works for now... */
    public CheckNetworkAnswer execute(final CheckNetworkCommand cmd) {
        LOGGER.debug("Checking if network name setup is done on "
                + config.getAgentHostname());

        final List<PhysicalNetworkSetupInfo> infoList = cmd.getPhysicalNetworkInfoList();
    /* here we assume all networks are set */
        for (final PhysicalNetworkSetupInfo info : infoList) {
            if (info.getGuestNetworkName() == null) {
                info.setGuestNetworkName(config.getAgentGuestNetworkName());
            }
            if (info.getPublicNetworkName() == null) {
                info.setPublicNetworkName(config.getAgentPublicNetworkName());
            }
            if (info.getPrivateNetworkName() == null) {
                info.setPrivateNetworkName(config.getAgentPrivateNetworkName());
            }
            if (info.getStorageNetworkName() == null) {
                info.setStorageNetworkName(config.getAgentStorageNetworkName());
            }

            if (!isNetworkSetupByName(info.getGuestNetworkName())) {
                final String msg = "Guest Physical Network id:"
                        + info.getPhysicalNetworkId()
                        + ", Guest Network is not configured on the backend by name "
                        + info.getGuestNetworkName();
                LOGGER.error(msg);
                return new CheckNetworkAnswer(cmd, false, msg);
            }
            if (!isNetworkSetupByName(info.getPrivateNetworkName())) {
                final String msg = "Private Physical Network id:"
                        + info.getPhysicalNetworkId()
                        + ", Private Network is not configured on the backend by name "
                        + info.getPrivateNetworkName();
                LOGGER.error(msg);
                return new CheckNetworkAnswer(cmd, false, msg);
            }
            if (!isNetworkSetupByName(info.getPublicNetworkName())) {
                final String msg = "Public Physical Network id:"
                        + info.getPhysicalNetworkId()
                        + ", Public Network is not configured on the backend by name "
                        + info.getPublicNetworkName();
                LOGGER.error(msg);
                return new CheckNetworkAnswer(cmd, false, msg);
            }
      /* Storage network is optional, will revert to private otherwise */
        }

        return new CheckNetworkAnswer(cmd, true,
                "Network Setup check by names is done");
    }

    /**/
    private boolean isNetworkSetupByName(final String nameTag) {
        if (nameTag != null) {
            LOGGER.debug("Looking for network setup by name " + nameTag);

            try {
                final Network net = new Network(connection);
                net.getInterfaceList();
                if (net.getBridgeByName(nameTag) != null) {
                    LOGGER.debug("Found bridge with name: " + nameTag);
                    return true;
                }
            } catch (final Ovm3ResourceException e) {
                LOGGER.debug("Unxpected error looking for name: " + nameTag, e);
                return false;
            }
        }
        LOGGER.debug("No bridge with name: " + nameTag);
        return false;
    }

    public Answer execute(final PingTestCommand cmd) {
        try {
            if (cmd.getComputingHostIp() != null) {
                final CloudstackPlugin cSp = new CloudstackPlugin(connection);
                if (!cSp.ping(cmd.getComputingHostIp())) {
                    return new Answer(cmd, false, "ping failed");
                }
            } else {
                return new Answer(cmd, false, "why asks me to ping a router???");
            }
            return new Answer(cmd, true, "success");
        } catch (final Ovm3ResourceException e) {
            LOGGER.debug("Ping " + cmd.getComputingHostIp() + " failed", e);
            return new Answer(cmd, false, e.getMessage());
        }
    }

    /* getNetwork needs to be split in pure retrieval versus creation */
    public String getNetwork(final NicTO nic) throws Ovm3ResourceException {
        String vlanId = null;
        String bridgeName = null;
        if (nic.getBroadcastType() == BroadcastDomainType.Vlan) {
            vlanId = BroadcastDomainType.getValue(nic.getBroadcastUri());
        }

        if (nic.getType() == TrafficType.Guest) {
            if (nic.getBroadcastType() == BroadcastDomainType.Vlan
                    && !"untagged".equalsIgnoreCase(vlanId)) {
        /*
         * This is completely the wrong place for this, we should NEVER create a network when we're just trying to
         * figure out if it's there The name of this is misleading and wrong.
         */
                bridgeName = createVlanBridge(config.getAgentGuestNetworkName(),
                        Integer.valueOf(vlanId));
            } else {
                bridgeName = config.getAgentGuestNetworkName();
            }

      /* VLANs for other mgmt traffic ? */
        } else if (nic.getType() == TrafficType.Control) {
            bridgeName = config.getAgentControlNetworkName();
        } else if (nic.getType() == TrafficType.Public) {
            bridgeName = config.getAgentPublicNetworkName();
        } else if (nic.getType() == TrafficType.Management) {
            bridgeName = config.getAgentPrivateNetworkName();
        } else if (nic.getType() == TrafficType.Storage) {
            bridgeName = config.getAgentStorageNetworkName();
        } else {
            throw new CloudRuntimeException("Unknown network traffic type:"
                    + nic.getType());
        }
        return bridgeName;
    }

    private String createVlanBridge(final String networkName, final Integer vlanId)
            throws Ovm3ResourceException {
        if (vlanId < 1 || vlanId > 4094) {
            final String msg = "Incorrect vlan " + vlanId
                    + ", needs to be between 1 and 4094";
            LOGGER.error(msg);
            throw new CloudRuntimeException(msg);
        }
        final Network net = new Network(connection);
    /* figure out if our bridged vlan exists, if not then create */
        final String brName = networkName + "." + vlanId.toString();
        try {
            final String physInterface = net.getPhysicalByBridgeName(networkName);
            if (net.getInterfaceByName(brName) == null) {
                net.startOvsVlanBridge(brName, physInterface, vlanId);
            } else {
                LOGGER.debug("Interface " + brName + " already exists");
            }
        } catch (final Ovm3ResourceException e) {
            final String msg = "Unable to create vlan " + vlanId.toString() + " bridge for " + networkName;
            LOGGER.warn(msg + ": " + e);
            throw new CloudRuntimeException(msg + ":" + e.getMessage());
        }
        return brName;
    }
}
