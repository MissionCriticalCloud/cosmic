package com.cloud.network.guru;

import com.cloud.dc.DataCenter;
import com.cloud.dc.Vlan.VlanType;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dc.dao.VlanDao;
import com.cloud.deploy.DeployDestination;
import com.cloud.deploy.DeploymentPlan;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientAddressCapacityException;
import com.cloud.exception.InsufficientVirtualNetworkCapacityException;
import com.cloud.network.IpAddressManager;
import com.cloud.network.Network;
import com.cloud.network.Network.State;
import com.cloud.network.NetworkProfile;
import com.cloud.network.Networks.AddressFormat;
import com.cloud.network.Networks.BroadcastDomainType;
import com.cloud.network.Networks.IsolationType;
import com.cloud.network.Networks.Mode;
import com.cloud.network.Networks.TrafficType;
import com.cloud.network.addr.PublicIp;
import com.cloud.network.dao.IPAddressDao;
import com.cloud.network.dao.IPAddressVO;
import com.cloud.network.dao.NetworkVO;
import com.cloud.offering.NetworkOffering;
import com.cloud.user.Account;
import com.cloud.utils.component.AdapterBase;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.db.TransactionCallbackNoReturn;
import com.cloud.utils.db.TransactionStatus;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.Nic.ReservationStrategy;
import com.cloud.vm.NicProfile;
import com.cloud.vm.ReservationContext;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachineProfile;
import org.apache.cloudstack.engine.orchestration.service.NetworkOrchestrationService;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PublicNetworkGuru extends AdapterBase implements NetworkGuru {
    private static final Logger s_logger = LoggerFactory.getLogger(PublicNetworkGuru.class);
    private static final TrafficType[] TrafficTypes = {TrafficType.Public};
    @Inject
    DataCenterDao _dcDao;
    @Inject
    VlanDao _vlanDao;
    @Inject
    NetworkOrchestrationService _networkMgr;
    @Inject
    IPAddressDao _ipAddressDao;
    @Inject
    IpAddressManager _ipAddrMgr;

    protected PublicNetworkGuru() {
        super();
    }

    @Override
    public Network design(final NetworkOffering offering, final DeploymentPlan plan, final Network network, final Account owner) {
        if (!canHandle(offering)) {
            return null;
        }

        if (offering.getTrafficType() == TrafficType.Public) {
            final NetworkVO ntwk =
                    new NetworkVO(offering.getTrafficType(), Mode.Static, network.getBroadcastDomainType(), offering.getId(), State.Setup, plan.getDataCenterId(),
                            plan.getPhysicalNetworkId(), offering.getRedundantRouter());
            return ntwk;
        } else {
            return null;
        }
    }

    protected boolean canHandle(final NetworkOffering offering) {
        return isMyTrafficType(offering.getTrafficType()) && offering.isSystemOnly();
    }

    @Override
    public Network implement(final Network network, final NetworkOffering offering, final DeployDestination destination, final ReservationContext context)
            throws InsufficientVirtualNetworkCapacityException {
        return network;
    }

    @Override
    public NicProfile allocate(final Network network, NicProfile nic, final VirtualMachineProfile vm) throws InsufficientVirtualNetworkCapacityException,
            InsufficientAddressCapacityException, ConcurrentOperationException {

        final DataCenter dc = _dcDao.findById(network.getDataCenterId());

        if (nic != null && nic.getRequestedIPv4() != null) {
            throw new CloudRuntimeException("Does not support custom ip allocation at this time: " + nic);
        }

        if (nic == null) {
            nic = new NicProfile(ReservationStrategy.Create, null, null, null, null);
        }

        getIp(nic, dc, vm, network);

        if (nic.getIPv4Address() == null) {
            nic.setReservationStrategy(ReservationStrategy.Start);
        } else if (vm.getVirtualMachine().getType() == VirtualMachine.Type.DomainRouter) {
            nic.setReservationStrategy(ReservationStrategy.Managed);
        } else {
            nic.setReservationStrategy(ReservationStrategy.Create);
        }

        return nic;
    }

    protected void getIp(final NicProfile nic, final DataCenter dc, final VirtualMachineProfile vm, final Network network) throws InsufficientVirtualNetworkCapacityException,
            InsufficientAddressCapacityException, ConcurrentOperationException {
        if (nic.getIPv4Address() == null) {
            final PublicIp ip = _ipAddrMgr.assignPublicIpAddress(dc.getId(), null, vm.getOwner(), VlanType.VirtualNetwork, null, null, false);
            nic.setIPv4Address(ip.getAddress().toString());
            nic.setIPv4Gateway(ip.getGateway());
            nic.setIPv4Netmask(ip.getNetmask());
            if (network.getBroadcastDomainType() == BroadcastDomainType.Vxlan) {
                nic.setIsolationUri(BroadcastDomainType.Vxlan.toUri(ip.getVlanTag()));
                nic.setBroadcastUri(BroadcastDomainType.Vxlan.toUri(ip.getVlanTag()));
                nic.setBroadcastType(BroadcastDomainType.Vxlan);
            } else {
                nic.setIsolationUri(IsolationType.Vlan.toUri(ip.getVlanTag()));
                nic.setBroadcastUri(BroadcastDomainType.Vlan.toUri(ip.getVlanTag()));
                nic.setBroadcastType(BroadcastDomainType.Vlan);
            }
            nic.setFormat(AddressFormat.Ip4);
            nic.setReservationId(String.valueOf(ip.getVlanTag()));
            nic.setMacAddress(ip.getMacAddress());
        }

        nic.setIPv4Dns1(dc.getDns1());
        nic.setIPv4Dns2(dc.getDns2());
    }

    @Override
    public void reserve(final NicProfile nic, final Network network, final VirtualMachineProfile vm, final DeployDestination dest, final ReservationContext context)
            throws InsufficientVirtualNetworkCapacityException, InsufficientAddressCapacityException, ConcurrentOperationException {
        if (nic.getIPv4Address() == null) {
            getIp(nic, dest.getDataCenter(), vm, network);
        }
    }

    @Override
    public boolean release(final NicProfile nic, final VirtualMachineProfile vm, final String reservationId) {
        return true;
    }

    @Override
    @DB
    public void deallocate(final Network network, final NicProfile nic, final VirtualMachineProfile vm) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("public network deallocate network: networkId: " + nic.getNetworkId() + ", ip: " + nic.getIPv4Address());
        }

        final IPAddressVO ip = _ipAddressDao.findByIpAndSourceNetworkId(nic.getNetworkId(), nic.getIPv4Address());
        if (ip != null && nic.getReservationStrategy() != ReservationStrategy.Managed) {
            Transaction.execute(new TransactionCallbackNoReturn() {
                @Override
                public void doInTransactionWithoutResult(final TransactionStatus status) {
                    _ipAddrMgr.markIpAsUnavailable(ip.getId());
                    _ipAddressDao.unassignIpAddress(ip.getId());
                }
            });
        }
        nic.deallocate();

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Deallocated nic: " + nic);
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
    public void shutdown(final NetworkProfile network, final NetworkOffering offering) {
    }

    @Override
    public boolean trash(final Network network, final NetworkOffering offering) {
        return true;
    }

    @Override
    public void updateNetworkProfile(final NetworkProfile networkProfile) {
        final DataCenter dc = _dcDao.findById(networkProfile.getDataCenterId());
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
