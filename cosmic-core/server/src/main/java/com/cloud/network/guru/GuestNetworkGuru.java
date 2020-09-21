package com.cloud.network.guru;

import com.cloud.configuration.Config;
import com.cloud.context.CallContext;
import com.cloud.db.model.Zone;
import com.cloud.db.repository.ZoneRepository;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dc.dao.VlanDao;
import com.cloud.deploy.DeployDestination;
import com.cloud.deploy.DeploymentPlan;
import com.cloud.engine.orchestration.service.NetworkOrchestrationService;
import com.cloud.event.ActionEventUtils;
import com.cloud.event.EventTypes;
import com.cloud.event.EventVO;
import com.cloud.framework.config.ConfigKey;
import com.cloud.framework.config.Configurable;
import com.cloud.framework.config.dao.ConfigurationDao;
import com.cloud.legacymodel.dc.DataCenter;
import com.cloud.legacymodel.exceptions.CloudRuntimeException;
import com.cloud.legacymodel.exceptions.InsufficientAddressCapacityException;
import com.cloud.legacymodel.exceptions.InsufficientVirtualNetworkCapacityException;
import com.cloud.legacymodel.exceptions.InvalidParameterValueException;
import com.cloud.legacymodel.network.Network;
import com.cloud.legacymodel.network.Network.Provider;
import com.cloud.legacymodel.network.Network.Service;
import com.cloud.legacymodel.network.Network.State;
import com.cloud.legacymodel.network.Nic.ReservationStrategy;
import com.cloud.legacymodel.user.Account;
import com.cloud.legacymodel.utils.Pair;
import com.cloud.model.enumeration.BroadcastDomainType;
import com.cloud.model.enumeration.DHCPMode;
import com.cloud.model.enumeration.GuestType;
import com.cloud.model.enumeration.IpAddressFormat;
import com.cloud.model.enumeration.NetworkType;
import com.cloud.model.enumeration.TrafficType;
import com.cloud.model.enumeration.VirtualMachineType;
import com.cloud.network.IpAddressManager;
import com.cloud.network.NetworkModel;
import com.cloud.network.NetworkProfile;
import com.cloud.network.PhysicalNetwork;
import com.cloud.network.PhysicalNetwork.IsolationMethod;
import com.cloud.network.dao.IPAddressDao;
import com.cloud.network.dao.IPAddressVO;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.dao.NetworkVO;
import com.cloud.network.dao.PhysicalNetworkDao;
import com.cloud.network.dao.PhysicalNetworkVO;
import com.cloud.network.vpc.dao.VpcDao;
import com.cloud.offering.NetworkOffering;
import com.cloud.offerings.dao.NetworkOfferingDao;
import com.cloud.offerings.dao.NetworkOfferingServiceMapDao;
import com.cloud.server.ConfigurationServer;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.utils.component.AdapterBase;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.db.TransactionCallbackNoReturn;
import com.cloud.utils.db.TransactionStatus;
import com.cloud.utils.net.NetUtils;
import com.cloud.vm.NicProfile;
import com.cloud.vm.ReservationContext;
import com.cloud.vm.VirtualMachineProfile;
import com.cloud.vm.dao.NicDao;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GuestNetworkGuru extends AdapterBase implements NetworkGuru, Configurable {
    static final ConfigKey<Boolean> UseSystemGuestVlans =
            new ConfigKey<>(
                    "Advanced",
                    Boolean.class,
                    "use.system.guest.vlans",
                    "true",
                    "If true, when account has dedicated guest vlan range(s), once the vlans dedicated to the account have been consumed vlans will be allocated from the system " +
                            "pool",
                    false, ConfigKey.Scope.Account);
    private static final Logger s_logger = LoggerFactory.getLogger(GuestNetworkGuru.class);
    private static final TrafficType[] TrafficTypes = {TrafficType.Guest};
    @Inject
    protected VpcDao _vpcDao;
    @Inject
    protected NetworkOrchestrationService _networkMgr;
    @Inject
    protected NetworkModel _networkModel;
    @Inject
    protected DataCenterDao _dcDao;
    @Inject
    protected VlanDao _vlanDao;
    @Inject
    protected NicDao _nicDao;
    @Inject
    protected VMTemplateDao _templateDao;
    @Inject
    protected NetworkOfferingDao _networkOfferingDao;
    @Inject
    protected NetworkOfferingServiceMapDao _networkOfferingServiceMapDao;
    @Inject
    protected NetworkDao _networkDao;
    @Inject
    protected PhysicalNetworkDao _physicalNetworkDao;
    protected IsolationMethod[] _isolationMethods;
    @Inject
    ConfigurationDao _configDao;
    @Inject
    IPAddressDao _ipAddressDao;
    @Inject
    ConfigurationServer _configServer;
    @Inject
    IpAddressManager _ipAddrMgr;
    @Inject
    ZoneRepository zoneRepository;

    Random _rand = new Random(System.currentTimeMillis());
    String _defaultGateway;
    String _defaultCidr;

    protected GuestNetworkGuru() {
        super();
        _isolationMethods = null;
    }

    public boolean isMyIsolationMethod(final PhysicalNetwork physicalNetwork) {
        if (physicalNetwork == null) {
            // Can't tell if there is no physical network
            return false;
        }

        List<String> methods = new ArrayList<>();
        for (final String method : physicalNetwork.getIsolationMethods()) {
            methods.add(method.toLowerCase());
        }
        if (methods.isEmpty()) {
            // The empty isolation method is assumed to be VLAN
            s_logger.debug("Empty physical isolation type for physical network " + physicalNetwork.getUuid());
            methods = new ArrayList<>(1);
            methods.add("VLAN".toLowerCase());
        }

        for (final IsolationMethod m : _isolationMethods) {
            if (methods.contains(m.toString().toLowerCase())) {
                return true;
            }
        }

        logMismatchInRequiredAndSupportedIsolationMethods(methods);

        return false;
    }

    private void logMismatchInRequiredAndSupportedIsolationMethods(final List<String> methods) {
        final String requiredIsolationMethods = Arrays.toString(methods.toArray(new String[methods.size()]));
        final String supportedIsolationMethods = Arrays.toString(_isolationMethods);
        s_logger.debug("Isolation methods '" + requiredIsolationMethods + "' are not supported by this guru (supported methods are " + supportedIsolationMethods + ")");
    }

    public IsolationMethod[] getIsolationMethods() {
        return _isolationMethods;
    }

    @Override
    public Network design(final NetworkOffering offering, final DeploymentPlan plan, final Network userSpecified, final Account owner) {
        final DataCenter dc = _dcDao.findById(plan.getDataCenterId());
        final PhysicalNetworkVO physnet = _physicalNetworkDao.findById(plan.getPhysicalNetworkId());

        if (!canHandle(offering, dc.getNetworkType(), physnet)) {
            return null;
        }

        final NetworkVO network = new NetworkVO(
                offering.getTrafficType(),
                GuestType.Sync.equals(offering.getGuestType())
                        ? DHCPMode.Static
                        : DHCPMode.Dhcp,
                BroadcastDomainType.Vlan,
                offering.getId(),
                State.Allocated,
                plan.getDataCenterId(),
                plan.getPhysicalNetworkId(),
                offering.getRedundantRouter()
        );
        if (userSpecified != null) {
            if (!GuestType.Private.equals(offering.getGuestType()) &&
                    ((userSpecified.getCidr() == null && userSpecified.getGateway() != null) || (userSpecified.getCidr() != null && userSpecified.getGateway() == null))) {
                throw new InvalidParameterValueException("CIDR and gateway must be specified together or the CIDR must represents the gateway.");
            }

            if (userSpecified.getDns1() != null) {
                network.setDns1(userSpecified.getDns1());
            }

            if (userSpecified.getDns2() != null) {
                network.setDns2(userSpecified.getDns2());
            }

            if (userSpecified.getCidr() != null) {
                network.setCidr(userSpecified.getCidr());
                network.setGateway(userSpecified.getGateway());
            } else {
                final String guestNetworkCidr = dc.getGuestNetworkCidr();
                if (guestNetworkCidr != null) {
                    final String[] cidrTuple = guestNetworkCidr.split("\\/");
                    network.setGateway(NetUtils.getIpRangeStartIpFromCidr(cidrTuple[0], Long.parseLong(cidrTuple[1])));
                    network.setCidr(guestNetworkCidr);
                } else if (dc.getNetworkType() == NetworkType.Advanced) {
                    throw new CloudRuntimeException("Can't design network " + network + "; guest CIDR is not configured per zone " + dc);
                }
            }

            if (offering.getSpecifyVlan()) {
                network.setBroadcastUri(userSpecified.getBroadcastUri());
                network.setState(State.Setup);
            }
        } else if (!GuestType.Sync.equals(offering.getGuestType())) {
            final String guestNetworkCidr = dc.getGuestNetworkCidr();
            if (guestNetworkCidr == null && dc.getNetworkType() == NetworkType.Advanced) {
                throw new CloudRuntimeException("Can't design network " + network + "; guest CIDR is not configured per zone " + dc);
            }
            final String[] cidrTuple = guestNetworkCidr.split("\\/");
            network.setGateway(NetUtils.getIpRangeStartIpFromCidr(cidrTuple[0], Long.parseLong(cidrTuple[1])));
            network.setCidr(guestNetworkCidr);
        }

        return network;
    }

    protected abstract boolean canHandle(NetworkOffering offering, final NetworkType networkType, PhysicalNetwork physicalNetwork);

    @Override
    public Network implement(final Network network, final NetworkOffering offering, final DeployDestination dest, final ReservationContext context)
            throws InsufficientVirtualNetworkCapacityException {
        assert network.getState() == State.Implementing : "Why are we implementing " + network;

        final long dcId = dest.getZone().getId();

        //get physical network id
        Long physicalNetworkId = network.getPhysicalNetworkId();

        // physical network id can be null in Guest Network in Basic zone, so locate the physical network
        if (physicalNetworkId == null) {
            physicalNetworkId = _networkModel.findPhysicalNetworkId(dcId, offering.getTags(), offering.getTrafficType());
        }

        final NetworkVO implemented =
                new NetworkVO(network.getTrafficType(), network.getMode(), network.getBroadcastDomainType(), network.getNetworkOfferingId(), State.Allocated,
                        network.getDataCenterId(), physicalNetworkId, offering.getRedundantRouter());

        allocateVnet(network, implemented, dcId, physicalNetworkId, context.getReservationId());

        if (network.getGateway() != null) {
            implemented.setGateway(network.getGateway());
        }

        if (network.getCidr() != null) {
            implemented.setCidr(network.getCidr());
        }
        return implemented;
    }

    protected void allocateVnet(final Network network, final NetworkVO implemented, final long dcId, final long physicalNetworkId, final String reservationId)
            throws InsufficientVirtualNetworkCapacityException {
        if (network.getBroadcastUri() == null) {
            final String vnet = _dcDao.allocateVnet(dcId, physicalNetworkId, network.getAccountId(), reservationId, UseSystemGuestVlans.valueIn(network.getAccountId()));
            if (vnet == null) {
                throw new InsufficientVirtualNetworkCapacityException("Unable to allocate vnet as a " + "part of network " + network + " implement ", DataCenter.class,
                        dcId);
            }
            implemented.setBroadcastUri(BroadcastDomainType.Vlan.toUri(vnet));
            ActionEventUtils.onCompletedActionEvent(CallContext.current().getCallingUserId(), network.getAccountId(), EventVO.LEVEL_INFO,
                    EventTypes.EVENT_ZONE_VLAN_ASSIGN, "Assigned Zone Vlan: " + vnet + " Network Id: " + network.getId(), 0);
        } else {
            implemented.setBroadcastUri(network.getBroadcastUri());
        }
    }

    @Override
    public NicProfile allocate(final Network network, NicProfile nic, final VirtualMachineProfile vm) throws InsufficientVirtualNetworkCapacityException,
            InsufficientAddressCapacityException {

        assert network.getTrafficType() == TrafficType.Guest : "Look at my name!  Why are you calling" + " me when the traffic type is : " + network.getTrafficType();

        if (nic == null) {
            nic = new NicProfile(ReservationStrategy.Start, null, null, null, null);
        }

        final Zone zone = zoneRepository.findById(network.getDataCenterId()).orElse(null);

        if (nic.getIPv4Address() == null && !GuestType.Sync.equals(network.getGuestType())) {
            nic.setBroadcastUri(network.getBroadcastUri());
            nic.setIsolationUri(network.getBroadcastUri());
            nic.setIPv4Gateway(network.getGateway());

            String guestIp;
            if (network.getSpecifyIpRanges()) {
                _ipAddrMgr.allocateDirectIp(nic, zone, vm, network, nic.getRequestedIPv4(), null);
            } else {
                final VirtualMachineType vmtype = vm.getVirtualMachine().getType();

                switch (vmtype) {
                    case User:
                        guestIp = assignGuestOrGatewayIp(network, nic, vm, zone);

                        break;
                    case DomainRouter:
                        if (_networkModel.isProviderSupportServiceInNetwork(network.getId(), Service.Gateway, Provider.VPCVirtualRouter)) {
                            // Networks that support the Gateway service acquire the gateway ip on their nic
                            guestIp = network.getGateway();
                        } else {
                            // In other cases, acquire an ip address from the DHCP range (take lowest possible)
                            guestIp = _ipAddrMgr.acquireGuestIpAddressForRouter(network, nic.getRequestedIPv4());
                        }
                        break;
                    default:
                        // Backwards compatibility
                        guestIp = _ipAddrMgr.acquireGuestIpAddress(network, nic.getRequestedIPv4());
                        break;
                }

                if (guestIp == null) {
                    throw new InsufficientVirtualNetworkCapacityException("Unable to acquire Guest IP" +
                            " address for network " + network, DataCenter.class,
                            zone.getId());
                }
                nic.setIPv4Address(guestIp);
                nic.setIPv4Netmask(NetUtils.cidr2Netmask(network.getCidr()));

                if (network.getDns1() != null && network.getDns1().equals("")) {
                    nic.setIPv4Dns1(null);
                } else {
                    nic.setIPv4Dns1(network.getDns1());
                }
                if (network.getDns2() != null && network.getDns2().equals("")) {
                    nic.setIPv4Dns2(null);
                } else {
                    nic.setIPv4Dns2(network.getDns2());
                }

                nic.setFormat(IpAddressFormat.Ip4);
            }
        }

        nic.setReservationStrategy(ReservationStrategy.Start);

        if (nic.getMacAddress() == null) {
            nic.setMacAddress(_networkModel.getNextAvailableMacAddressInNetwork(network.getId()));
            if (nic.getMacAddress() == null) {
                throw new InsufficientAddressCapacityException("Unable to allocate more mac addresses", Network.class, network.getId());
            }
        }

        return nic;
    }

    private String assignGuestOrGatewayIp(final Network network, final NicProfile nic, final VirtualMachineProfile vm, final Zone zone) throws InsufficientVirtualNetworkCapacityException {
        String guestIp;
        // Default assign new ip
        guestIp = _ipAddrMgr.acquireGuestIpAddress(network, nic.getRequestedIPv4());

        // Is our user VM supposed to be a remote gateway?
        final long templateId = vm.getTemplateId();
        final VMTemplateVO vmTemplateVO = _templateDao.findById(templateId);
        final Boolean isRemoteGatewayTemplate = vmTemplateVO.getRemoteGatewayTemplate();
        s_logger.debug("isRemoteGatewayTemplate has value " + isRemoteGatewayTemplate.toString());
        final long networkOfferingId = network.getNetworkOfferingId();
        NetworkOffering networkOffering = _networkOfferingDao.findById(networkOfferingId);

        s_logger.debug("Check if the gateway ip is requested");
        // Check if we can assign the gateway
        if (nic.getIPv4Gateway() != null && nic.getIPv4Gateway().equals(nic.getRequestedIPv4())) {
            s_logger.debug("VM requests gateway ip address for network " + network.getName() + " with offering " + networkOffering.getName() + " . Check service offering");

            final boolean networkOfferingSupportsGatewayService = _networkOfferingServiceMapDao.areServicesSupportedByNetworkOffering(networkOfferingId, Service.Gateway);
            s_logger.debug("networkOfferingSupportsGatewayService has value " + networkOfferingSupportsGatewayService);

            // The VM we start is based on a template that is a RemoteGateway AND the network itself does not have the Gateway service (so the vm can be the gateway)
            if (isRemoteGatewayTemplate && !networkOfferingSupportsGatewayService) {
                // Assign gateway
                guestIp = nic.getIPv4Gateway();
                s_logger.debug("VM requests gateway ip address for network " + network.getName() + " with offering " + networkOffering.getName() + ". " +
                        "Allowed! IP address " + guestIp + " (aka gateway) will be assigned.");
            } else {
                s_logger.debug("Requested network ip is gateway but either template is not RemoteGateway enabled or network VPC router has Gateway service.");
                throw new InsufficientVirtualNetworkCapacityException("Unable to acquire Gateway IP address. Template needs to be RemoteGateway enabled" +
                        " or network VPC router already has Gateway service. Network: " + network, DataCenter.class, zone.getId());
            }
        }
        s_logger.debug("Acquired guest ip is " + guestIp);
        return guestIp;
    }

    @Override
    public void reserve(final NicProfile nic, final Network network, final VirtualMachineProfile vm, final DeployDestination dest, final ReservationContext context)
            throws InsufficientVirtualNetworkCapacityException, InsufficientAddressCapacityException {
        assert nic.getReservationStrategy() == ReservationStrategy.Start : "What can I do for nics that are not allocated at start? ";

        nic.setBroadcastUri(network.getBroadcastUri());
        nic.setIsolationUri(network.getBroadcastUri());
    }

    @Override
    public boolean release(final NicProfile nic, final VirtualMachineProfile vm, final String reservationId) {
        nic.setBroadcastUri(null);
        nic.setIsolationUri(null);
        return true;
    }

    @Override
    @DB
    public void deallocate(final Network network, final NicProfile nic, final VirtualMachineProfile vm) {
        if (network.getSpecifyIpRanges()) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Deallocate network: networkId: " + nic.getNetworkId() + ", ip: " + nic.getIPv4Address());
            }

            final IPAddressVO ip = _ipAddressDao.findByIpAndSourceNetworkId(nic.getNetworkId(), nic.getIPv4Address());
            if (ip != null) {
                Transaction.execute(new TransactionCallbackNoReturn() {
                    @Override
                    public void doInTransactionWithoutResult(final TransactionStatus status) {
                        _ipAddrMgr.markIpAsUnavailable(ip.getId());
                        _ipAddressDao.unassignIpAddress(ip.getId());
                    }
                });
            }
            nic.deallocate();
        }
    }

    @Override
    public void updateNicProfile(final NicProfile profile, final Network network) {
        final DataCenter dc = _dcDao.findById(network.getDataCenterId());
        if (profile != null) {
            profile.setIPv4Dns1(dc.getDns1());
            profile.setIPv4Dns2(dc.getDns2());
        }
    }

    @Override
    public void shutdown(final NetworkProfile profile, final NetworkOffering offering) {
        if (profile.getBroadcastUri() == null) {
            return; // Nothing to do here if the uri is null already
        }

        if ((profile.getBroadcastDomainType() == BroadcastDomainType.Vlan || profile.getBroadcastDomainType() == BroadcastDomainType.Vxlan) && !offering.getSpecifyVlan()) {
            s_logger.debug("Releasing vnet for the network id=" + profile.getId());
            _dcDao.releaseVnet(BroadcastDomainType.getValue(profile.getBroadcastUri()), profile.getDataCenterId(), profile.getPhysicalNetworkId(), profile.getAccountId(),
                    profile.getReservationId());
            ActionEventUtils.onCompletedActionEvent(CallContext.current().getCallingUserId(), profile.getAccountId(), EventVO.LEVEL_INFO, EventTypes.EVENT_ZONE_VLAN_RELEASE,
                    "Released Zone Vnet: " + BroadcastDomainType.getValue(profile.getBroadcastUri()) + " for Network: " + profile.getId(), 0);
        }

        profile.setBroadcastUri(null);
    }

    @Override
    public boolean trash(final Network network, final NetworkOffering offering) {
        return true;
    }

    @Override
    public void updateNetworkProfile(final NetworkProfile networkProfile) {
        final DataCenter dc = _dcDao.findById(networkProfile.getDataCenterId());
        if (networkProfile.getDns1() != null && networkProfile.getDns1().equals("")) {
            networkProfile.setDns1(null);
        }
        if (networkProfile.getDns2() != null && networkProfile.getDns2().equals("")) {
            networkProfile.setDns2(null);
        }
    }

    @Override
    public TrafficType[] getSupportedTrafficType() {
        return TrafficTypes;
    }

    @Override
    public boolean isMyTrafficType(final TrafficType type) {
        for (final TrafficType t : TrafficTypes) {
            if (t == type) {
                return true;
            }
        }
        s_logger.debug("Traffic type " + type + " is not supported by this guru");
        return false;
    }

    public int getVlanOffset(final long physicalNetworkId, final int vlanTag) {
        final PhysicalNetworkVO pNetwork = _physicalNetworkDao.findById(physicalNetworkId);
        if (pNetwork == null) {
            throw new CloudRuntimeException("Could not find the physical Network " + physicalNetworkId + ".");
        }

        if (pNetwork.getVnet() == null) {
            throw new CloudRuntimeException("Could not find vlan range for physical Network " + physicalNetworkId + ".");
        }
        Integer lowestVlanTag = null;
        final List<Pair<Integer, Integer>> vnetList = pNetwork.getVnet();
        //finding the vlanrange in which the vlanTag lies.
        for (final Pair<Integer, Integer> vnet : vnetList) {
            if (vlanTag >= vnet.first() && vlanTag <= vnet.second()) {
                lowestVlanTag = vnet.first();
            }
        }
        if (lowestVlanTag == null) {
            throw new InvalidParameterValueException("The vlan tag does not belong to any of the existing vlan ranges");
        }
        return vlanTag - lowestVlanTag;
    }

    public int getGloballyConfiguredCidrSize() {
        try {
            final String globalVlanBits = _configDao.getValue(Config.GuestVlanBits.key());
            return 8 + Integer.parseInt(globalVlanBits);
        } catch (final Exception e) {
            throw new CloudRuntimeException("Failed to read the globally configured VLAN bits size.");
        }
    }

    @Override
    public String getConfigComponentName() {
        return GuestNetworkGuru.class.getSimpleName();
    }

    @Override
    public ConfigKey<?>[] getConfigKeys() {
        return new ConfigKey<?>[]{UseSystemGuestVlans};
    }
}
