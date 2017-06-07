package com.cloud.network.guru;

import static com.cloud.network.guru.GuestNetworkGuru.UseSystemGuestVlans;

import com.cloud.configuration.ConfigurationManager;
import com.cloud.context.CallContext;
import com.cloud.dao.EntityManager;
import com.cloud.dc.DataCenter;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.deploy.DeployDestination;
import com.cloud.deploy.DeploymentPlan;
import com.cloud.event.ActionEventUtils;
import com.cloud.event.EventTypes;
import com.cloud.event.EventVO;
import com.cloud.exception.InsufficientAddressCapacityException;
import com.cloud.exception.InsufficientVirtualNetworkCapacityException;
import com.cloud.model.enumeration.NetworkType;
import com.cloud.network.Network;
import com.cloud.network.Network.GuestType;
import com.cloud.network.Network.State;
import com.cloud.network.NetworkModel;
import com.cloud.network.NetworkProfile;
import com.cloud.network.Networks.AddressFormat;
import com.cloud.network.Networks.BroadcastDomainType;
import com.cloud.network.Networks.IsolationType;
import com.cloud.network.Networks.Mode;
import com.cloud.network.Networks.TrafficType;
import com.cloud.network.PhysicalNetwork;
import com.cloud.network.dao.NetworkVO;
import com.cloud.network.dao.PhysicalNetworkDao;
import com.cloud.network.dao.PhysicalNetworkVO;
import com.cloud.network.vpc.PrivateIpAddress;
import com.cloud.network.vpc.PrivateIpVO;
import com.cloud.network.vpc.dao.PrivateIpDao;
import com.cloud.offering.NetworkOffering;
import com.cloud.user.Account;
import com.cloud.utils.component.AdapterBase;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.exception.InvalidParameterValueException;
import com.cloud.utils.net.NetUtils;
import com.cloud.vm.Nic.ReservationStrategy;
import com.cloud.vm.NicProfile;
import com.cloud.vm.ReservationContext;
import com.cloud.vm.VirtualMachineProfile;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrivateNetworkGuru extends AdapterBase implements NetworkGuru {
    private static final Logger s_logger = LoggerFactory.getLogger(PrivateNetworkGuru.class);
    private static final TrafficType[] TrafficTypes = {TrafficType.Guest};
    @Inject
    protected ConfigurationManager _configMgr;
    @Inject
    protected PrivateIpDao _privateIpDao;
    @Inject
    protected NetworkModel _networkMgr;
    @Inject
    EntityManager _entityMgr;
    @Inject
    protected DataCenterDao _dcDao;
    @Inject
    protected NetworkModel _networkModel;
    @Inject
    protected PhysicalNetworkDao physicalNetworkDao;

    protected PhysicalNetwork.IsolationMethod[] _isolationMethods;

    protected PrivateNetworkGuru() {
        super();
        _isolationMethods = new PhysicalNetwork.IsolationMethod[]{PhysicalNetwork.IsolationMethod.VLAN};
    }

    @Override
    public Network design(final NetworkOffering offering, final DeploymentPlan plan, final Network userSpecified, final Account owner) {
        final PhysicalNetworkVO physnet = physicalNetworkDao.findById(plan.getPhysicalNetworkId());
        final DataCenter dc = _entityMgr.findById(DataCenter.class, plan.getDataCenterId());
        if (!canHandle(offering, dc, physnet)) {
            return null;
        }

        final BroadcastDomainType broadcastType;
        if (userSpecified != null && userSpecified.getBroadcastDomainType() != null) {
            broadcastType = userSpecified.getBroadcastDomainType();
        } else {
            broadcastType = BroadcastDomainType.Vlan;
        }
        final NetworkVO network =
                new NetworkVO(offering.getTrafficType(), Mode.Static, broadcastType, offering.getId(), State.Allocated, plan.getDataCenterId(),
                        plan.getPhysicalNetworkId(), offering.getRedundantRouter());
        if (userSpecified != null) {
            if (!GuestType.Private.equals(offering.getGuestType()) &&
                    ((userSpecified.getCidr() == null && userSpecified.getGateway() != null) || (userSpecified.getCidr() != null && userSpecified.getGateway() == null))) {
                throw new InvalidParameterValueException("CIDR and gateway must be specified together or the CIDR must represents the gateway.");
            }

            if (userSpecified.getCidr() != null) {
                network.setCidr(userSpecified.getCidr());
                network.setGateway(userSpecified.getGateway());
            } else {
                throw new InvalidParameterValueException("Can't design network " + network + "; netmask/gateway or cidr must be passed in");
            }

            if (offering.getSpecifyVlan()) {
                network.setBroadcastUri(userSpecified.getBroadcastUri());
                network.setState(State.Setup);
            }
        } else {
            throw new CloudRuntimeException("Can't design network " + network + "; netmask/gateway or cidr must be passed in");
        }

        return network;
    }

    protected boolean canHandle(final NetworkOffering offering, final DataCenter dc, final PhysicalNetwork physicalNetwork) {
        // This guru handles only system Guest network
        if (dc.getNetworkType() == NetworkType.Advanced && isMyTrafficType(offering.getTrafficType()) &&
                isMyIsolationMethod(physicalNetwork) && ((offering.getGuestType() == GuestType.Isolated && offering.isSystemOnly()) || (offering.getGuestType() == GuestType
                .Private && !offering.isSystemOnly()))
                ) {
            return true;
        } else {
            s_logger.trace("We only take care of system Guest networks of type   " + GuestType.Isolated + " in zone of type " + NetworkType.Advanced);
            return false;
        }
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

        for (final PhysicalNetwork.IsolationMethod m : _isolationMethods) {
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

    @Override
    public Network implement(final Network network, final NetworkOffering offering, final DeployDestination dest, final ReservationContext context)
            throws InsufficientVirtualNetworkCapacityException {
        assert network.getState() == State.Implementing : "Why are we implementing " + network;

        final long dcId = dest.getDataCenter().getId();

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
        final DataCenter dc = _entityMgr.findById(DataCenter.class, network.getDataCenterId());

        if (nic == null) {
            nic = new NicProfile(ReservationStrategy.Create, null, null, null, null);
        }

        getIp(nic, dc, network);

        if (nic.getIPv4Address() == null) {
            nic.setReservationStrategy(ReservationStrategy.Start);
        } else {
            nic.setReservationStrategy(ReservationStrategy.Create);
        }

        return nic;
    }

    protected void getIp(final NicProfile nic, final DataCenter dc, final Network network) throws InsufficientVirtualNetworkCapacityException,
            InsufficientAddressCapacityException {
        if (nic.getIPv4Address() == null) {
            final PrivateIpVO ipVO = _privateIpDao.allocateIpAddress(network.getDataCenterId(), network.getId(), network.getVpcId(), null);
            final String vlanTag = BroadcastDomainType.getValue(network.getBroadcastUri());
            final String netmask = NetUtils.getCidrNetmask(network.getCidr());
            final PrivateIpAddress ip =
                    new PrivateIpAddress(ipVO, vlanTag, network.getGateway(), netmask, NetUtils.long2Mac(NetUtils.createSequenceBasedMacAddress(ipVO.getMacAddress())));

            nic.setIPv4Address(ip.getIpAddress());
            nic.setIPv4Gateway(ip.getGateway());
            nic.setIPv4Netmask(ip.getNetmask());
            nic.setIsolationUri(IsolationType.Vlan.toUri(ip.getBroadcastUri()));
            nic.setBroadcastUri(IsolationType.Vlan.toUri(ip.getBroadcastUri()));
            nic.setBroadcastType(BroadcastDomainType.Vlan);
            nic.setFormat(AddressFormat.Ip4);
            nic.setReservationId(String.valueOf(ip.getBroadcastUri()));
            nic.setMacAddress(ip.getMacAddress());
        }

        nic.setIPv4Dns1(dc.getDns1());
        nic.setIPv4Dns2(dc.getDns2());
    }

    @Override
    public void reserve(final NicProfile nic, final Network network, final VirtualMachineProfile vm, final DeployDestination dest, final ReservationContext context)
            throws InsufficientVirtualNetworkCapacityException, InsufficientAddressCapacityException {
        if (nic.getIPv4Address() == null) {
            getIp(nic, _entityMgr.findById(DataCenter.class, network.getDataCenterId()), network);
            nic.setReservationStrategy(ReservationStrategy.Create);
        }
    }

    @Override
    public boolean release(final NicProfile nic, final VirtualMachineProfile vm, final String reservationId) {
        return true;
    }

    @Override
    public void deallocate(final Network network, final NicProfile nic, final VirtualMachineProfile vm) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Deallocate network: networkId: " + nic.getNetworkId() + ", ip: " + nic.getIPv4Address());
        }

        final PrivateIpVO ip = _privateIpDao.findByIpAndSourceNetworkId(nic.getNetworkId(), nic.getIPv4Address());
        if (ip != null) {
            _privateIpDao.releaseIpAddress(nic.getIPv4Address(), nic.getNetworkId());
        }
        nic.deallocate();
    }

    @Override
    public void updateNicProfile(final NicProfile profile, final Network network) {
        final DataCenter dc = _entityMgr.findById(DataCenter.class, network.getDataCenterId());
        if (profile != null) {
            profile.setIPv4Dns1(dc.getDns1());
            profile.setIPv4Dns2(dc.getDns2());
        }
    }

    @Override
    public void shutdown(final NetworkProfile profile, final NetworkOffering offering) {

    }

    @Override
    public boolean trash(final Network network, final NetworkOffering offering) {
        return true;
    }

    @Override
    public void updateNetworkProfile(final NetworkProfile networkProfile) {
        final DataCenter dc = _entityMgr.findById(DataCenter.class, networkProfile.getDataCenterId());
        networkProfile.setDns1(dc.getDns1());
        networkProfile.setDns2(dc.getDns2());
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
        return false;
    }
}
