package com.cloud.hypervisor.kvm.resource;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.HostVmStateReportEntry;
import com.cloud.agent.api.PingCommand;
import com.cloud.agent.api.PingRoutingCommand;
import com.cloud.agent.api.PingRoutingWithNwGroupsCommand;
import com.cloud.agent.api.SetupGuestNetworkCommand;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.api.StartupRoutingCommand;
import com.cloud.agent.api.StartupStorageCommand;
import com.cloud.agent.api.VmDiskStatsEntry;
import com.cloud.agent.api.VmStatsEntry;
import com.cloud.agent.api.routing.IpAssocCommand;
import com.cloud.agent.api.routing.IpAssocVpcCommand;
import com.cloud.agent.api.routing.NetworkElementCommand;
import com.cloud.agent.api.routing.SetSourceNatCommand;
import com.cloud.agent.api.to.DataStoreTO;
import com.cloud.agent.api.to.DataTO;
import com.cloud.agent.api.to.DiskTO;
import com.cloud.agent.api.to.IpAddressTO;
import com.cloud.agent.api.to.NfsTO;
import com.cloud.agent.api.to.NicTO;
import com.cloud.agent.api.to.VirtualMachineTO;
import com.cloud.agent.resource.virtualnetwork.VRScripts;
import com.cloud.agent.resource.virtualnetwork.VirtualRouterDeployer;
import com.cloud.agent.resource.virtualnetwork.VirtualRoutingResource;
import com.cloud.dc.Vlan;
import com.cloud.exception.InternalErrorException;
import com.cloud.host.Host.Type;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.hypervisor.kvm.resource.LibvirtVmDef.ClockDef;
import com.cloud.hypervisor.kvm.resource.LibvirtVmDef.ConsoleDef;
import com.cloud.hypervisor.kvm.resource.LibvirtVmDef.CpuModeDef;
import com.cloud.hypervisor.kvm.resource.LibvirtVmDef.CpuTuneDef;
import com.cloud.hypervisor.kvm.resource.LibvirtVmDef.DevicesDef;
import com.cloud.hypervisor.kvm.resource.LibvirtVmDef.DiskDef;
import com.cloud.hypervisor.kvm.resource.LibvirtVmDef.DiskDef.DeviceType;
import com.cloud.hypervisor.kvm.resource.LibvirtVmDef.DiskDef.DiskProtocol;
import com.cloud.hypervisor.kvm.resource.LibvirtVmDef.FeaturesDef;
import com.cloud.hypervisor.kvm.resource.LibvirtVmDef.GraphicDef;
import com.cloud.hypervisor.kvm.resource.LibvirtVmDef.GuestDef;
import com.cloud.hypervisor.kvm.resource.LibvirtVmDef.GuestResourceDef;
import com.cloud.hypervisor.kvm.resource.LibvirtVmDef.InputDef;
import com.cloud.hypervisor.kvm.resource.LibvirtVmDef.InterfaceDef;
import com.cloud.hypervisor.kvm.resource.LibvirtVmDef.InterfaceDef.GuestNetType;
import com.cloud.hypervisor.kvm.resource.LibvirtVmDef.QemuGuestAgentDef;
import com.cloud.hypervisor.kvm.resource.LibvirtVmDef.RngDef;
import com.cloud.hypervisor.kvm.resource.LibvirtVmDef.RngDef.RngBackendModel;
import com.cloud.hypervisor.kvm.resource.LibvirtVmDef.SerialDef;
import com.cloud.hypervisor.kvm.resource.LibvirtVmDef.TermPolicy;
import com.cloud.hypervisor.kvm.resource.LibvirtVmDef.VideoDef;
import com.cloud.hypervisor.kvm.resource.LibvirtVmDef.VirtioSerialDef;
import com.cloud.hypervisor.kvm.resource.LibvirtVmDef.WatchDogDef;
import com.cloud.hypervisor.kvm.resource.LibvirtVmDef.WatchDogDef.WatchDogAction;
import com.cloud.hypervisor.kvm.resource.LibvirtVmDef.WatchDogDef.WatchDogModel;
import com.cloud.hypervisor.kvm.resource.wrapper.LibvirtRequestWrapper;
import com.cloud.hypervisor.kvm.resource.wrapper.LibvirtUtilitiesHelper;
import com.cloud.hypervisor.kvm.storage.KvmPhysicalDisk;
import com.cloud.hypervisor.kvm.storage.KvmStoragePool;
import com.cloud.hypervisor.kvm.storage.KvmStoragePoolManager;
import com.cloud.hypervisor.kvm.storage.KvmStorageProcessor;
import com.cloud.network.Networks.BroadcastDomainType;
import com.cloud.network.Networks.RouterPrivateIpStrategy;
import com.cloud.network.Networks.TrafficType;
import com.cloud.resource.ServerResource;
import com.cloud.resource.ServerResourceBase;
import com.cloud.storage.JavaStorageLayer;
import com.cloud.storage.Storage;
import com.cloud.storage.Storage.StoragePoolType;
import com.cloud.storage.StorageLayer;
import com.cloud.storage.Volume;
import com.cloud.storage.resource.StorageSubsystemCommandHandler;
import com.cloud.storage.resource.StorageSubsystemCommandHandlerBase;
import com.cloud.utils.ExecutionResult;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.Pair;
import com.cloud.utils.PropertiesUtil;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.net.NetUtils;
import com.cloud.utils.script.OutputInterpreter;
import com.cloud.utils.script.OutputInterpreter.AllLinesParser;
import com.cloud.utils.script.Script;
import com.cloud.utils.ssh.SshHelper;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachine.PowerState;
import org.apache.cloudstack.storage.to.PrimaryDataStoreTO;
import org.apache.cloudstack.storage.to.VolumeObjectTO;
import org.apache.cloudstack.utils.hypervisor.HypervisorUtils;
import org.apache.cloudstack.utils.linux.CpuStat;
import org.apache.cloudstack.utils.linux.MemStat;
import org.apache.cloudstack.utils.qemu.QemuImg.PhysicalDiskFormat;

import javax.ejb.Local;
import javax.naming.ConfigurationException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Strings;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.DomainBlockStats;
import org.libvirt.DomainInfo;
import org.libvirt.DomainInfo.DomainState;
import org.libvirt.DomainInterfaceStats;
import org.libvirt.LibvirtException;
import org.libvirt.NodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LibvirtComputingResource execute requests on the computing/routing host using the libvirt API
 *
 * @config {@table || Param Name | Description | Values | Default || || hypervisor.type | type of local hypervisor |
 * string | kvm || || hypervisor.uri | local hypervisor to connect to | URI | qemu:///system || || domr.arch |
 * instruction set for domr template | string | i686 || || private.bridge.name | private bridge where the domrs
 * have their private interface | string | vmops0 || || public.bridge.name | public bridge where the domrs have
 * their public interface | string | br0 || || private.network.name | name of the network where the domrs have
 * their private interface | string | vmops-private || || private.ipaddr.start | start of the range of private
 * ip addresses for domrs | ip address | 192.168.166.128 || || private.ipaddr.end | end of the range of private
 * ip addresses for domrs | ip address | start + 126 || || private.macaddr.start | start of the range of private
 * mac addresses for domrs | mac address | 00:16:3e:77:e2:a0 || || private.macaddr.end | end of the range of
 * private mac addresses for domrs | mac address | start + 126 || || pool | the parent of the storage pool
 * hierarchy * }
 **/
@Local(value = {ServerResource.class})
public class LibvirtComputingResource extends ServerResourceBase implements ServerResource, VirtualRouterDeployer {

    public static final String SSHKEYSPATH = "/root/.ssh";
    public static final String SSHPRVKEYPATH = SSHKEYSPATH + File.separator + "id_rsa.cloud";
    public static final String SSHPUBKEYPATH = SSHKEYSPATH + File.separator + "id_rsa.pub.cloud";
    public static final String BASH_SCRIPT_PATH = "/bin/bash";

    protected static final String DEFAULT_OVS_VIF_DRIVER_CLASS = "com.cloud.hypervisor.kvm.resource.OvsVifDriver";
    protected static final String DEFAULT_BRIDGE_VIF_DRIVER_CLASS = "com.cloud.hypervisor.kvm.resource.BridgeVifDriver";
    protected static final HashMap<DomainState, PowerState> s_powerStatesTable;
    private static final Logger LOGGER = LoggerFactory.getLogger(LibvirtComputingResource.class);

    static {
        s_powerStatesTable = new HashMap<>();
        s_powerStatesTable.put(DomainState.VIR_DOMAIN_SHUTOFF, PowerState.PowerOff);
        s_powerStatesTable.put(DomainState.VIR_DOMAIN_PAUSED, PowerState.PowerOn);
        s_powerStatesTable.put(DomainState.VIR_DOMAIN_RUNNING, PowerState.PowerOn);
        s_powerStatesTable.put(DomainState.VIR_DOMAIN_BLOCKED, PowerState.PowerOn);
        s_powerStatesTable.put(DomainState.VIR_DOMAIN_NOSTATE, PowerState.PowerUnknown);
        s_powerStatesTable.put(DomainState.VIR_DOMAIN_SHUTDOWN, PowerState.PowerOff);
    }

    private final Map<String, String> pifs = new HashMap<>();
    private final Map<String, VmStats> vmStats = new ConcurrentHashMap<>();
    private final LibvirtUtilitiesHelper libvirtUtilitiesHelper = new LibvirtUtilitiesHelper();
    protected HypervisorType hypervisorType;
    protected String hypervisorUri;
    protected long hypervisorLibvirtVersion;
    protected long hypervisorQemuVersion;
    protected String hypervisorPath;
    protected String networkDirectSourceMode;
    protected String networkDirectDevice;
    protected String sysvmIsoPath;
    protected String privNwName;
    protected String privBridgeName;
    protected String linkLocalBridgeName;
    protected String publicBridgeName;
    protected String guestBridgeName;
    protected String privateIp;
    protected String pool;
    protected String localGateway;
    protected String localStoragePath;
    protected String localStorageUuid;
    protected boolean noMemBalloon = false;
    protected String guestCpuMode;
    protected String guestCpuModel;
    protected boolean noKvmClock;
    protected String videoHw;
    protected int videoRam;
    protected Pair<Integer, Integer> hostOsVersion;
    protected int migrateSpeed;
    protected int migrateDowntime;
    protected int migratePauseAfter;
    protected boolean diskActivityCheckEnabled;
    protected long diskActivityCheckFileSizeMin = 10485760; // 10MB
    protected int diskActivityCheckTimeoutSeconds = 120; // 120s
    protected long diskActivityInactiveThresholdMilliseconds = 30000; // 30s
    protected boolean rngEnable = false;
    protected RngBackendModel rngBackendModel = RngBackendModel.RANDOM;
    protected String rngPath = "/dev/random";
    protected WatchDogAction watchDogAction = WatchDogAction.NONE;
    protected WatchDogModel watchDogModel = WatchDogModel.I6300ESB;
    protected List<String> cpuFeatures;
    protected BridgeType bridgeType;
    protected List<String> vmsKilled = new ArrayList<>();
    protected boolean disconnected = true;
    protected int cmdsTimeout;
    protected int stopTimeout;
    protected CpuStat cpuStat = new CpuStat();
    protected MemStat memStat = new MemStat();
    protected StorageSubsystemCommandHandler storageHandler;
    String[] ifNamePatterns = {
            "^eth",
            "^bond",
            "^vlan",
            "^vx",
            "^em",
            "^ens",
            "^eno",
            "^enp",
            "^team",
            "^enx",
            "^p\\d+p\\d+"
    };
    private String modifyVlanPath;
    private String versionstringpath;
    private String patchViaSocketPath;
    private String createVmPath;
    private String manageSnapshotPath;
    private String resizeVolumePath;
    private String createTmplPath;
    private String heartBeatPath;
    private String securityGroupPath;
    private String ovsPvlanDhcpHostPath;
    private String ovsPvlanVmPath;
    private String routerProxyPath;
    private String ovsTunnelPath;
    private String host;
    private String dcId;
    private String pod;
    private String clusterId;
    private long hvVersion;
    private long kernelVersion;
    private int timeout;
    private VirtualRoutingResource virtRouterResource;
    private String pingTestPath;
    private String updateHostPasswdPath;
    private long dom0MinMem;
    private KvmHaMonitor monitor;
    private String mountPoint = "/mnt";
    private StorageLayer storage;
    private KvmStoragePoolManager storagePoolMgr;
    private VifDriver defaultVifDriver;
    private Map<TrafficType, VifDriver> trafficTypeVifDrivers;
    private boolean canBridgeFirewall;

    @Override
    public ExecutionResult executeInVR(final String routerIp, final String script, final String args) {
        return executeInVR(routerIp, script, args, timeout / 1000);
    }

    @Override
    public ExecutionResult executeInVR(final String routerIp, final String script, final String args, final int timeout) {
        final Script command = new Script(routerProxyPath, timeout * 1000, LOGGER);
        final AllLinesParser parser = new AllLinesParser();
        command.add(script);
        command.add(routerIp);
        if (args != null) {
            command.add(args);
        }
        String details = command.execute(parser);
        if (details == null) {
            details = parser.getLines();
        }

        LOGGER.debug("Executing script in VR " + script);

        return new ExecutionResult(command.getExitValue() == 0, details);
    }

    @Override
    public ExecutionResult createFileInVR(final String routerIp, final String path, final String filename,
                                          final String content) {
        final File permKey = new File("/root/.ssh/id_rsa.cloud");
        String error = null;

        LOGGER.debug("Creating file in VR " + filename);

        try {
            SshHelper.scpTo(routerIp, 3922, "root", permKey, null, path, content.getBytes(), filename, null);
        } catch (final Exception e) {
            LOGGER.warn("Fail to create file " + path + filename + " in VR " + routerIp, e);
            error = e.getMessage();
        }
        return new ExecutionResult(error == null, error);
    }

    @Override
    public ExecutionResult prepareCommand(final NetworkElementCommand cmd) {
        // Update IP used to access router
        cmd.setRouterAccessIp(cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP));
        assert cmd.getRouterAccessIp() != null;

        if (cmd instanceof IpAssocVpcCommand) {
            return prepareNetworkElementCommand((IpAssocVpcCommand) cmd);
        } else if (cmd instanceof IpAssocCommand) {
            return prepareNetworkElementCommand((IpAssocCommand) cmd);
        } else if (cmd instanceof SetupGuestNetworkCommand) {
            return prepareNetworkElementCommand((SetupGuestNetworkCommand) cmd);
        } else if (cmd instanceof SetSourceNatCommand) {
            return prepareNetworkElementCommand((SetSourceNatCommand) cmd);
        }
        return new ExecutionResult(true, null);
    }

    @Override
    public ExecutionResult cleanupCommand(final NetworkElementCommand cmd) {
        if (cmd instanceof IpAssocCommand && !(cmd instanceof IpAssocVpcCommand)) {
            return cleanupNetworkElementCommand((IpAssocCommand) cmd);
        }
        return new ExecutionResult(true, null);
    }

    protected ExecutionResult cleanupNetworkElementCommand(final IpAssocCommand cmd) {

        final String routerName = cmd.getAccessDetail(NetworkElementCommand.ROUTER_NAME);
        final String routerIp = cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP);
        final Connect conn;

        try {
            conn = LibvirtConnection.getConnectionByVmName(routerName);
            final List<InterfaceDef> nics = getInterfaces(conn, routerName, guestBridgeName);
            final Map<String, Integer> broadcastUriAllocatedToVm = new HashMap<>();

            Integer nicPos = 0;
            for (final InterfaceDef nic : nics) {
                if (nic.getBrName().equalsIgnoreCase(linkLocalBridgeName)) {
                    broadcastUriAllocatedToVm.put("LinkLocal", nicPos);
                } else {
                    if (nic.getBrName().equalsIgnoreCase(publicBridgeName) || nic.getBrName().equalsIgnoreCase(privBridgeName)) {
                        broadcastUriAllocatedToVm.put(BroadcastDomainType.Vlan.toUri(Vlan.UNTAGGED).toString(), nicPos);
                    } else {
                        final String broadcastUri = getBroadcastUriFromBridge(nic.getBrName());
                        broadcastUriAllocatedToVm.put(broadcastUri, nicPos);
                    }
                }
                nicPos++;
            }

            final IpAddressTO[] ips = cmd.getIpAddresses();
            final int numOfIps = ips.length;
            int nicNum = 0;
            for (final IpAddressTO ip : ips) {

                if (!broadcastUriAllocatedToVm.containsKey(ip.getBroadcastUri())) {
          /* plug a vif into router */
                    vifHotPlug(conn, routerName, ip.getBroadcastUri(), ip.getVifMacAddress());
                    broadcastUriAllocatedToVm.put(ip.getBroadcastUri(), nicPos++);
                }
                nicNum = broadcastUriAllocatedToVm.get(ip.getBroadcastUri());

                if (numOfIps == 1 && !ip.isAdd()) {
                    vifHotUnPlug(conn, routerName, ip.getVifMacAddress());
                    networkUsage(routerIp, "deleteVif", "eth" + nicNum);
                }
            }
        } catch (final LibvirtException e) {
            LOGGER.error("ipassoccmd failed", e);
            return new ExecutionResult(false, e.getMessage());
        } catch (final InternalErrorException e) {
            LOGGER.error("ipassoccmd failed", e);
            return new ExecutionResult(false, e.getMessage());
        }

        return new ExecutionResult(true, null);
    }

    public List<InterfaceDef> getInterfaces(final Connect conn, final String vmName, final String interfaceToExclude) {
        final List<InterfaceDef> interfaces = getInterfaces(conn, vmName);
        final List<InterfaceDef> interfacesToReturn = new ArrayList<>();
        for (final InterfaceDef interfaceDef : interfaces) {
            if (!interfaceDef.getBrName().equalsIgnoreCase(interfaceToExclude)) {
                interfacesToReturn.add(interfaceDef);
            }
        }

        return interfacesToReturn;
    }

    private void vifHotUnPlug(final Connect conn, final String vmName, final String macAddr)
            throws InternalErrorException, LibvirtException {

        Domain vm = null;
        vm = getDomain(conn, vmName);
        final List<InterfaceDef> pluggedNics = getInterfaces(conn, vmName);
        for (final InterfaceDef pluggedNic : pluggedNics) {
            if (pluggedNic.getMacAddress().equalsIgnoreCase(macAddr)) {
                vm.detachDevice(pluggedNic.toString());
                // We don't know which "traffic type" is associated with
                // each interface at this point, so inform all vif drivers
                for (final VifDriver vifDriver : getAllVifDrivers()) {
                    vifDriver.unplug(pluggedNic);
                }
            }
        }
    }

    public List<VifDriver> getAllVifDrivers() {
        final Set<VifDriver> vifDrivers = new HashSet<>();

        vifDrivers.add(defaultVifDriver);
        vifDrivers.addAll(trafficTypeVifDrivers.values());

        final ArrayList<VifDriver> vifDriverList = new ArrayList<>(vifDrivers);

        return vifDriverList;
    }

    protected ExecutionResult prepareNetworkElementCommand(final IpAssocVpcCommand cmd) {
        final String routerName = cmd.getAccessDetail(NetworkElementCommand.ROUTER_NAME);

        try {
            final Connect conn = LibvirtConnection.getConnectionByVmName(routerName);
            final IpAddressTO[] ips = cmd.getIpAddresses();
            final Map<String, Integer> bridgeToNicNum = new HashMap<>();
            final List<InterfaceDef> pluggedNics = getInterfaces(conn, routerName);

            buildBridgeToNicNumHashMap(bridgeToNicNum, pluggedNics);

            for (final IpAddressTO ip : ips) {
                setIpNicDevId(bridgeToNicNum, ip);
            }

            return new ExecutionResult(true, null);
        } catch (final LibvirtException e) {
            LOGGER.error("Ip Assoc failure on applying one ip due to exception:  ", e);
            return new ExecutionResult(false, e.getMessage());
        }
    }

    public ExecutionResult prepareNetworkElementCommand(final IpAssocCommand cmd) {
        final String routerName = cmd.getAccessDetail(NetworkElementCommand.ROUTER_NAME);
        final String routerIp = cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP);

        try {
            final Connect conn = LibvirtConnection.getConnectionByVmName(routerName);
            final IpAddressTO[] ips = cmd.getIpAddresses();
            final Map<String, Integer> bridgeToNicNum = new HashMap<>();
            final List<InterfaceDef> pluggedNics = getInterfaces(conn, routerName);

            Integer devNum = buildBridgeToNicNumHashMap(bridgeToNicNum, pluggedNics);

            int nicNum;
            for (final IpAddressTO ip : ips) {
                boolean newNic = false;
                if (!bridgeToNicNum.containsKey(getBridgeNameFromTrafficType(ip.getTrafficType()))) {
          /* plug a vif into router */
                    vifHotPlug(conn, routerName, ip.getBroadcastUri(), ip.getVifMacAddress());
                    bridgeToNicNum.put(getBridgeNameFromTrafficType(ip.getTrafficType()), devNum++);
                    newNic = true;
                }
                nicNum = setIpNicDevId(bridgeToNicNum, ip);
                networkUsage(routerIp, "addVif", "eth" + nicNum);

                ip.setNewNic(newNic);
            }
            return new ExecutionResult(true, null);
        } catch (final LibvirtException e) {
            LOGGER.error("ipassoccmd failed", e);
            return new ExecutionResult(false, e.getMessage());
        } catch (final InternalErrorException e) {
            LOGGER.error("ipassoccmd failed", e);
            return new ExecutionResult(false, e.getMessage());
        }
    }

    private ExecutionResult prepareNetworkElementCommand(final SetupGuestNetworkCommand cmd) {
        final Connect conn;
        final NicTO nic = cmd.getNic();
        final String routerName = cmd.getAccessDetail(NetworkElementCommand.ROUTER_NAME);

        try {
            conn = LibvirtConnection.getConnectionByVmName(routerName);
            final List<InterfaceDef> pluggedNics = getInterfaces(conn, routerName);
            InterfaceDef routerNic = null;

            for (final InterfaceDef pluggedNic : pluggedNics) {
                if (pluggedNic.getMacAddress().equalsIgnoreCase(nic.getMac())) {
                    routerNic = pluggedNic;
                    break;
                }
            }

            if (routerNic == null) {
                return new ExecutionResult(false, "Can not find nic with mac " + nic.getMac() + " for VM " + routerName);
            }

            return new ExecutionResult(true, null);
        } catch (final LibvirtException e) {
            final String msg = "Creating guest network failed due to " + e.toString();
            LOGGER.warn(msg, e);
            return new ExecutionResult(false, msg);
        }
    }

    protected ExecutionResult prepareNetworkElementCommand(final SetSourceNatCommand cmd) {
        final Connect conn;
        final String routerName = cmd.getAccessDetail(NetworkElementCommand.ROUTER_NAME);
        cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP);
        final IpAddressTO pubIp = cmd.getIpAddress();

        try {
            conn = LibvirtConnection.getConnectionByVmName(routerName);
            Integer devNum = 0;
            final String pubVlan = pubIp.getBroadcastUri();
            final List<InterfaceDef> pluggedNics = getInterfaces(conn, routerName);

            for (final InterfaceDef pluggedNic : pluggedNics) {
                final String pluggedVlanBr = pluggedNic.getBrName();
                final String pluggedVlanId = getBroadcastUriFromBridge(pluggedVlanBr);
                if (pubVlan.equalsIgnoreCase(Vlan.UNTAGGED) && pluggedVlanBr.equalsIgnoreCase(publicBridgeName)) {
                    break;
                } else if (pluggedVlanBr.equalsIgnoreCase(linkLocalBridgeName)) {
          /* skip over, no physical bridge device exists */
                } else if (pluggedVlanId == null) {
          /* this should only be true in the case of link local bridge */
                    return new ExecutionResult(false,
                            "unable to find the vlan id for bridge " + pluggedVlanBr + " when attempting to set up" + pubVlan
                                    + " on router " + routerName);
                } else if (pluggedVlanId.equals(pubVlan)) {
                    break;
                }
                devNum++;
            }

            pubIp.setNicDevId(devNum);

            return new ExecutionResult(true, "success");
        } catch (final LibvirtException e) {
            final String msg = "Ip SNAT failure due to " + e.toString();
            LOGGER.error(msg, e);
            return new ExecutionResult(false, msg);
        }
    }

    public List<InterfaceDef> getInterfaces(final Connect conn, final String vmName) {
        final LibvirtDomainXmlParser parser = new LibvirtDomainXmlParser();
        Domain dm = null;
        try {
            dm = conn.domainLookupByName(vmName);
            parser.parseDomainXml(dm.getXMLDesc(0));
            return parser.getInterfaces();
        } catch (final LibvirtException e) {
            LOGGER.debug("Failed to get dom xml: " + e.toString());
            return new ArrayList<>();
        } finally {
            try {
                if (dm != null) {
                    dm.free();
                }
            } catch (final LibvirtException e) {
                LOGGER.trace("Ignoring libvirt error.", e);
            }
        }
    }

    private Integer buildBridgeToNicNumHashMap(final Map<String, Integer> bridgeToNicNum, final List<InterfaceDef> pluggedNics) {
        Integer devNum = 0;
        for (final InterfaceDef pluggedNic : pluggedNics) {
            final String pluggedVlan = pluggedNic.getBrName();

            if (pluggedVlan.equalsIgnoreCase(linkLocalBridgeName) || pluggedVlan.equalsIgnoreCase(publicBridgeName)
                    || pluggedVlan.equalsIgnoreCase(privBridgeName) || pluggedVlan.equalsIgnoreCase(guestBridgeName)) {
                bridgeToNicNum.put(pluggedVlan, devNum);
            }
            devNum++;
        }
        return devNum;
    }

    private Integer setIpNicDevId(final Map<String, Integer> bridgeToNicNum, final IpAddressTO ip) {
        if (ip.getTrafficType().equals(TrafficType.Public) && bridgeToNicNum.containsKey(publicBridgeName)) {
            ip.setNicDevId(bridgeToNicNum.get(publicBridgeName));
        } else if (ip.getTrafficType().equals(TrafficType.Management) && bridgeToNicNum.containsKey(privBridgeName)) {
            ip.setNicDevId(bridgeToNicNum.get(privBridgeName));
        } else if (ip.getTrafficType().equals(TrafficType.Guest) && bridgeToNicNum.containsKey(guestBridgeName)) {
            ip.setNicDevId(bridgeToNicNum.get(guestBridgeName));
        } else if (ip.getTrafficType().equals(TrafficType.Control) && bridgeToNicNum.containsKey(linkLocalBridgeName)) {
            ip.setNicDevId(bridgeToNicNum.get(linkLocalBridgeName));
        }
        return ip.getNicDevId();
    }

    private String getBridgeNameFromTrafficType(final TrafficType trafficType) {
        final String bridgeName;
        switch (trafficType) {
            case Public:
                bridgeName = publicBridgeName;
                break;
            case Management:
                bridgeName = privBridgeName;
                break;
            case Guest:
                bridgeName = guestBridgeName;
                break;
            case Control:
                bridgeName = linkLocalBridgeName;
                break;
            default:
                bridgeName = "";
        }
        return bridgeName;
    }

    private void vifHotPlug(final Connect conn, final String vmName, final String broadcastUri, final String macAddr)
            throws InternalErrorException, LibvirtException {
        final NicTO nicTo = new NicTO();
        nicTo.setMac(macAddr);
        nicTo.setType(TrafficType.Public);
        if (broadcastUri == null) {
            nicTo.setBroadcastType(BroadcastDomainType.Native);
        } else {
            final URI uri = BroadcastDomainType.fromString(broadcastUri);
            nicTo.setBroadcastType(BroadcastDomainType.getSchemeValue(uri));
            nicTo.setBroadcastUri(uri);
        }

        final Domain vm = getDomain(conn, vmName);
        vm.attachDevice(getVifDriver(nicTo.getType()).plug(nicTo, "Other PV", "").toString());
    }

    public String networkUsage(final String privateIpAddress, final String option, final String vif) {
        final Script getUsage = new Script(routerProxyPath, LOGGER);
        getUsage.add("netusage.sh");
        getUsage.add(privateIpAddress);
        if (option.equals("get")) {
            getUsage.add("-g");
        } else if (option.equals("create")) {
            getUsage.add("-c");
        } else if (option.equals("reset")) {
            getUsage.add("-r");
        } else if (option.equals("addVif")) {
            getUsage.add("-a", vif);
        } else if (option.equals("deleteVif")) {
            getUsage.add("-d", vif);
        }

        final OutputInterpreter.OneLineParser usageParser = new OutputInterpreter.OneLineParser();
        final String result = getUsage.execute(usageParser);
        if (result != null) {
            LOGGER.debug("Failed to execute networkUsage:" + result);
            return null;
        }
        return usageParser.getLine();
    }

    private String getBroadcastUriFromBridge(final String brName) {
        final String pif = matchPifFileInDirectory(brName);
        final Pattern pattern = Pattern.compile("(\\D+)(\\d+)(\\D*)(\\d*)");
        final Matcher matcher = pattern.matcher(pif);
        LOGGER.debug("getting broadcast uri for pif " + pif + " and bridge " + brName);
        if (matcher.find()) {
            if (brName.startsWith("brvx")) {
                return BroadcastDomainType.Vxlan.toUri(matcher.group(2)).toString();
            } else {
                if (!matcher.group(4).isEmpty()) {
                    return BroadcastDomainType.Vlan.toUri(matcher.group(4)).toString();
                } else {
                    // untagged or not matching (eth|bond|team)#.#
                    LOGGER.debug("failed to get vNet id from bridge " + brName
                            + "attached to physical interface" + pif + ", perhaps untagged interface");
                    return "";
                }
            }
        } else {
            LOGGER.debug("failed to get vNet id from bridge " + brName + "attached to physical interface" + pif);
            return "";
        }
    }

    public Domain getDomain(final Connect conn, final String vmName) throws LibvirtException {
        return conn.domainLookupByName(vmName);
    }

    public VifDriver getVifDriver(final TrafficType trafficType) {
        VifDriver vifDriver = trafficTypeVifDrivers.get(trafficType);

        if (vifDriver == null) {
            vifDriver = defaultVifDriver;
        }

        return vifDriver;
    }

    private String matchPifFileInDirectory(final String bridgeName) {
        final File brif = new File("/sys/devices/virtual/net/" + bridgeName + "/brif");

        if (!brif.isDirectory()) {
            final File pif = new File("/sys/class/net/" + bridgeName);
            if (pif.isDirectory()) {
                // if bridgeName already refers to a pif, return it as-is
                return bridgeName;
            }
            LOGGER.debug("failing to get physical interface from bridge " + bridgeName + ", does " + brif.getAbsolutePath()
                    + "exist?");
            return "";
        }

        final File[] interfaces = brif.listFiles();

        for (final File interface1 : interfaces) {
            final String fname = interface1.getName();
            LOGGER.debug("matchPifFileInDirectory: file name '" + fname + "'");
            if (isInterface(fname)) {
                return fname;
            }
        }

        LOGGER.debug("failing to get physical interface from bridge " + bridgeName
                + ", did not find an eth*, bond*, team*, vlan*, em*, p*p*, ens*, eno*, enp*, or enx* in "
                + brif.getAbsolutePath());
        return "";
    }

    boolean isInterface(final String fname) {
        final StringBuffer commonPattern = new StringBuffer();
        for (final String ifNamePattern : ifNamePatterns) {
            commonPattern.append("|(").append(ifNamePattern).append(".*)");
        }
        if (fname.matches(commonPattern.toString())) {
            return true;
        }
        return false;
    }

    public LibvirtUtilitiesHelper getLibvirtUtilitiesHelper() {
        return libvirtUtilitiesHelper;
    }

    public CpuStat getCpuStat() {
        return cpuStat;
    }

    public MemStat getMemStat() {
        return memStat;
    }

    public VirtualRoutingResource getVirtRouterResource() {
        return virtRouterResource;
    }

    public String getPublicBridgeName() {
        return publicBridgeName;
    }

    public KvmStoragePoolManager getStoragePoolMgr() {
        return storagePoolMgr;
    }

    public String getPrivateIp() {
        return privateIp;
    }

    public int getMigrateDowntime() {
        return migrateDowntime;
    }

    public int getMigratePauseAfter() {
        return migratePauseAfter;
    }

    public int getMigrateSpeed() {
        return migrateSpeed;
    }

    public String getPingTestPath() {
        return pingTestPath;
    }

    public String getUpdateHostPasswdPath() {
        return updateHostPasswdPath;
    }

    public int getTimeout() {
        return timeout;
    }

    public String getOvsTunnelPath() {
        return ovsTunnelPath;
    }

    public KvmHaMonitor getMonitor() {
        return monitor;
    }

    public StorageLayer getStorage() {
        return storage;
    }

    public String createTmplPath() {
        return createTmplPath;
    }

    public int getCmdsTimeout() {
        return cmdsTimeout;
    }

    public String manageSnapshotPath() {
        return manageSnapshotPath;
    }

    public String getGuestBridgeName() {
        return guestBridgeName;
    }

    public String getOvsPvlanDhcpHostPath() {
        return ovsPvlanDhcpHostPath;
    }

    public String getOvsPvlanVmPath() {
        return ovsPvlanVmPath;
    }

    public String getResizeVolumePath() {
        return resizeVolumePath;
    }

    public StorageSubsystemCommandHandler getStorageHandler() {
        return storageHandler;
    }

    @Override
    protected String getDefaultScriptsDir() {
        return null;
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        boolean success = super.configure(name, params);
        if (!success) {
            return false;
        }

        storage = new JavaStorageLayer();
        storage.configure("StorageLayer", params);

        String domrScriptsDir = (String) params.get("domr.scripts.dir");
        if (domrScriptsDir == null) {
            domrScriptsDir = getDefaultDomrScriptsDir();
        }

        String hypervisorScriptsDir = (String) params.get("hypervisor.scripts.dir");
        if (hypervisorScriptsDir == null) {
            hypervisorScriptsDir = getDefaultHypervisorScriptsDir();
        }

        String kvmScriptsDir = (String) params.get("kvm.scripts.dir");
        if (kvmScriptsDir == null) {
            kvmScriptsDir = getDefaultKvmScriptsDir();
        }

        String networkScriptsDir = (String) params.get("network.scripts.dir");
        if (networkScriptsDir == null) {
            networkScriptsDir = getDefaultNetworkScriptsDir();
        }

        String storageScriptsDir = (String) params.get("storage.scripts.dir");
        if (storageScriptsDir == null) {
            storageScriptsDir = getDefaultStorageScriptsDir();
        }

        final String networkBridgeType = (String) params.get("network.bridge.type");
        if (networkBridgeType == null) {
            bridgeType = BridgeType.NATIVE;
        } else {
            bridgeType = BridgeType.valueOf(networkBridgeType.toUpperCase());
        }

        params.put("domr.scripts.dir", domrScriptsDir);

        virtRouterResource = new VirtualRoutingResource(this);
        success = virtRouterResource.configure(name, params);

        if (!success) {
            return false;
        }

        host = (String) params.get("host");
        if (host == null) {
            host = "localhost";
        }

        dcId = (String) params.get("zone");
        if (dcId == null) {
            dcId = "default";
        }

        pod = (String) params.get("pod");
        if (pod == null) {
            pod = "default";
        }

        clusterId = (String) params.get("cluster");

        updateHostPasswdPath = Script.findScript(hypervisorScriptsDir, VRScripts.UPDATE_HOST_PASSWD);
        if (updateHostPasswdPath == null) {
            throw new ConfigurationException("Unable to find update_host_passwd.sh");
        }

        modifyVlanPath = Script.findScript(networkScriptsDir, "modifyvlan.sh");
        if (modifyVlanPath == null) {
            throw new ConfigurationException("Unable to find modifyvlan.sh");
        }

        versionstringpath = Script.findScript(kvmScriptsDir, "versions.sh");
        if (versionstringpath == null) {
            throw new ConfigurationException("Unable to find versions.sh");
        }

        patchViaSocketPath = Script.findScript(kvmScriptsDir + "/patch/", "patchviasocket.pl");
        if (patchViaSocketPath == null) {
            throw new ConfigurationException("Unable to find patchviasocket.pl");
        }

        heartBeatPath = Script.findScript(kvmScriptsDir, "kvmheartbeat.sh");
        if (heartBeatPath == null) {
            throw new ConfigurationException("Unable to find kvmheartbeat.sh");
        }

        createVmPath = Script.findScript(storageScriptsDir, "createvm.sh");
        if (createVmPath == null) {
            throw new ConfigurationException("Unable to find the createvm.sh");
        }

        manageSnapshotPath = Script.findScript(storageScriptsDir, "managesnapshot.sh");
        if (manageSnapshotPath == null) {
            throw new ConfigurationException("Unable to find the managesnapshot.sh");
        }

        resizeVolumePath = Script.findScript(storageScriptsDir, "resizevolume.sh");
        if (resizeVolumePath == null) {
            throw new ConfigurationException("Unable to find the resizevolume.sh");
        }

        createTmplPath = Script.findScript(storageScriptsDir, "createtmplt.sh");
        if (createTmplPath == null) {
            throw new ConfigurationException("Unable to find the createtmplt.sh");
        }

        securityGroupPath = Script.findScript(networkScriptsDir, "security_group.py");
        if (securityGroupPath == null) {
            throw new ConfigurationException("Unable to find the security_group.py");
        }

        ovsTunnelPath = Script.findScript(networkScriptsDir, "ovstunnel.py");
        if (ovsTunnelPath == null) {
            throw new ConfigurationException("Unable to find the ovstunnel.py");
        }

        routerProxyPath = Script.findScript("scripts/network/domr/", "router_proxy.sh");
        if (routerProxyPath == null) {
            throw new ConfigurationException("Unable to find the router_proxy.sh");
        }

        ovsPvlanDhcpHostPath = Script.findScript(networkScriptsDir, "ovs-pvlan-dhcp-host.sh");
        if (ovsPvlanDhcpHostPath == null) {
            throw new ConfigurationException("Unable to find the ovs-pvlan-dhcp-host.sh");
        }

        ovsPvlanVmPath = Script.findScript(networkScriptsDir, "ovs-pvlan-vm.sh");
        if (ovsPvlanVmPath == null) {
            throw new ConfigurationException("Unable to find the ovs-pvlan-vm.sh");
        }

        String value = (String) params.get("developer");
        final boolean isDeveloper = Boolean.parseBoolean(value);

        if (isDeveloper) {
            params.putAll(getDeveloperProperties());
        }

        pool = (String) params.get("pool");
        if (pool == null) {
            pool = "/root";
        }

        final String instance = (String) params.get("instance");

        hypervisorType = HypervisorType.getType((String) params.get("hypervisor.type"));
        if (hypervisorType == HypervisorType.None) {
            hypervisorType = HypervisorType.KVM;
        }

        hypervisorUri = (String) params.get("hypervisor.uri");
        if (hypervisorUri == null) {
            hypervisorUri = LibvirtConnection.getHypervisorUri(hypervisorType.toString());
        }

        networkDirectSourceMode = (String) params.get("network.direct.source.mode");
        networkDirectDevice = (String) params.get("network.direct.device");

        String startMac = (String) params.get("private.macaddr.start");
        if (startMac == null) {
            startMac = "00:16:3e:77:e2:a0";
        }

        String startIp = (String) params.get("private.ipaddr.start");
        if (startIp == null) {
            startIp = "192.168.166.128";
        }

        pingTestPath = Script.findScript(kvmScriptsDir, "pingtest.sh");
        if (pingTestPath == null) {
            throw new ConfigurationException("Unable to find the pingtest.sh");
        }

        linkLocalBridgeName = (String) params.get("private.bridge.name");
        if (linkLocalBridgeName == null) {
            if (isDeveloper) {
                linkLocalBridgeName = "cloud-" + instance + "-0";
            } else {
                linkLocalBridgeName = "cloud0";
            }
        }

        publicBridgeName = (String) params.get("public.network.device");
        if (publicBridgeName == null) {
            publicBridgeName = "cloudbr0";
        }

        privBridgeName = (String) params.get("private.network.device");
        if (privBridgeName == null) {
            privBridgeName = "cloudbr1";
        }

        guestBridgeName = (String) params.get("guest.network.device");
        if (guestBridgeName == null) {
            guestBridgeName = privBridgeName;
        }

        privNwName = (String) params.get("private.network.name");
        if (privNwName == null) {
            if (isDeveloper) {
                privNwName = "cloud-" + instance + "-private";
            } else {
                privNwName = "cloud-private";
            }
        }

        localStoragePath = (String) params.get("local.storage.path");
        if (localStoragePath == null) {
            localStoragePath = "/var/lib/libvirt/images/";
        }

        final File storagePath = new File(localStoragePath);
        localStoragePath = storagePath.getAbsolutePath();

        localStorageUuid = (String) params.get("local.storage.uuid");
        if (localStorageUuid == null) {
            localStorageUuid = UUID.randomUUID().toString();
        }

        value = (String) params.get("scripts.timeout");
        timeout = NumbersUtil.parseInt(value, 30 * 60) * 1000;

        value = (String) params.get("stop.script.timeout");
        stopTimeout = NumbersUtil.parseInt(value, 120) * 1000;

        value = (String) params.get("cmds.timeout");
        cmdsTimeout = NumbersUtil.parseInt(value, 7200) * 1000;

        value = (String) params.get("vm.memballoon.disable");
        if (Boolean.parseBoolean(value)) {
            noMemBalloon = true;
        }

        videoHw = (String) params.get("vm.video.hardware");
        value = (String) params.get("vm.video.ram");
        videoRam = NumbersUtil.parseInt(value, 0);

        value = (String) params.get("host.reserved.mem.mb");
        // Reserve 1GB unless admin overrides
        dom0MinMem = NumbersUtil.parseInt(value, 1024) * 1024 * 1024L;

        value = (String) params.get("kvmclock.disable");
        if (Boolean.parseBoolean(value)) {
            noKvmClock = true;
        }

        value = (String) params.get("vm.rng.enable");
        if (Boolean.parseBoolean(value)) {
            rngEnable = true;
            value = (String) params.get("vm.rng.model");
            if (!Strings.isNullOrEmpty(value)) {
                rngBackendModel = RngBackendModel.valueOf(value.toUpperCase());
            }
            value = (String) params.get("vm.rng.path");
            if (!Strings.isNullOrEmpty(value)) {
                rngPath = value;
            }
        }

        value = (String) params.get("vm.watchdog.model");
        if (!Strings.isNullOrEmpty(value)) {
            watchDogModel = WatchDogModel.valueOf(value.toUpperCase());
        }

        value = (String) params.get("vm.watchdog.action");
        if (!Strings.isNullOrEmpty(value)) {
            watchDogAction = WatchDogAction.valueOf(value.toUpperCase());
        }

        LibvirtConnection.initialize(hypervisorUri);
        Connect conn = null;
        try {
            conn = LibvirtConnection.getConnection();

            if (bridgeType == BridgeType.OPENVSWITCH) {
                if (conn.getLibVirVersion() < 10 * 1000 + 0) {
                    throw new ConfigurationException("Libvirt version 0.10.0 required for openvswitch support, but version "
                            + conn.getLibVirVersion() + " detected");
                }
            }
        } catch (final LibvirtException e) {
            throw new CloudRuntimeException(e.getMessage());
        }

        if (HypervisorType.KVM == hypervisorType) {
      /* Does node support HVM guest? If not, exit */
            if (!isHvmEnabled(conn)) {
                throw new ConfigurationException("NO HVM support on this machine, please make sure: "
                        + "1. VT/SVM is supported by your CPU, or is enabled in BIOS. "
                        + "2. kvm modules are loaded (kvm, kvm_amd|kvm_intel)");
            }
        }

        hypervisorPath = getHypervisorPath(conn);
        try {
            hvVersion = conn.getVersion();
            hvVersion = hvVersion % 1000000 / 1000;
            hypervisorLibvirtVersion = conn.getLibVirVersion();
            hypervisorQemuVersion = conn.getVersion();
        } catch (final LibvirtException e) {
            LOGGER.trace("Ignoring libvirt error.", e);
        }

        guestCpuMode = (String) params.get("guest.cpu.mode");
        if (guestCpuMode != null) {
            guestCpuModel = (String) params.get("guest.cpu.model");

            if (hypervisorLibvirtVersion < 9 * 1000 + 10) {
                LOGGER.warn("Libvirt version 0.9.10 required for guest cpu mode, but version "
                        + prettyVersion(hypervisorLibvirtVersion)
                        + " detected, so it will be disabled");
                guestCpuMode = "";
                guestCpuModel = "";
            }
            params.put("guest.cpu.mode", guestCpuMode);
            params.put("guest.cpu.model", guestCpuModel);
        }

        final String guestCpuFeatures = (String) params.get("guest.cpu.features");
        if (guestCpuFeatures != null) {
            cpuFeatures = new ArrayList<>();
            for (final String feature : guestCpuFeatures.split(" ")) {
                if (!feature.isEmpty()) {
                    cpuFeatures.add(feature);
                }
            }
        }

        final String[] info = NetUtils.getNetworkParams(_privateNic);

        monitor = new KvmHaMonitor(null, info[0], heartBeatPath);
        final Thread ha = new Thread(monitor);
        ha.start();

        storagePoolMgr = new KvmStoragePoolManager(storage, monitor);

        sysvmIsoPath = (String) params.get("systemvm.iso.path");
        if (sysvmIsoPath == null) {
            final String[] isoPaths = {"/usr/share/cosmic-common/vms/systemvm.iso"};
            for (final String isoPath : isoPaths) {
                if (storage.exists(isoPath)) {
                    sysvmIsoPath = isoPath;
                    break;
                }
            }
            if (sysvmIsoPath == null) {
                LOGGER.debug("Can't find system vm ISO");
            }
        }

        switch (bridgeType) {
            case OPENVSWITCH:
                getOvsPifs();
                break;
            case NATIVE:
            default:
                getPifs();
                break;
        }

        if (pifs.get("private") == null) {
            LOGGER.debug("Failed to get private nic name");
            throw new ConfigurationException("Failed to get private nic name");
        }

        if (pifs.get("public") == null) {
            LOGGER.debug("Failed to get public nic name");
            throw new ConfigurationException("Failed to get public nic name");
        }
        LOGGER.debug("Found pif: " + pifs.get("private") + " on " + privBridgeName + ", pif: " + pifs.get("public") + " on "
                + publicBridgeName);

        canBridgeFirewall = canBridgeFirewall(pifs.get("public"));

        localGateway = Script.runSimpleBashScript("ip route |grep default|awk '{print $3}'");
        if (localGateway == null) {
            LOGGER.debug("Failed to found the local gateway");
        }

        mountPoint = (String) params.get("mount.path");
        if (mountPoint == null) {
            mountPoint = "/mnt";
        }

        value = (String) params.get("vm.migrate.downtime");
        migrateDowntime = NumbersUtil.parseInt(value, -1);

        value = (String) params.get("vm.migrate.pauseafter");
        migratePauseAfter = NumbersUtil.parseInt(value, -1);

        value = (String) params.get("vm.migrate.speed");
        migrateSpeed = NumbersUtil.parseInt(value, -1);
        if (migrateSpeed == -1) {
            // get guest network device speed
            migrateSpeed = 0;
            final String speed = Script.runSimpleBashScript(
                    "ethtool " + pifs.get("public") + " |grep Speed | cut -d \\  -f 2");
            if (speed != null) {
                final String[] tokens = speed.split("M");
                if (tokens.length == 2) {
                    try {
                        migrateSpeed = Integer.parseInt(tokens[0]);
                    } catch (final NumberFormatException e) {
                        LOGGER.trace("Ignoring migrateSpeed extraction error.", e);
                    }
                    LOGGER.debug("device " + pifs.get("public") + " has speed: " + String.valueOf(migrateSpeed));
                }
            }
            params.put("vm.migrate.speed", String.valueOf(migrateSpeed));
        }

        final Map<String, String> bridges = new HashMap<>();
        bridges.put("linklocal", linkLocalBridgeName);
        bridges.put("public", publicBridgeName);
        bridges.put("private", privBridgeName);
        bridges.put("guest", guestBridgeName);

        params.put("libvirt.host.bridges", bridges);
        params.put("libvirt.host.pifs", pifs);

        params.put("libvirt.computing.resource", this);
        params.put("libvirtVersion", hypervisorLibvirtVersion);

        configureVifDrivers(params);
        configureDiskActivityChecks(params);

        final KvmStorageProcessor storageProcessor = new KvmStorageProcessor(storagePoolMgr, this);
        storageProcessor.configure(name, params);
        storageHandler = new StorageSubsystemCommandHandlerBase(storageProcessor);

        final String unameKernelVersion = Script.runSimpleBashScript("uname -r");
        final String[] kernelVersions = unameKernelVersion.split("[\\.\\-]");
        kernelVersion = Integer.parseInt(kernelVersions[0]) * 1000 * 1000
                + (long) Integer.parseInt(kernelVersions[1]) * 1000 + Integer.parseInt(kernelVersions[2]);

    /*
     * Disable this, the code using this is pretty bad and non portable getOsVersion();
     */
        return true;
    }

    protected String getDefaultDomrScriptsDir() {
        return "scripts/network/domr";
    }

    protected String getDefaultHypervisorScriptsDir() {
        return "scripts/vm/hypervisor";
    }

    protected String getDefaultKvmScriptsDir() {
        return "scripts/vm/hypervisor/kvm";
    }

    protected String getDefaultNetworkScriptsDir() {
        return "scripts/vm/network/vnet";
    }

    protected String getDefaultStorageScriptsDir() {
        return "scripts/storage/qcow2";
    }

    private Map<String, Object> getDeveloperProperties() throws ConfigurationException {

        final File file = PropertiesUtil.findConfigFile("developer.properties");
        if (file == null) {
            throw new ConfigurationException("Unable to find developer.properties.");
        }

        LOGGER.info("developer.properties found at " + file.getAbsolutePath());
        try {
            final Properties properties = PropertiesUtil.loadFromFile(file);

            final String startMac = (String) properties.get("private.macaddr.start");
            if (startMac == null) {
                throw new ConfigurationException("Developers must specify start mac for private ip range");
            }

            final String startIp = (String) properties.get("private.ipaddr.start");
            if (startIp == null) {
                throw new ConfigurationException("Developers must specify start ip for private ip range");
            }
            final Map<String, Object> params = PropertiesUtil.toMap(properties);

            String endIp = (String) properties.get("private.ipaddr.end");
            if (endIp == null) {
                endIp = getEndIpFromStartIp(startIp, 16);
                params.put("private.ipaddr.end", endIp);
            }
            return params;
        } catch (final FileNotFoundException ex) {
            throw new CloudRuntimeException("Cannot find the file: " + file.getAbsolutePath(), ex);
        } catch (final IOException ex) {
            throw new CloudRuntimeException("IOException in reading " + file.getAbsolutePath(), ex);
        }
    }

    private boolean isHvmEnabled(final Connect conn) {
        final LibvirtCapXmlParser parser = new LibvirtCapXmlParser();
        try {
            parser.parseCapabilitiesXml(conn.getCapabilities());
            final ArrayList<String> osTypes = parser.getGuestOsType();
            for (final String o : osTypes) {
                if (o.equalsIgnoreCase("hvm")) {
                    return true;
                }
            }
        } catch (final LibvirtException e) {
            LOGGER.trace("Ignoring libvirt error.", e);
        }
        return false;
    }

    private String getHypervisorPath(final Connect conn) {
        final LibvirtCapXmlParser parser = new LibvirtCapXmlParser();
        try {
            parser.parseCapabilitiesXml(conn.getCapabilities());
        } catch (final LibvirtException e) {
            LOGGER.debug(e.getMessage());
        }
        return parser.getEmulator();
    }

    @Override
    public Answer executeRequest(final Command cmd) {

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        try {
            return wrapper.execute(cmd, this);
        } catch (final Exception e) {
            return Answer.createUnsupportedCommandAnswer(cmd);
        }
    }

    private String prettyVersion(final long version) {
        final long major = version / 1000000;
        final long minor = version % 1000000 / 1000;
        final long release = version % 1000000 % 1000;
        return major + "." + minor + "." + release;
    }

    private void getOvsPifs() {
        final String cmdout = Script.runSimpleBashScript("ovs-vsctl list-br | sed '{:q;N;s/\\n/%/g;t q}'");
        LOGGER.debug("cmdout was " + cmdout);
        final List<String> bridges = Arrays.asList(cmdout.split("%"));
        for (final String bridge : bridges) {
            LOGGER.debug("looking for pif for bridge " + bridge);
            // String pif = getOvsPif(bridge);
            // Not really interested in the pif name at this point for ovs
            // bridges
            final String pif = bridge;
            if (publicBridgeName != null && bridge.equals(publicBridgeName)) {
                pifs.put("public", pif);
            }
            if (guestBridgeName != null && bridge.equals(guestBridgeName)) {
                pifs.put("private", pif);
            }
            pifs.put(bridge, pif);
        }
        LOGGER.debug("done looking for pifs, no more bridges");
    }

    private void getPifs() {
        final File dir = new File("/sys/devices/virtual/net");
        final File[] netdevs = dir.listFiles();
        final List<String> bridges = new ArrayList<>();
        for (final File netdev : netdevs) {
            final File isbridge = new File(netdev.getAbsolutePath() + "/bridge");
            final String netdevName = netdev.getName();
            LOGGER.debug("looking in file " + netdev.getAbsolutePath() + "/bridge");
            if (isbridge.exists()) {
                LOGGER.debug("Found bridge " + netdevName);
                bridges.add(netdevName);
            }
        }

        for (final String bridge : bridges) {
            LOGGER.debug("looking for pif for bridge " + bridge);
            final String pif = getPif(bridge);
            if (publicBridgeName != null && bridge.equals(publicBridgeName)) {
                pifs.put("public", pif);
            }
            if (guestBridgeName != null && bridge.equals(guestBridgeName)) {
                pifs.put("private", pif);
            }
            pifs.put(bridge, pif);
        }

        // guest(private) creates bridges on a pif, if private bridge not found try pif direct
        // This addresses the unnecessary requirement of someone to create an unused bridge just for traffic label
        if (pifs.get("private") == null) {
            LOGGER.debug("guest(private) traffic label '" + guestBridgeName
                    + "' not found as bridge, looking for physical interface");
            final File dev = new File("/sys/class/net/" + guestBridgeName);
            if (dev.exists()) {
                LOGGER.debug("guest(private) traffic label '" + guestBridgeName + "' found as a physical device");
                pifs.put("private", guestBridgeName);
            }
        }

        // public creates bridges on a pif, if private bridge not found try pif direct
        // This addresses the unnecessary requirement of someone to create an unused bridge just for traffic label
        if (pifs.get("public") == null) {
            LOGGER.debug(
                    "public traffic label '" + publicBridgeName + "' not found as bridge, looking for physical interface");
            final File dev = new File("/sys/class/net/" + publicBridgeName);
            if (dev.exists()) {
                LOGGER.debug("public traffic label '" + publicBridgeName + "' found as a physical device");
                pifs.put("public", publicBridgeName);
            }
        }

        LOGGER.debug("done looking for pifs, no more bridges");
    }

    private boolean canBridgeFirewall(final String prvNic) {
        final Script cmd = new Script(securityGroupPath, timeout, LOGGER);
        cmd.add("can_bridge_firewall");
        cmd.add(prvNic);
        final String result = cmd.execute();
        if (result != null) {
            return false;
        }
        return true;
    }

    protected void configureVifDrivers(final Map<String, Object> params) throws ConfigurationException {
        final String libvirtVifDriver = "libvirt.vif.driver";

        trafficTypeVifDrivers = new HashMap<>();

        // Load the default vif driver
        String defaultVifDriverName = (String) params.get(libvirtVifDriver);
        if (defaultVifDriverName == null) {
            if (bridgeType == BridgeType.OPENVSWITCH) {
                LOGGER.info("No libvirt.vif.driver specified. Defaults to OvsVifDriver.");
                defaultVifDriverName = DEFAULT_OVS_VIF_DRIVER_CLASS;
            } else {
                LOGGER.info("No libvirt.vif.driver specified. Defaults to BridgeVifDriver.");
                defaultVifDriverName = DEFAULT_BRIDGE_VIF_DRIVER_CLASS;
            }
        }
        defaultVifDriver = getVifDriverClass(defaultVifDriverName, params);

        // Load any per-traffic-type vif drivers
        for (final Map.Entry<String, Object> entry : params.entrySet()) {
            final String k = entry.getKey();
            final String vifDriverPrefix = libvirtVifDriver + ".";

            if (k.startsWith(vifDriverPrefix)) {
                // Get trafficType
                final String trafficTypeSuffix = k.substring(vifDriverPrefix.length());

                // Does this suffix match a real traffic type?
                final TrafficType trafficType = TrafficType.getTrafficType(trafficTypeSuffix);
                if (!trafficType.equals(TrafficType.None)) {
                    // Get vif driver class name
                    final String vifDriverClassName = (String) entry.getValue();
                    // if value is null, ignore
                    if (vifDriverClassName != null) {
                        // add traffic type to vif driver mapping to Map
                        trafficTypeVifDrivers.put(trafficType, getVifDriverClass(vifDriverClassName, params));
                    }
                }
            }
        }
    }

    protected void configureDiskActivityChecks(final Map<String, Object> params) {
        diskActivityCheckEnabled = Boolean.parseBoolean((String) params.get("vm.diskactivity.checkenabled"));
        if (diskActivityCheckEnabled) {
            final int timeout = NumbersUtil.parseInt((String) params.get("vm.diskactivity.checktimeout_s"), 0);
            if (timeout > 0) {
                diskActivityCheckTimeoutSeconds = timeout;
            }
            final long inactiveTime = NumbersUtil.parseLong((String) params.get("vm.diskactivity.inactivetime_ms"), 0L);
            if (inactiveTime > 0) {
                diskActivityInactiveThresholdMilliseconds = inactiveTime;
            }
        }
    }

    private String getEndIpFromStartIp(final String startIp, final int numIps) {
        final String[] tokens = startIp.split("[.]");
        assert tokens.length == 4;
        int lastbyte = Integer.parseInt(tokens[3]);
        lastbyte = lastbyte + numIps;
        tokens[3] = Integer.toString(lastbyte);
        final StringBuilder end = new StringBuilder(15);
        end.append(tokens[0]).append(".").append(tokens[1]).append(".").append(tokens[2]).append(".").append(tokens[3]);
        return end.toString();
    }

    private String getPif(final String bridge) {
        String pif = matchPifFileInDirectory(bridge);
        final File vlanfile = new File("/proc/net/vlan/" + pif);

        if (vlanfile.isFile()) {
            pif = Script.runSimpleBashScript("grep ^Device\\: /proc/net/vlan/" + pif + " | awk {'print $2'}");
        }

        return pif;
    }

    protected VifDriver getVifDriverClass(final String vifDriverClassName, final Map<String, Object> params)
            throws ConfigurationException {
        final VifDriver vifDriver;

        try {
            final Class<?> clazz = Class.forName(vifDriverClassName);
            vifDriver = (VifDriver) clazz.newInstance();
            vifDriver.configure(params);
        } catch (final ClassNotFoundException e) {
            throw new ConfigurationException("Unable to find class for libvirt.vif.driver " + e);
        } catch (final InstantiationException e) {
            throw new ConfigurationException("Unable to instantiate class for libvirt.vif.driver " + e);
        } catch (final IllegalAccessException e) {
            throw new ConfigurationException("Unable to instantiate class for libvirt.vif.driver " + e);
        }
        return vifDriver;
    }

    @Override
    public boolean stop() {
        try {
            final Connect conn = LibvirtConnection.getConnection();
            conn.close();
        } catch (final LibvirtException e) {
            LOGGER.trace("Ignoring libvirt error.", e);
        }

        return true;
    }

    protected String getNetworkDirectSourceMode() {
        return networkDirectSourceMode;
    }

    protected String getNetworkDirectDevice() {
        return networkDirectDevice;
    }

    public boolean passCmdLine(final String vmName, final String cmdLine) throws InternalErrorException {
        final Script command = new Script(patchViaSocketPath, 5 * 1000, LOGGER);
        final String result;
        command.add("-n", vmName);
        command.add("-p", cmdLine.replaceAll(" ", "%"));
        result = command.execute();
        if (result != null) {
            LOGGER.debug("passcmd failed:" + result);
            return false;
        }
        return true;
    }

    boolean isDirectAttachedNetwork(final String type) {
        if ("untagged".equalsIgnoreCase(type)) {
            return true;
        } else {
            try {
                Long.valueOf(type);
            } catch (final NumberFormatException e) {
                return true;
            }
            return false;
        }
    }

    public synchronized boolean destroyTunnelNetwork(final String bridge) {
        findOrCreateTunnelNetwork(bridge);

        final Script cmd = new Script(ovsTunnelPath, timeout, LOGGER);
        cmd.add("destroy_ovs_bridge");
        cmd.add("--bridge", bridge);

        final String result = cmd.execute();

        if (result != null) {
            LOGGER.debug("OVS Bridge could not be destroyed due to error ==> " + result);
            return false;
        }
        return true;
    }

    public synchronized boolean findOrCreateTunnelNetwork(final String nwName) {
        try {
            if (checkNetwork(nwName)) {
                return true;
            }
            // if not found, create a new one
            final Map<String, String> otherConfig = new HashMap<>();
            otherConfig.put("ovs-host-setup", "");
            Script.runSimpleBashScript("ovs-vsctl -- --may-exist add-br "
                    + nwName + " -- set bridge " + nwName
                    + " other_config:ovs-host-setup='-1'");
            LOGGER.debug("### KVM network for tunnels created:" + nwName);
        } catch (final Exception e) {
            LOGGER.warn("createTunnelNetwork failed", e);
            return false;
        }
        return true;
    }

    public boolean checkNetwork(final String networkName) {
        if (networkName == null) {
            return true;
        }

        if (bridgeType == BridgeType.OPENVSWITCH) {
            return checkOvsNetwork(networkName);
        } else {
            return checkBridgeNetwork(networkName);
        }
    }

    private boolean checkOvsNetwork(final String networkName) {
        LOGGER.debug("Checking if network " + networkName + " exists as openvswitch bridge");
        if (networkName == null) {
            return true;
        }

        final Script command = new Script("/bin/sh", timeout);
        command.add("-c");
        command.add("ovs-vsctl br-exists " + networkName);
        return "0".equals(command.execute(null));
    }

    private boolean checkBridgeNetwork(final String networkName) {
        if (networkName == null) {
            return true;
        }

        final String name = matchPifFileInDirectory(networkName);

        if (name == null || name.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    public synchronized boolean configureTunnelNetwork(final long networkId,
                                                       final long hostId, final String nwName) {
        try {
            final boolean findResult = findOrCreateTunnelNetwork(nwName);
            if (!findResult) {
                LOGGER.warn(
                        "LibvirtComputingResource.findOrCreateTunnelNetwork() failed! Cannot proceed creating the tunnel.");
                return false;
            }
            final String configuredHosts = Script.runSimpleBashScript("ovs-vsctl get bridge " + nwName
                    + " other_config:ovs-host-setup");
            boolean configured = false;
            if (configuredHosts != null) {
                final String[] hostIdsStr = configuredHosts.split(",");
                for (final String hostIdStr : hostIdsStr) {
                    if (hostIdStr.equals(((Long) hostId).toString())) {
                        configured = true;
                        break;
                    }
                }
            }
            if (!configured) {
                final Script cmd = new Script(ovsTunnelPath, timeout, LOGGER);
                cmd.add("setup_ovs_bridge");
                cmd.add("--key", nwName);
                cmd.add("--cs_host_id", ((Long) hostId).toString());
                cmd.add("--bridge", nwName);
                final String result = cmd.execute();
                if (result != null) {
                    throw new CloudRuntimeException(
                            "Unable to pre-configure OVS bridge " + nwName
                                    + " for network ID:" + networkId);
                }
            }
        } catch (final Exception e) {
            LOGGER.warn("createandConfigureTunnelNetwork failed", e);
            return false;
        }
        return true;
    }

    protected Storage.StorageResourceType getStorageResourceType() {
        return Storage.StorageResourceType.STORAGE_POOL;
    }

    // this is much like PrimaryStorageDownloadCommand, but keeping it separate
    public KvmPhysicalDisk templateToPrimaryDownload(final String templateUrl, final KvmStoragePool primaryPool,
                                                     final String volUuid) {
        final int index = templateUrl.lastIndexOf("/");
        final String mountpoint = templateUrl.substring(0, index);
        String templateName = null;
        if (index < templateUrl.length() - 1) {
            templateName = templateUrl.substring(index + 1);
        }

        KvmPhysicalDisk templateVol = null;
        KvmStoragePool secondaryPool = null;
        try {
            secondaryPool = storagePoolMgr.getStoragePoolByUri(mountpoint);
      /* Get template vol */
            if (templateName == null) {
                secondaryPool.refresh();
                final List<KvmPhysicalDisk> disks = secondaryPool.listPhysicalDisks();
                if (disks == null || disks.isEmpty()) {
                    LOGGER.error("Failed to get volumes from pool: " + secondaryPool.getUuid());
                    return null;
                }
                for (final KvmPhysicalDisk disk : disks) {
                    if (disk.getName().endsWith("qcow2")) {
                        templateVol = disk;
                        break;
                    }
                }
                if (templateVol == null) {
                    LOGGER.error("Failed to get template from pool: " + secondaryPool.getUuid());
                    return null;
                }
            } else {
                templateVol = secondaryPool.getPhysicalDisk(templateName);
            }

      /* Copy volume to primary storage */

            final KvmPhysicalDisk primaryVol = storagePoolMgr.copyPhysicalDisk(templateVol, volUuid, primaryPool, 0);
            return primaryVol;
        } catch (final CloudRuntimeException e) {
            LOGGER.error("Failed to download template to primary storage", e);
            return null;
        } finally {
            if (secondaryPool != null) {
                storagePoolMgr.deleteStoragePool(secondaryPool.getType(), secondaryPool.getUuid());
            }
        }
    }

    public String getResizeScriptType(final KvmStoragePool pool, final KvmPhysicalDisk vol) {
        final StoragePoolType poolType = pool.getType();
        final PhysicalDiskFormat volFormat = vol.getFormat();

        if (pool.getType() == StoragePoolType.CLVM && volFormat == PhysicalDiskFormat.RAW) {
            return "CLVM";
        } else if ((poolType == StoragePoolType.NetworkFilesystem
                || poolType == StoragePoolType.SharedMountPoint
                || poolType == StoragePoolType.Filesystem
                || poolType == StoragePoolType.Gluster)
                && volFormat == PhysicalDiskFormat.QCOW2) {
            return "QCOW2";
        }
        throw new CloudRuntimeException("Cannot determine resize type from pool type " + pool.getType());
    }

    public PowerState getVmState(final Connect conn, final String vmName) {
        int retry = 3;
        Domain vms = null;
        while (retry-- > 0) {
            try {
                vms = conn.domainLookupByName(vmName);
                final PowerState s = convertToPowerState(vms.getInfo().state);
                return s;
            } catch (final LibvirtException e) {
                LOGGER.warn("Can't get vm state " + vmName + e.getMessage() + "retry:" + retry);
            } finally {
                try {
                    if (vms != null) {
                        vms.free();
                    }
                } catch (final LibvirtException l) {
                    LOGGER.trace("Ignoring libvirt error.", l);
                }
            }
        }
        return PowerState.PowerOff;
    }

    protected PowerState convertToPowerState(final DomainState ps) {
        final PowerState state = s_powerStatesTable.get(ps);
        return state == null ? PowerState.PowerUnknown : state;
    }

    public long[] getNetworkStats(final String privateIp) {
        final String result = networkUsage(privateIp, "get", null);
        final long[] stats = new long[2];
        if (result != null) {
            final String[] splitResult = result.split(":");
            int index = 0;
            while (index < splitResult.length - 1) {
                stats[0] += Long.parseLong(splitResult[index++]);
                stats[1] += Long.parseLong(splitResult[index++]);
            }
        }
        return stats;
    }

    public long[] getVpcNetworkStats(final String privateIp, final String publicIp, final String option) {
        final String result = configureVpcNetworkUsage(privateIp, publicIp, option, null);
        final long[] stats = new long[2];
        if (result != null) {
            final String[] splitResult = result.split(":");
            int index = 0;
            while (index < splitResult.length - 1) {
                stats[0] += Long.parseLong(splitResult[index++]);
                stats[1] += Long.parseLong(splitResult[index++]);
            }
        }
        return stats;
    }

    public String configureVpcNetworkUsage(final String privateIpAddress, final String publicIp, final String option,
                                           final String vpcCidr) {
        final Script getUsage = new Script(routerProxyPath, LOGGER);
        getUsage.add("vpc_netusage.sh");
        getUsage.add(privateIpAddress);
        getUsage.add("-l", publicIp);

        if (option.equals("get")) {
            getUsage.add("-g");
        } else if (option.equals("create")) {
            getUsage.add("-c");
            getUsage.add("-v", vpcCidr);
        } else if (option.equals("reset")) {
            getUsage.add("-r");
        } else if (option.equals("vpn")) {
            getUsage.add("-n");
        } else if (option.equals("remove")) {
            getUsage.add("-d");
        }

        final OutputInterpreter.OneLineParser usageParser = new OutputInterpreter.OneLineParser();
        final String result = getUsage.execute(usageParser);
        if (result != null) {
            LOGGER.debug("Failed to execute VPCNetworkUsage:" + result);
            return null;
        }
        return usageParser.getLine();
    }

    public void handleVmStartFailure(final Connect conn, final String vmName, final LibvirtVmDef vm) {
        if (vm != null && vm.getDevices() != null) {
            cleanupVmNetworks(conn, vm.getDevices().getInterfaces());
        }
    }

    private void cleanupVmNetworks(final Connect conn, final List<InterfaceDef> nics) {
        if (nics != null) {
            for (final InterfaceDef nic : nics) {
                for (final VifDriver vifDriver : getAllVifDrivers()) {
                    vifDriver.unplug(nic);
                }
            }
        }
    }

    public LibvirtVmDef createVmFromSpec(final VirtualMachineTO vmTo) {
        final LibvirtVmDef vm = new LibvirtVmDef();
        vm.setDomainName(vmTo.getName());
        String uuid = vmTo.getUuid();
        uuid = getUuid(uuid);
        vm.setDomUuid(uuid);
        vm.setDomDescription(vmTo.getOs());
        vm.setPlatformEmulator(vmTo.getPlatformEmulator());

        final GuestDef guest = new GuestDef();

        guest.setGuestType(GuestDef.GuestType.KVM);
        vm.setHvsType(HypervisorType.KVM.toString().toLowerCase());
        vm.setLibvirtVersion(hypervisorLibvirtVersion);
        vm.setQemuVersion(hypervisorQemuVersion);

        guest.setGuestArch(vmTo.getArch());
        guest.setMachineType("pc");
        guest.setUuid(uuid);
        guest.setBootOrder(GuestDef.BootOrder.CDROM);
        guest.setBootOrder(GuestDef.BootOrder.HARDISK);

        vm.addComp(guest);

        final GuestResourceDef grd = new GuestResourceDef();

        if (vmTo.getMinRam() != vmTo.getMaxRam() && !noMemBalloon) {
            grd.setMemBalloning(true);
            grd.setCurrentMem(vmTo.getMinRam() / 1024);
            grd.setMemorySize(vmTo.getMaxRam() / 1024);
        } else {
            grd.setMemorySize(vmTo.getMaxRam() / 1024);
        }
        final int vcpus = vmTo.getCpus();
        grd.setVcpuNum(vcpus);
        vm.addComp(grd);

        final CpuModeDef cmd = new CpuModeDef();
        cmd.setMode(guestCpuMode);
        cmd.setModel(guestCpuModel);
        if (vmTo.getType() == VirtualMachine.Type.User) {
            cmd.setFeatures(cpuFeatures);
        }
        // multi cores per socket, for larger core configs
        if (vcpus % 6 == 0) {
            final int sockets = vcpus / 6;
            cmd.setTopology(6, sockets);
        } else if (vcpus % 4 == 0) {
            final int sockets = vcpus / 4;
            cmd.setTopology(4, sockets);
        }
        vm.addComp(cmd);

        if (hypervisorLibvirtVersion >= 9000) {
            final CpuTuneDef ctd = new CpuTuneDef();
            if (vmTo.getMinSpeed() != null) {
                ctd.setShares(vmTo.getCpus() * vmTo.getMinSpeed());
            } else {
                ctd.setShares(vmTo.getCpus() * vmTo.getSpeed());
            }
            vm.addComp(ctd);
        }

        final FeaturesDef features = new FeaturesDef();
        features.addFeatures("pae");
        features.addFeatures("apic");
        features.addFeatures("acpi");
        // for rhel 6.5 and above, hyperv enlightment feature is added
    /*
     * if (vmTO.getOs().contains("Windows Server 2008") && hostOsVersion != null && ((hostOsVersion.first() == 6 &&
     * hostOsVersion.second() >= 5) || (hostOsVersion.first() >= 7))) { LibvirtVMDef.HyperVEnlightenmentFeatureDef hyv =
     * new LibvirtVMDef.HyperVEnlightenmentFeatureDef(); hyv.setRelaxed(true); features.addHyperVFeature(hyv); }
     */
        vm.addComp(features);

        final TermPolicy term = new TermPolicy();
        term.setCrashPolicy("destroy");
        term.setPowerOffPolicy("destroy");
        term.setRebootPolicy("restart");
        vm.addComp(term);

        final ClockDef clock = new ClockDef();
        if (vmTo.getOs().startsWith("Windows")) {
            clock.setClockOffset(ClockDef.ClockOffset.LOCALTIME);
            clock.setTimer("rtc", "catchup", null);
        } else if (vmTo.getType() != VirtualMachine.Type.User || isGuestPvEnabled(vmTo.getOs())) {
            if (hypervisorLibvirtVersion >= 9 * 1000 + 10) {
                clock.setTimer("kvmclock", null, null, noKvmClock);
            }
        }

        vm.addComp(clock);

        final DevicesDef devices = new DevicesDef();
        devices.setEmulatorPath(hypervisorPath);
        devices.setGuestType(guest.getGuestType());

        final SerialDef serial = new SerialDef("pty", null, (short) 0);
        devices.addDevice(serial);

        if (vmTo.getType() != VirtualMachine.Type.User) {
            final VirtioSerialDef vserial = new VirtioSerialDef(vmTo.getName(), null);
            devices.addDevice(vserial);
        }

        final QemuGuestAgentDef guestagent = new QemuGuestAgentDef();
        devices.addDevice(guestagent);

        if (rngEnable) {
            final RngDef rngDevice = new RngDef(rngPath, rngBackendModel);
            devices.addDevice(rngDevice);
        }

        final WatchDogDef watchDog = new WatchDogDef(watchDogAction, watchDogModel);
        devices.addDevice(watchDog);

        final VideoDef videoCard = new VideoDef(videoHw, videoRam);
        devices.addDevice(videoCard);

        final ConsoleDef console = new ConsoleDef("pty", null, null, (short) 0);
        devices.addDevice(console);

        // add the VNC port passwd here, get the passwd from the vmInstance.
        final String passwd = vmTo.getVncPassword();
        final GraphicDef grap = new GraphicDef("vnc", (short) 0, true, vmTo.getVncAddr(), passwd, null);
        devices.addDevice(grap);

        final InputDef input = new InputDef("tablet", "usb");
        devices.addDevice(input);

        vm.addComp(devices);

        return vm;
    }

    protected String getUuid(String uuid) {
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        } else {
            try {
                final UUID uuid2 = UUID.fromString(uuid);
                final String uuid3 = uuid2.toString();
                if (!uuid3.equals(uuid)) {
                    uuid = UUID.randomUUID().toString();
                }
            } catch (final IllegalArgumentException e) {
                uuid = UUID.randomUUID().toString();
            }
        }
        return uuid;
    }

    boolean isGuestPvEnabled(final String guestOsName) {
        if (guestOsName == null) {
            return false;
        }
        if (guestOsName.startsWith("Ubuntu") || guestOsName.startsWith("Fedora 13")
                || guestOsName.startsWith("Fedora 12")
                || guestOsName.startsWith("Fedora 11")
                || guestOsName.startsWith("Fedora 10") || guestOsName.startsWith("Fedora 9")
                || guestOsName.startsWith("CentOS 5.3") || guestOsName.startsWith("CentOS 5.4")
                || guestOsName.startsWith("CentOS 5.5") || guestOsName.startsWith("CentOS")
                || guestOsName.startsWith("Fedora")
                || guestOsName.startsWith("Red Hat Enterprise Linux 5.3")
                || guestOsName.startsWith("Red Hat Enterprise Linux 5.4")
                || guestOsName.startsWith("Red Hat Enterprise Linux 5.5")
                || guestOsName.startsWith("Red Hat Enterprise Linux 6")
                || guestOsName.startsWith("Debian GNU/Linux")
                || guestOsName.startsWith("FreeBSD 10") || guestOsName.startsWith("Oracle")
                || guestOsName.startsWith("Other PV")) {
            return true;
        } else {
            return false;
        }
    }

    public void createVifs(final VirtualMachineTO vmSpec, final LibvirtVmDef vm)
            throws InternalErrorException, LibvirtException {
        final NicTO[] nics = vmSpec.getNics();
        final Map<String, String> params = vmSpec.getDetails();
        String nicAdapter = "";
        if (params != null && params.get("nicAdapter") != null && !params.get("nicAdapter").isEmpty()) {
            nicAdapter = params.get("nicAdapter");
        }
        for (int i = 0; i < nics.length; i++) {
            for (final NicTO nic : vmSpec.getNics()) {
                if (nic.getDeviceId() == i) {
                    createVif(vm, nic, nicAdapter);
                }
            }
        }
    }

    private void createVif(final LibvirtVmDef vm, final NicTO nic, final String nicAdapter)
            throws InternalErrorException, LibvirtException {

        if (nic.getType().equals(TrafficType.Guest) && nic.getBroadcastType().equals(BroadcastDomainType.Vsp)) {
            final String vrIp = nic.getBroadcastUri().getPath().substring(1);
            vm.getMetaData().getMetadataNode(LibvirtVmDef.NuageExtensionDef.class).addNuageExtension(nic.getMac(), vrIp);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("NIC with MAC " + nic.getMac() + " and BroadcastDomainType " + nic.getBroadcastType()
                        + " in network(" + nic.getGateway() + "/" + nic.getNetmask() + ") is " + nic.getType()
                        + " traffic type. So, vsp-vr-ip " + vrIp + " is set in the metadata");
            }
        }

        vm.getDevices().addDevice(
                getVifDriver(nic.getType()).plug(nic, vm.getPlatformEmulator().toString(), nicAdapter).toString());
    }

    public String getVolumePath(final Connect conn, final DiskTO volume) throws LibvirtException, URISyntaxException {
        final DataTO data = volume.getData();
        final DataStoreTO store = data.getDataStore();

        if (volume.getType() == Volume.Type.ISO && data.getPath() != null) {
            final NfsTO nfsStore = (NfsTO) store;
            final String isoPath = nfsStore.getUrl() + File.separator + data.getPath();
            final int index = isoPath.lastIndexOf("/");
            final String path = isoPath.substring(0, index);
            final String name = isoPath.substring(index + 1);
            final KvmStoragePool secondaryPool = storagePoolMgr.getStoragePoolByUri(path);
            final KvmPhysicalDisk isoVol = secondaryPool.getPhysicalDisk(name);
            return isoVol.getPath();
        } else {
            return data.getPath();
        }
    }

    @Override
    public PingCommand getCurrentStatus(final long id) {

        if (!canBridgeFirewall) {
            return new PingRoutingCommand(com.cloud.host.Host.Type.Routing, id, this.getHostVmStateReport());
        } else {
            final HashMap<String, Pair<Long, Long>> nwGrpStates = syncNetworkGroups(id);
            return new PingRoutingWithNwGroupsCommand(getType(), id, this.getHostVmStateReport(), nwGrpStates);
        }
    }

    public void createVbd(final Connect conn, final VirtualMachineTO vmSpec, final String vmName, final LibvirtVmDef vm)
            throws InternalErrorException, LibvirtException, URISyntaxException {
        final List<DiskTO> disks = Arrays.asList(vmSpec.getDisks());
        Collections.sort(disks, new Comparator<DiskTO>() {
            @Override
            public int compare(final DiskTO arg0, final DiskTO arg1) {
                return arg0.getDiskSeq() > arg1.getDiskSeq() ? 1 : -1;
            }
        });

        for (final DiskTO volume : disks) {
            KvmPhysicalDisk physicalDisk = null;
            KvmStoragePool pool = null;
            final DataTO data = volume.getData();
            if (volume.getType() == Volume.Type.ISO && data.getPath() != null) {
                final NfsTO nfsStore = (NfsTO) data.getDataStore();
                final String volPath = nfsStore.getUrl() + File.separator + data.getPath();
                final int index = volPath.lastIndexOf("/");
                final String volDir = volPath.substring(0, index);
                final String volName = volPath.substring(index + 1);
                final KvmStoragePool secondaryStorage = storagePoolMgr.getStoragePoolByUri(volDir);
                physicalDisk = secondaryStorage.getPhysicalDisk(volName);
            } else if (volume.getType() != Volume.Type.ISO) {
                final PrimaryDataStoreTO store = (PrimaryDataStoreTO) data.getDataStore();
                physicalDisk = storagePoolMgr.getPhysicalDisk(store.getPoolType(), store.getUuid(), data.getPath());
                pool = physicalDisk.getPool();
            }

            String volPath = null;
            if (physicalDisk != null) {
                volPath = physicalDisk.getPath();
            }

            // check for disk activity, if detected we should exit because vm is running elsewhere
            if (diskActivityCheckEnabled && physicalDisk != null && physicalDisk.getFormat() == PhysicalDiskFormat.QCOW2) {
                LOGGER.debug("Checking physical disk file at path " + volPath
                        + " for disk activity to ensure vm is not running elsewhere");
                try {
                    HypervisorUtils.checkVolumeFileForActivity(volPath, diskActivityCheckTimeoutSeconds,
                            diskActivityInactiveThresholdMilliseconds, diskActivityCheckFileSizeMin);
                } catch (final IOException ex) {
                    throw new CloudRuntimeException("Unable to check physical disk file for activity", ex);
                }
                LOGGER.debug("Disk activity check cleared");
            }

            // if params contains a rootDiskController key, use its value (this is what other HVs are doing)
            DiskDef.DiskBus diskBusType = null;
            final Map<String, String> params = vmSpec.getDetails();
            if (params != null && params.get("rootDiskController") != null && !params.get("rootDiskController").isEmpty()) {
                final String rootDiskController = params.get("rootDiskController");
                LOGGER.debug("Passed custom disk bus " + rootDiskController);
                for (final DiskDef.DiskBus bus : DiskDef.DiskBus.values()) {
                    if (bus.toString().equalsIgnoreCase(rootDiskController)) {
                        LOGGER.debug("Found matching enum for disk bus " + rootDiskController);
                        diskBusType = bus;
                        break;
                    }
                }
            }

            if (diskBusType == null) {
                diskBusType = getGuestDiskModel(vmSpec.getPlatformEmulator());
            }
            final DiskDef disk = new DiskDef();
            if (volume.getType() == Volume.Type.ISO) {
                if (volPath == null) {
          /* Add iso as placeholder */
                    disk.defIsoDisk(null);
                } else {
                    disk.defIsoDisk(volPath);
                }
            } else {
                final int devId = volume.getDiskSeq().intValue();

                if (pool.getType() == StoragePoolType.RBD) {
          /*
           * For RBD pools we use the secret mechanism in libvirt. We store the secret under the UUID of the pool,
           * that's why we pass the pool's UUID as the authSecret
           */
                    disk.defNetworkBasedDisk(physicalDisk.getPath().replace("rbd:", ""), pool.getSourceHost(),
                            pool.getSourcePort(), pool.getAuthUserName(),
                            pool.getUuid(), devId, diskBusType, DiskProtocol.RBD, DiskDef.DiskFmtType.RAW);
                } else if (pool.getType() == StoragePoolType.Gluster) {
                    final String mountpoint = pool.getLocalPath();
                    final String path = physicalDisk.getPath();
                    final String glusterVolume = pool.getSourceDir().replace("/", "");
                    disk.defNetworkBasedDisk(glusterVolume + path.replace(mountpoint, ""), pool.getSourceHost(),
                            pool.getSourcePort(), null,
                            null, devId, diskBusType, DiskProtocol.GLUSTER, DiskDef.DiskFmtType.QCOW2);
                } else if (pool.getType() == StoragePoolType.CLVM || physicalDisk.getFormat() == PhysicalDiskFormat.RAW) {
                    disk.defBlockBasedDisk(physicalDisk.getPath(), devId, diskBusType);
                } else {
                    if (volume.getType() == Volume.Type.DATADISK) {
                        disk.defFileBasedDisk(physicalDisk.getPath(), devId, DiskDef.DiskBus.VIRTIO, DiskDef.DiskFmtType.QCOW2);
                    } else {
                        disk.defFileBasedDisk(physicalDisk.getPath(), devId, diskBusType, DiskDef.DiskFmtType.QCOW2);
                    }
                }
            }

            if (data instanceof VolumeObjectTO) {
                final VolumeObjectTO volumeObjectTo = (VolumeObjectTO) data;
                disk.setSerial(diskUuidToSerial(volumeObjectTo.getUuid()));
                if (volumeObjectTo.getBytesReadRate() != null && volumeObjectTo.getBytesReadRate() > 0) {
                    disk.setBytesReadRate(volumeObjectTo.getBytesReadRate());
                }
                if (volumeObjectTo.getBytesWriteRate() != null && volumeObjectTo.getBytesWriteRate() > 0) {
                    disk.setBytesWriteRate(volumeObjectTo.getBytesWriteRate());
                }
                if (volumeObjectTo.getIopsReadRate() != null && volumeObjectTo.getIopsReadRate() > 0) {
                    disk.setIopsReadRate(volumeObjectTo.getIopsReadRate());
                }
                if (volumeObjectTo.getIopsWriteRate() != null && volumeObjectTo.getIopsWriteRate() > 0) {
                    disk.setIopsWriteRate(volumeObjectTo.getIopsWriteRate());
                }
                if (volumeObjectTo.getCacheMode() != null) {
                    disk.setCacheMode(DiskDef.DiskCacheMode.valueOf(volumeObjectTo.getCacheMode().toString().toUpperCase()));
                }
            }
            vm.getDevices().addDevice(disk);
        }

        if (vmSpec.getType() != VirtualMachine.Type.User) {
            if (sysvmIsoPath != null) {
                final DiskDef iso = new DiskDef();
                iso.defIsoDisk(sysvmIsoPath);
                vm.getDevices().addDevice(iso);
            }
        }
    }

    @Override
    public Type getType() {
        return Type.Routing;
    }

    private DiskDef.DiskBus getGuestDiskModel(final String platformEmulator) {
        if (isGuestPvEnabled(platformEmulator)) {
            return DiskDef.DiskBus.VIRTIO;
        } else {
            return DiskDef.DiskBus.IDE;
        }
    }

    private Map<String, String> getVersionStrings() {
        final Script command = new Script(versionstringpath, timeout, LOGGER);
        final KeyValueInterpreter kvi = new KeyValueInterpreter();
        final String result = command.execute(kvi);
        if (result == null) {
            return kvi.getKeyValues();
        } else {
            return new HashMap<>(1);
        }
    }

    public String diskUuidToSerial(final String uuid) {
        final String uuidWithoutHyphen = uuid.replace("-", "");
        return uuidWithoutHyphen.substring(0, Math.min(uuidWithoutHyphen.length(), 20));
    }

    @Override
    public StartupCommand[] initialize() {

        final List<Object> info = getHostInfo();

        final StartupRoutingCommand cmd = new StartupRoutingCommand((Integer) info.get(0), (Long) info.get(1),
                (Long) info.get(2), (Long) info.get(4), (String) info.get(3), hypervisorType,
                RouterPrivateIpStrategy.HostLocal);
        cmd.setCpuSockets((Integer) info.get(5));
        fillNetworkInformation(cmd);
        privateIp = cmd.getPrivateIpAddress();
        cmd.getHostDetails().putAll(getVersionStrings());
        cmd.setPool(pool);
        cmd.setCluster(clusterId);
        cmd.setGatewayIpAddress(localGateway);
        cmd.setIqn(getIqn());

        StartupStorageCommand sscmd = null;
        try {

            final KvmStoragePool localStoragePool = storagePoolMgr.createStoragePool(localStorageUuid, "localhost", -1,
                    localStoragePath, "", StoragePoolType.Filesystem);
            final com.cloud.agent.api.StoragePoolInfo pi = new com.cloud.agent.api.StoragePoolInfo(localStoragePool.getUuid(),
                    cmd.getPrivateIpAddress(), localStoragePath, localStoragePath,
                    StoragePoolType.Filesystem, localStoragePool.getCapacity(), localStoragePool.getAvailable());

            sscmd = new StartupStorageCommand();
            sscmd.setPoolInfo(pi);
            sscmd.setGuid(pi.getUuid());
            sscmd.setDataCenter(dcId);
            sscmd.setResourceType(Storage.StorageResourceType.STORAGE_POOL);
        } catch (final CloudRuntimeException e) {
            LOGGER.debug("Unable to initialize local storage pool: " + e);
        }

        if (sscmd != null) {
            return new StartupCommand[]{cmd, sscmd};
        } else {
            return new StartupCommand[]{cmd};
        }
    }

    protected KvmStoragePoolManager getPoolManager() {
        return storagePoolMgr;
    }

    public synchronized String attachOrDetachIso(final Connect conn, final String vmName, String isoPath,
                                                 final boolean isAttach) throws LibvirtException, URISyntaxException,
            InternalErrorException {
        String isoXml = null;
        if (isoPath != null && isAttach) {
            final int index = isoPath.lastIndexOf("/");
            final String path = isoPath.substring(0, index);
            final String name = isoPath.substring(index + 1);
            final KvmStoragePool secondaryPool = storagePoolMgr.getStoragePoolByUri(path);
            final KvmPhysicalDisk isoVol = secondaryPool.getPhysicalDisk(name);
            isoPath = isoVol.getPath();

            final DiskDef iso = new DiskDef();
            iso.defIsoDisk(isoPath);
            isoXml = iso.toString();
        } else {
            final DiskDef iso = new DiskDef();
            iso.defIsoDisk(null);
            isoXml = iso.toString();
        }

        final List<DiskDef> disks = getDisks(conn, vmName);
        final String result = attachOrDetachDevice(conn, true, vmName, isoXml);
        if (result == null && !isAttach) {
            for (final DiskDef disk : disks) {
                if (disk.getDeviceType() == DiskDef.DeviceType.CDROM) {
                    cleanupDisk(disk);
                }
            }
        }
        return result;
    }

    private String getIqn() {
        try {
            final String textToFind = "InitiatorName=";

            final Script iScsiAdmCmd = new Script(true, "grep", 0, LOGGER);

            iScsiAdmCmd.add(textToFind);
            iScsiAdmCmd.add("/etc/iscsi/initiatorname.iscsi");

            final OutputInterpreter.OneLineParser parser = new OutputInterpreter.OneLineParser();

            final String result = iScsiAdmCmd.execute(parser);

            if (result != null) {
                return null;
            }

            final String textFound = parser.getLine().trim();

            return textFound.substring(textToFind.length());
        } catch (final Exception ex) {
            return null;
        }
    }

    public List<DiskDef> getDisks(final Connect conn, final String vmName) {
        final LibvirtDomainXmlParser parser = new LibvirtDomainXmlParser();
        Domain dm = null;
        try {
            dm = conn.domainLookupByName(vmName);
            parser.parseDomainXml(dm.getXMLDesc(0));
            return parser.getDisks();
        } catch (final LibvirtException e) {
            LOGGER.debug("Failed to get dom xml: " + e.toString());
            return new ArrayList<>();
        } finally {
            try {
                if (dm != null) {
                    dm.free();
                }
            } catch (final LibvirtException e) {
                LOGGER.trace("Ignoring libvirt error.", e);
            }
        }
    }

    protected synchronized String attachOrDetachDevice(final Connect conn, final boolean attach, final String vmName,
                                                       final String xml) throws LibvirtException, InternalErrorException {
        Domain dm = null;
        try {
            dm = conn.domainLookupByName(vmName);
            if (attach) {
                LOGGER.debug("Attaching device: " + xml);
                dm.attachDevice(xml);
            } else {
                LOGGER.debug("Detaching device: " + xml);
                dm.detachDevice(xml);
            }
        } catch (final LibvirtException e) {
            if (attach) {
                LOGGER.warn("Failed to attach device to " + vmName + ": " + e.getMessage());
            } else {
                LOGGER.warn("Failed to detach device from " + vmName + ": " + e.getMessage());
            }
            throw e;
        } finally {
            if (dm != null) {
                try {
                    dm.free();
                } catch (final LibvirtException l) {
                    LOGGER.trace("Ignoring libvirt error.", l);
                }
            }
        }

        return null;
    }

    private HashMap<String, HostVmStateReportEntry> getHostVmStateReport() {
        final HashMap<String, HostVmStateReportEntry> vmStates = new HashMap<>();
        Connect conn = null;

        if (hypervisorType == HypervisorType.KVM) {
            try {
                conn = LibvirtConnection.getConnectionByType(HypervisorType.KVM.toString());
                vmStates.putAll(getHostVmStateReport(conn));
            } catch (final LibvirtException e) {
                LOGGER.debug("Failed to get connection: " + e.getMessage());
            }
        }

        return vmStates;
    }

    public boolean cleanupDisk(final DiskDef disk) {
        final String path = disk.getDiskPath();

        if (path == null) {
            LOGGER.debug("Unable to clean up disk with null path (perhaps empty cdrom drive):" + disk);
            return false;
        }

        if (path.endsWith("systemvm.iso")) {
            // don't need to clean up system vm ISO as it's stored in local
            return true;
        }

        return storagePoolMgr.disconnectPhysicalDiskByPath(path);
    }

    private HashMap<String, HostVmStateReportEntry> getHostVmStateReport(final Connect conn) {
        final HashMap<String, HostVmStateReportEntry> vmStates = new HashMap<>();

        String[] vms = null;
        int[] ids = null;

        try {
            ids = conn.listDomains();
        } catch (final LibvirtException e) {
            LOGGER.warn("Unable to listDomains", e);
            return null;
        }
        try {
            vms = conn.listDefinedDomains();
        } catch (final LibvirtException e) {
            LOGGER.warn("Unable to listDomains", e);
            return null;
        }

        Domain dm = null;
        for (final int id : ids) {
            try {
                dm = conn.domainLookupByID(id);

                final DomainState ps = dm.getInfo().state;

                final PowerState state = convertToPowerState(ps);

                LOGGER.trace("VM " + dm.getName() + ": powerstate = " + ps + "; vm state=" + state.toString());
                final String vmName = dm.getName();

                // TODO : for XS/KVM (host-based resource), we require to remove
                // VM completely from host, for some reason, KVM seems to still keep
                // Stopped VM around, to work-around that, reporting only powered-on VM
                //
                if (state == PowerState.PowerOn) {
                    vmStates.put(vmName, new HostVmStateReportEntry(state, conn.getHostName()));
                }
            } catch (final LibvirtException e) {
                LOGGER.warn("Unable to get vms", e);
            } finally {
                try {
                    if (dm != null) {
                        dm.free();
                    }
                } catch (final LibvirtException e) {
                    LOGGER.trace("Ignoring libvirt error.", e);
                }
            }
        }

        for (final String vm : vms) {
            try {

                dm = conn.domainLookupByName(vm);

                final DomainState ps = dm.getInfo().state;
                final PowerState state = convertToPowerState(ps);
                final String vmName = dm.getName();
                LOGGER.trace("VM " + vmName + ": powerstate = " + ps + "; vm state=" + state.toString());

                // TODO : for XS/KVM (host-based resource), we require to remove
                // VM completely from host, for some reason, KVM seems to still keep
                // Stopped VM around, to work-around that, reporting only powered-on VM
                //
                if (state == PowerState.PowerOn) {
                    vmStates.put(vmName, new HostVmStateReportEntry(state, conn.getHostName()));
                }
            } catch (final LibvirtException e) {
                LOGGER.warn("Unable to get vms", e);
            } finally {
                try {
                    if (dm != null) {
                        dm.free();
                    }
                } catch (final LibvirtException e) {
                    LOGGER.trace("Ignoring libvirt error.", e);
                }
            }
        }

        return vmStates;
    }

    public synchronized String attachOrDetachDisk(final Connect conn,
                                                  final boolean attach, final String vmName, final KvmPhysicalDisk attachingDisk,
                                                  final int devId, final Long bytesReadRate, final Long bytesWriteRate, final Long iopsReadRate,
                                                  final Long iopsWriteRate, final String cacheMode) throws LibvirtException, InternalErrorException {
        List<DiskDef> disks = null;
        Domain dm = null;
        DiskDef diskdef = null;
        final KvmStoragePool attachingPool = attachingDisk.getPool();
        try {
            if (!attach) {
                dm = conn.domainLookupByName(vmName);
                final LibvirtDomainXmlParser parser = new LibvirtDomainXmlParser();
                final String xml = dm.getXMLDesc(0);
                parser.parseDomainXml(xml);
                disks = parser.getDisks();

                for (final DiskDef disk : disks) {
                    final String file = disk.getDiskPath();
                    if (file != null && file.equalsIgnoreCase(attachingDisk.getPath())) {
                        diskdef = disk;
                        break;
                    }
                }
                if (diskdef == null) {
                    throw new InternalErrorException("disk: " + attachingDisk.getPath() + " is not attached before");
                }
            } else {
                diskdef = new DiskDef();
                if (attachingPool.getType() == StoragePoolType.RBD) {
                    diskdef.defNetworkBasedDisk(attachingDisk.getPath(), attachingPool.getSourceHost(),
                            attachingPool.getSourcePort(), attachingPool.getAuthUserName(),
                            attachingPool.getUuid(), devId, DiskDef.DiskBus.VIRTIO, DiskProtocol.RBD, DiskDef.DiskFmtType.RAW);
                } else if (attachingPool.getType() == StoragePoolType.Gluster) {
                    diskdef.defNetworkBasedDisk(attachingDisk.getPath(), attachingPool.getSourceHost(),
                            attachingPool.getSourcePort(), null,
                            null, devId, DiskDef.DiskBus.VIRTIO, DiskProtocol.GLUSTER, DiskDef.DiskFmtType.QCOW2);
                } else if (attachingDisk.getFormat() == PhysicalDiskFormat.QCOW2) {
                    diskdef.defFileBasedDisk(attachingDisk.getPath(), devId, DiskDef.DiskBus.VIRTIO, DiskDef.DiskFmtType.QCOW2);
                } else if (attachingDisk.getFormat() == PhysicalDiskFormat.RAW) {
                    diskdef.defBlockBasedDisk(attachingDisk.getPath(), devId, DiskDef.DiskBus.VIRTIO);
                }
                if (bytesReadRate != null && bytesReadRate > 0) {
                    diskdef.setBytesReadRate(bytesReadRate);
                }
                if (bytesWriteRate != null && bytesWriteRate > 0) {
                    diskdef.setBytesWriteRate(bytesWriteRate);
                }
                if (iopsReadRate != null && iopsReadRate > 0) {
                    diskdef.setIopsReadRate(iopsReadRate);
                }
                if (iopsWriteRate != null && iopsWriteRate > 0) {
                    diskdef.setIopsWriteRate(iopsWriteRate);
                }

                if (cacheMode != null) {
                    diskdef.setCacheMode(DiskDef.DiskCacheMode.valueOf(cacheMode.toUpperCase()));
                }
            }

            final String xml = diskdef.toString();
            return attachOrDetachDevice(conn, attach, vmName, xml);
        } finally {
            if (dm != null) {
                dm.free();
            }
        }
    }

    protected List<Object> getHostInfo() {
        final ArrayList<Object> info = new ArrayList<>();
        long speed = 0;
        long cpus = 0;
        long ram = 0;
        int cpuSockets = 0;
        String cap = null;
        try {
            final Connect conn = LibvirtConnection.getConnection();
            final NodeInfo hosts = conn.nodeInfo();
            speed = getCpuSpeed(hosts);

      /*
       * Some CPUs report a single socket and multiple NUMA cells.
       * We need to multiply them to get the correct socket count.
       */
            cpuSockets = hosts.sockets;
            if (hosts.nodes > 0) {
                cpuSockets = hosts.sockets * hosts.nodes;
            }
            cpus = hosts.cpus;
            ram = hosts.memory * 1024L;
            final LibvirtCapXmlParser parser = new LibvirtCapXmlParser();
            parser.parseCapabilitiesXml(conn.getCapabilities());
            final ArrayList<String> oss = parser.getGuestOsType();
            for (final String s : oss) {
        /*
         * Even host supports guest os type more than hvm, we only report hvm to management server
         */
                if (s.equalsIgnoreCase("hvm")) {
                    cap = "hvm";
                }
            }
        } catch (final LibvirtException e) {
            LOGGER.trace("Ignoring libvirt error.", e);
        }

        if (isSnapshotSupported()) {
            cap = cap + ",snapshot";
        }

        info.add((int) cpus);
        info.add(speed);
        // Report system's RAM as actual RAM minus host OS reserved RAM
        ram = ram - dom0MinMem;
        info.add(ram);
        info.add(cap);
        info.add(dom0MinMem);
        info.add(cpuSockets);
        LOGGER.debug("cpus=" + cpus + ", speed=" + speed + ", ram=" + ram + ", _dom0MinMem=" + dom0MinMem
                + ", cpu sockets=" + cpuSockets);

        return info;
    }

    protected List<String> getAllVmNames(final Connect conn) {
        final ArrayList<String> la = new ArrayList<>();
        try {
            final String[] names = conn.listDefinedDomains();
            for (final String name : names) {
                la.add(name);
            }
        } catch (final LibvirtException e) {
            LOGGER.warn("Failed to list Defined domains", e);
        }

        int[] ids = null;
        try {
            ids = conn.listDomains();
        } catch (final LibvirtException e) {
            LOGGER.warn("Failed to list domains", e);
            return la;
        }

        Domain dm = null;
        for (final int id : ids) {
            try {
                dm = conn.domainLookupByID(id);
                la.add(dm.getName());
            } catch (final LibvirtException e) {
                LOGGER.warn("Unable to get vms", e);
            } finally {
                try {
                    if (dm != null) {
                        dm.free();
                    }
                } catch (final LibvirtException e) {
                    LOGGER.trace("Ignoring libvirt error.", e);
                }
            }
        }

        return la;
    }

    protected static long getCpuSpeed(final NodeInfo nodeInfo) {
        try (final Reader reader = new FileReader(
                "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq")) {
            return Long.parseLong(IOUtils.toString(reader).trim()) / 1000;
        } catch (IOException | NumberFormatException e) {
            LOGGER.warn("Could not read cpuinfo_max_freq");
            return nodeInfo.mhz;
        }
    }

    public String rebootVm(final Connect conn, final String vmName) {
        Domain dm = null;
        String msg = null;
        try {
            dm = conn.domainLookupByName(vmName);
            // Get XML Dump including the secure information such as VNC password
            // By passing 1, or VIR_DOMAIN_XML_SECURE flag
            // https://libvirt.org/html/libvirt-libvirt-domain.html#virDomainXMLFlags
            String vmDef = dm.getXMLDesc(1);
            final LibvirtDomainXmlParser parser = new LibvirtDomainXmlParser();
            parser.parseDomainXml(vmDef);
            for (final InterfaceDef nic : parser.getInterfaces()) {
                if (nic.getNetType() == GuestNetType.BRIDGE && nic.getBrName().startsWith("cloudVirBr")) {
                    try {
                        final int vnetId = Integer.parseInt(nic.getBrName().replaceFirst("cloudVirBr", ""));
                        final String pifName = getPif(guestBridgeName);
                        final String newBrName = "br" + pifName + "-" + vnetId;
                        vmDef = vmDef.replaceAll("'" + nic.getBrName() + "'", "'" + newBrName + "'");
                        LOGGER.debug("VM bridge name is changed from " + nic.getBrName() + " to " + newBrName);
                    } catch (final NumberFormatException e) {
                        continue;
                    }
                }
            }
            LOGGER.debug(vmDef);
            msg = stopVm(conn, vmName);
            msg = startVm(conn, vmName, vmDef);
            return null;
        } catch (final LibvirtException e) {
            LOGGER.warn("Failed to create vm", e);
            msg = e.getMessage();
        } catch (final InternalErrorException e) {
            LOGGER.warn("Failed to create vm", e);
            msg = e.getMessage();
        } finally {
            try {
                if (dm != null) {
                    dm.free();
                }
            } catch (final LibvirtException e) {
                LOGGER.trace("Ignoring libvirt error.", e);
            }
        }

        return msg;
    }

    public String stopVm(final Connect conn, final String vmName) {
        DomainState state = null;
        Domain dm = null;

        LOGGER.debug("Try to stop the vm at first");
        String ret = stopVm(conn, vmName, false);
        if (ret == Script.ERR_TIMEOUT) {
            ret = stopVm(conn, vmName, true);
        } else if (ret != null) {
      /*
       * There is a race condition between libvirt and qemu: libvirt listens on qemu's monitor fd. If qemu is shutdown,
       * while libvirt is reading on the fd, then libvirt will report an error.
       */
      /* Retry 3 times, to make sure we can get the vm's status */
            for (int i = 0; i < 3; i++) {
                try {
                    dm = conn.domainLookupByName(vmName);
                    state = dm.getInfo().state;
                    break;
                } catch (final LibvirtException e) {
                    LOGGER.debug("Failed to get vm status:" + e.getMessage());
                } finally {
                    try {
                        if (dm != null) {
                            dm.free();
                        }
                    } catch (final LibvirtException l) {
                        LOGGER.trace("Ignoring libvirt error.", l);
                    }
                }
            }

            if (state == null) {
                LOGGER.debug("Can't get vm's status, assume it's dead already");
                return null;
            }

            if (state != DomainState.VIR_DOMAIN_SHUTOFF) {
                LOGGER.debug("Try to destroy the vm");
                ret = stopVm(conn, vmName, true);
                if (ret != null) {
                    return ret;
                }
            }
        }

        return null;
    }

    public String startVm(final Connect conn, final String vmName, final String domainXml)
            throws LibvirtException, InternalErrorException {
        try {
      /*
       * We create a transient domain here. When this method gets called we receive a full XML specification of the
       * guest, so no need to define it persistent.
       *
       * This also makes sure we never have any old "garbage" defined in libvirt which might haunt us.
       */

            // check for existing inactive vm definition and remove it
            // this can sometimes happen during crashes, etc
            Domain dm = null;
            try {
                dm = conn.domainLookupByName(vmName);
                if (dm != null && dm.isPersistent() == 1) {
                    // this is safe because it doesn't stop running VMs
                    dm.undefine();
                }
            } catch (final LibvirtException e) {
                // this is what we want, no domain found
            } finally {
                if (dm != null) {
                    dm.free();
                }
            }

            conn.domainCreateXML(domainXml, 0);
        } catch (final LibvirtException e) {
            throw e;
        }
        return null;
    }

    protected String stopVm(final Connect conn, final String vmName, final boolean force) {
        Domain dm = null;
        try {
            dm = conn.domainLookupByName(vmName);
            final int persist = dm.isPersistent();
            if (force) {
                if (dm.isActive() == 1) {
                    dm.destroy();
                    if (persist == 1) {
                        dm.undefine();
                    }
                }
            } else {
                if (dm.isActive() == 0) {
                    return null;
                }
                dm.shutdown();
                int retry = stopTimeout / 2000;
        /*
         * Wait for the domain gets into shutoff state. When it does the dm object will no longer work, so we need to
         * catch it.
         */
                try {
                    while (dm.isActive() == 1 && retry >= 0) {
                        Thread.sleep(2000);
                        retry--;
                    }
                } catch (final LibvirtException e) {
                    final String error = e.toString();
                    if (error.contains("Domain not found")) {
                        LOGGER.debug("successfully shut down vm " + vmName);
                    } else {
                        LOGGER.debug("Error in waiting for vm shutdown:" + error);
                    }
                }
                if (retry < 0) {
                    LOGGER.warn("Timed out waiting for domain " + vmName + " to shutdown gracefully");
                    return Script.ERR_TIMEOUT;
                } else {
                    if (persist == 1) {
                        dm.undefine();
                    }
                }
            }
        } catch (final LibvirtException e) {
            if (e.getMessage().contains("Domain not found")) {
                LOGGER.debug("VM " + vmName + " doesn't exist, no need to stop it");
                return null;
            }
            LOGGER.debug("Failed to stop VM :" + vmName + " :", e);
            return e.getMessage();
        } catch (final InterruptedException ie) {
            LOGGER.debug("Interrupted sleep");
            return ie.getMessage();
        } finally {
            try {
                if (dm != null) {
                    dm.free();
                }
            } catch (final LibvirtException e) {
                LOGGER.trace("Ignoring libvirt error.", e);
            }
        }

        return null;
    }

    public Integer getVncPort(final Connect conn, final String vmName) throws LibvirtException {
        final LibvirtDomainXmlParser parser = new LibvirtDomainXmlParser();
        Domain dm = null;
        try {
            dm = conn.domainLookupByName(vmName);
            final String xmlDesc = dm.getXMLDesc(0);
            parser.parseDomainXml(xmlDesc);
            return parser.getVncPort();
        } finally {
            try {
                if (dm != null) {
                    dm.free();
                }
            } catch (final LibvirtException l) {
                LOGGER.trace("Ignoring libvirt error.", l);
            }
        }
    }

    public boolean isCentosHost() {
        if (hvVersion <= 9) {
            return true;
        } else {
            return false;
        }
    }

    public List<VmDiskStatsEntry> getVmDiskStat(final Connect conn, final String vmName) throws LibvirtException {
        Domain dm = null;
        try {
            dm = getDomain(conn, vmName);

            final List<VmDiskStatsEntry> stats = new ArrayList<>();

            final List<DiskDef> disks = getDisks(conn, vmName);

            for (final DiskDef disk : disks) {
                if (disk.getDeviceType() != DeviceType.DISK) {
                    break;
                }
                final DomainBlockStats blockStats = dm.blockStats(disk.getDiskLabel());
                final String path = disk.getDiskPath(); // for example, path = /mnt/pool_uuid/disk_path/
                String diskPath = null;
                if (path != null) {
                    final String[] token = path.split("/");
                    if (token.length > 3) {
                        diskPath = token[3];
                        final VmDiskStatsEntry stat = new VmDiskStatsEntry(vmName, diskPath, blockStats.wr_req, blockStats.rd_req,
                                blockStats.wr_bytes, blockStats.rd_bytes);
                        stats.add(stat);
                    }
                }
            }

            return stats;
        } finally {
            if (dm != null) {
                dm.free();
            }
        }
    }

    public VmStatsEntry getVmStat(final Connect conn, final String vmName) throws LibvirtException {
        Domain dm = null;
        try {
            dm = getDomain(conn, vmName);
            final DomainInfo info = dm.getInfo();

            final VmStatsEntry stats = new VmStatsEntry();
            stats.setNumCPUs(info.nrVirtCpu);
            stats.setEntityType("vm");

      /* get cpu utilization */
            VmStats oldStats = null;

            final Calendar now = Calendar.getInstance();

            oldStats = vmStats.get(vmName);

            long elapsedTime = 0;
            if (oldStats != null) {
                elapsedTime = now.getTimeInMillis() - oldStats.timestamp.getTimeInMillis();
                double utilization = (info.cpuTime - oldStats.usedTime) / ((double) elapsedTime * 1000000);

                final NodeInfo node = conn.nodeInfo();
                utilization = utilization / node.cpus;
                if (utilization > 0) {
                    stats.setCPUUtilization(utilization * 100);
                }
            }

      /* get network stats */

            final List<InterfaceDef> vifs = getInterfaces(conn, vmName);
            long rx = 0;
            long tx = 0;
            for (final InterfaceDef vif : vifs) {
                final DomainInterfaceStats ifStats = dm.interfaceStats(vif.getDevName());
                rx += ifStats.rx_bytes;
                tx += ifStats.tx_bytes;
            }

            if (oldStats != null) {
                final double deltarx = rx - oldStats.rx;
                if (deltarx > 0) {
                    stats.setNetworkReadKBs(deltarx / 1024);
                }
                final double deltatx = tx - oldStats.tx;
                if (deltatx > 0) {
                    stats.setNetworkWriteKBs(deltatx / 1024);
                }
            }

      /* get disk stats */
            final List<DiskDef> disks = getDisks(conn, vmName);
            long ioRd = 0;
            long ioWr = 0;
            long bytesRd = 0;
            long bytesWr = 0;
            for (final DiskDef disk : disks) {
                final DomainBlockStats blockStats = dm.blockStats(disk.getDiskLabel());
                ioRd += blockStats.rd_req;
                ioWr += blockStats.wr_req;
                bytesRd += blockStats.rd_bytes;
                bytesWr += blockStats.wr_bytes;
            }

            if (oldStats != null) {
                final long deltaiord = ioRd - oldStats.ioRead;
                if (deltaiord > 0) {
                    stats.setDiskReadIOs(deltaiord);
                }
                final long deltaiowr = ioWr - oldStats.ioWrote;
                if (deltaiowr > 0) {
                    stats.setDiskWriteIOs(deltaiowr);
                }
                final double deltabytesrd = bytesRd - oldStats.bytesRead;
                if (deltabytesrd > 0) {
                    stats.setDiskReadKBs(deltabytesrd / 1024);
                }
                final double deltabyteswr = bytesWr - oldStats.bytesWrote;
                if (deltabyteswr > 0) {
                    stats.setDiskWriteKBs(deltabyteswr / 1024);
                }
            }

      /* save to Hashmap */
            final VmStats newStat = new VmStats();
            newStat.usedTime = info.cpuTime;
            newStat.rx = rx;
            newStat.tx = tx;
            newStat.ioRead = ioRd;
            newStat.ioWrote = ioWr;
            newStat.bytesRead = bytesRd;
            newStat.bytesWrote = bytesWr;
            newStat.timestamp = now;
            vmStats.put(vmName, newStat);
            return stats;
        } finally {
            if (dm != null) {
                dm.free();
            }
        }
    }

    public boolean destroyNetworkRulesForVm(final Connect conn, final String vmName) {
        if (!canBridgeFirewall) {
            return false;
        }
        String vif = null;
        final List<InterfaceDef> intfs = getInterfaces(conn, vmName);
        if (intfs.size() > 0) {
            final InterfaceDef intf = intfs.get(0);
            vif = intf.getDevName();
        }
        final Script cmd = new Script(securityGroupPath, timeout, LOGGER);
        cmd.add("destroy_network_rules_for_vm");
        cmd.add("--vmname", vmName);
        if (vif != null) {
            cmd.add("--vif", vif);
        }
        final String result = cmd.execute();
        if (result != null) {
            return false;
        }
        return true;
    }

    public boolean defaultNetworkRules(final Connect conn, final String vmName, final NicTO nic, final Long vmId,
                                       final String secIpStr) {
        if (!canBridgeFirewall) {
            return false;
        }

        final List<InterfaceDef> intfs = getInterfaces(conn, vmName);
        if (intfs.size() == 0 || intfs.size() < nic.getDeviceId()) {
            return false;
        }

        final InterfaceDef intf = intfs.get(nic.getDeviceId());
        final String brname = intf.getBrName();
        final String vif = intf.getDevName();

        final Script cmd = new Script(securityGroupPath, timeout, LOGGER);
        cmd.add("default_network_rules");
        cmd.add("--vmname", vmName);
        cmd.add("--vmid", vmId.toString());
        if (nic.getIp() != null) {
            cmd.add("--vmip", nic.getIp());
        }
        cmd.add("--vmmac", nic.getMac());
        cmd.add("--vif", vif);
        cmd.add("--brname", brname);
        cmd.add("--nicsecips", secIpStr);
        final String result = cmd.execute();
        if (result != null) {
            return false;
        }
        return true;
    }

    protected boolean post_default_network_rules(final Connect conn, final String vmName, final NicTO nic,
                                                 final Long vmId, final InetAddress dhcpServerIp, final String hostIp, final String hostMacAddr) {
        if (!canBridgeFirewall) {
            return false;
        }

        final List<InterfaceDef> intfs = getInterfaces(conn, vmName);
        if (intfs.size() < nic.getDeviceId()) {
            return false;
        }

        final InterfaceDef intf = intfs.get(nic.getDeviceId());
        final String brname = intf.getBrName();
        final String vif = intf.getDevName();

        final Script cmd = new Script(securityGroupPath, timeout, LOGGER);
        cmd.add("post_default_network_rules");
        cmd.add("--vmname", vmName);
        cmd.add("--vmid", vmId.toString());
        cmd.add("--vmip", nic.getIp());
        cmd.add("--vmmac", nic.getMac());
        cmd.add("--vif", vif);
        cmd.add("--brname", brname);
        if (dhcpServerIp != null) {
            cmd.add("--dhcpSvr", dhcpServerIp.getHostAddress());
        }

        cmd.add("--hostIp", hostIp);
        cmd.add("--hostMacAddr", hostMacAddr);
        final String result = cmd.execute();
        if (result != null) {
            return false;
        }
        return true;
    }

    public boolean configureDefaultNetworkRulesForSystemVm(final Connect conn, final String vmName) {
        if (!canBridgeFirewall) {
            return false;
        }

        final Script cmd = new Script(securityGroupPath, timeout, LOGGER);
        cmd.add("default_network_rules_systemvm");
        cmd.add("--vmname", vmName);
        cmd.add("--localbrname", linkLocalBridgeName);
        final String result = cmd.execute();
        if (result != null) {
            return false;
        }
        return true;
    }

    public boolean addNetworkRules(final String vmName, final String vmId, final String guestIp, final String sig,
                                   final String seq, final String mac, final String rules, final String vif, final String brname,
                                   final String secIps) {
        if (!canBridgeFirewall) {
            return false;
        }

        final String newRules = rules.replace(" ", ";");
        final Script cmd = new Script(securityGroupPath, timeout, LOGGER);
        cmd.add("add_network_rules");
        cmd.add("--vmname", vmName);
        cmd.add("--vmid", vmId);
        cmd.add("--vmip", guestIp);
        cmd.add("--sig", sig);
        cmd.add("--seq", seq);
        cmd.add("--vmmac", mac);
        cmd.add("--vif", vif);
        cmd.add("--brname", brname);
        cmd.add("--nicsecips", secIps);
        if (newRules != null && !newRules.isEmpty()) {
            cmd.add("--rules", newRules);
        }
        final String result = cmd.execute();
        if (result != null) {
            return false;
        }
        return true;
    }

    public boolean configureNetworkRulesVmSecondaryIp(final Connect conn, final String vmName, final String secIp,
                                                      final String action) {

        if (!canBridgeFirewall) {
            return false;
        }

        final Script cmd = new Script(securityGroupPath, timeout, LOGGER);
        cmd.add("network_rules_vmSecondaryIp");
        cmd.add("--vmname", vmName);
        cmd.add("--nicsecips", secIp);
        cmd.add("--action", action);

        final String result = cmd.execute();
        if (result != null) {
            return false;
        }
        return true;
    }

    public boolean cleanupRules() {
        if (!canBridgeFirewall) {
            return false;
        }
        final Script cmd = new Script(securityGroupPath, timeout, LOGGER);
        cmd.add("cleanup_rules");
        final String result = cmd.execute();
        if (result != null) {
            return false;
        }
        return true;
    }

    private String executeBashScript(final String script) {
        final Script command = new Script("/bin/bash", timeout, LOGGER);
        command.add("-c");
        command.add(script);
        return command.execute();
    }

    public Pair<Double, Double> getNicStats(final String nicName) {
        return new Pair<>(readDouble(nicName, "rx_bytes"), readDouble(nicName, "tx_bytes"));
    }

    static double readDouble(final String nicName, final String fileName) {
        final String path = "/sys/class/net/" + nicName + "/statistics/" + fileName;
        try {
            return Double.parseDouble(FileUtils.readFileToString(new File(path)));
        } catch (final IOException ioe) {
            LOGGER.warn("Failed to read the " + fileName + " for " + nicName + " from " + path, ioe);
            return 0.0;
        }
    }

    @Override
    public void setName(final String name) {
    }

    public HypervisorType getHypervisorType() {
        return hypervisorType;
    }

    public String mapRbdDevice(final KvmPhysicalDisk disk) {
        final KvmStoragePool pool = disk.getPool();
        // Check if rbd image is already mapped
        final String[] splitPoolImage = disk.getPath().split("/");
        String device = Script.runSimpleBashScript(
                "rbd showmapped | grep \"" + splitPoolImage[0] + "[ ]*" + splitPoolImage[1] + "\" | grep -o \"[^ ]*[ ]*$\"");
        if (device == null) {
            // If not mapped, map and return mapped device
            Script.runSimpleBashScript("rbd map " + disk.getPath() + " --id " + pool.getAuthUserName());
            device = Script.runSimpleBashScript(
                    "rbd showmapped | grep \"" + splitPoolImage[0] + "[ ]*" + splitPoolImage[1] + "\" | grep -o \"[^ ]*[ ]*$\"");
        }
        return device;
    }

    protected enum BridgeType {
        NATIVE, OPENVSWITCH
    }

    private static final class KeyValueInterpreter extends OutputInterpreter {
        private final Map<String, String> map = new HashMap<>();

        @Override
        public String interpret(final BufferedReader reader) throws IOException {
            String line = null;
            int numLines = 0;
            while ((line = reader.readLine()) != null) {
                final String[] toks = line.trim().split("=");
                if (toks.length < 2) {
                    LOGGER.warn("Failed to parse Script output: " + line);
                } else {
                    map.put(toks[0].trim(), toks[1].trim());
                }
                numLines++;
            }
            if (numLines == 0) {
                LOGGER.warn("KeyValueInterpreter: no output lines?");
            }
            return null;
        }

        public Map<String, String> getKeyValues() {
            return map;
        }
    }

    private class VmStats {
        long usedTime;
        long tx;
        long rx;
        long ioRead;
        long ioWrote;
        long bytesRead;
        long bytesWrote;
        Calendar timestamp;
    }

    public String getRuleLogsForVms() {
        final Script cmd = new Script(securityGroupPath, timeout, LOGGER);
        cmd.add("get_rule_logs_for_vms");
        final OutputInterpreter.OneLineParser parser = new OutputInterpreter.OneLineParser();
        final String result = cmd.execute(parser);
        if (result == null) {
            return parser.getLine();
        }
        return null;
    }

    private HashMap<String, Pair<Long, Long>> syncNetworkGroups(final long id) {
        final HashMap<String, Pair<Long, Long>> states = new HashMap<>();

        final String result = getRuleLogsForVms();
        LOGGER.trace("syncNetworkGroups: id=" + id + " got: " + result);
        final String[] rulelogs = result != null ? result.split(";") : new String[0];
        for (final String rulesforvm : rulelogs) {
            final String[] log = rulesforvm.split(",");
            if (log.length != 6) {
                continue;
            }
            try {
                states.put(log[0], new Pair<>(Long.parseLong(log[1]), Long.parseLong(log[5])));
            } catch (final NumberFormatException nfe) {
                states.put(log[0], new Pair<>(-1L, -1L));
            }
        }
        return states;
    }

    /* online snapshot supported by enhanced qemu-kvm */
    private boolean isSnapshotSupported() {
        final String result = executeBashScript("qemu-img --help|grep convert");
        if (result != null) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void setConfigParams(final Map<String, Object> params) {
    }

    @Override
    public Map<String, Object> getConfigParams() {
        return null;
    }

    @Override
    public int getRunLevel() {
        return 0;
    }

    @Override
    public void setRunLevel(final int level) {
    }
}
