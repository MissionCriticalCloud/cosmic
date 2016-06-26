package com.cloud.network.guru;

import com.cloud.dc.Pod;
import com.cloud.dc.StorageNetworkIpAddressVO;
import com.cloud.deploy.DeployDestination;
import com.cloud.deploy.DeploymentPlan;
import com.cloud.exception.InsufficientAddressCapacityException;
import com.cloud.exception.InsufficientVirtualNetworkCapacityException;
import com.cloud.network.Network;
import com.cloud.network.NetworkProfile;
import com.cloud.network.Networks.AddressFormat;
import com.cloud.network.Networks.BroadcastDomainType;
import com.cloud.network.Networks.Mode;
import com.cloud.network.Networks.TrafficType;
import com.cloud.network.StorageNetworkManager;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.dao.NetworkVO;
import com.cloud.offering.NetworkOffering;
import com.cloud.user.Account;
import com.cloud.utils.net.NetUtils;
import com.cloud.vm.Nic.ReservationStrategy;
import com.cloud.vm.NicProfile;
import com.cloud.vm.ReservationContext;
import com.cloud.vm.VirtualMachineProfile;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageNetworkGuru extends PodBasedNetworkGuru implements NetworkGuru {
    private static final Logger s_logger = LoggerFactory.getLogger(StorageNetworkGuru.class);
    private static final TrafficType[] TrafficTypes = {TrafficType.Storage};
    @Inject
    StorageNetworkManager _sNwMgr;
    @Inject
    NetworkDao _nwDao;

    protected StorageNetworkGuru() {
        super();
    }

    @Override
    public Network design(final NetworkOffering offering, final DeploymentPlan plan, final Network userSpecified, final Account owner) {
        if (!canHandle(offering)) {
            return null;
        }

        final NetworkVO config =
                new NetworkVO(offering.getTrafficType(), Mode.Static, BroadcastDomainType.Native, offering.getId(), Network.State.Setup, plan.getDataCenterId(),
                        plan.getPhysicalNetworkId(), offering.getRedundantRouter());
        return config;
    }

    protected boolean canHandle(final NetworkOffering offering) {
        if (isMyTrafficType(offering.getTrafficType()) && offering.isSystemOnly()) {
            return true;
        } else {
            s_logger.trace("It's not storage network offering, skip it.");
            return false;
        }
    }

    @Override
    public Network implement(final Network network, final NetworkOffering offering, final DeployDestination destination, final ReservationContext context)
            throws InsufficientVirtualNetworkCapacityException {
        assert network.getTrafficType() == TrafficType.Storage : "Why are you sending this configuration to me " + network;
        if (!_sNwMgr.isStorageIpRangeAvailable(destination.getDataCenter().getId())) {
            return super.implement(network, offering, destination, context);
        }
        return network;
    }

    @Override
    public NicProfile allocate(final Network network, final NicProfile nic, final VirtualMachineProfile vm) throws InsufficientVirtualNetworkCapacityException,
            InsufficientAddressCapacityException {
        assert network.getTrafficType() == TrafficType.Storage : "Well, I can't take care of this config now can I? " + network;
        if (!_sNwMgr.isStorageIpRangeAvailable(network.getDataCenterId())) {
            return super.allocate(network, nic, vm);
        }

        return new NicProfile(ReservationStrategy.Start, null, null, null, null);
    }

    @Override
    public void reserve(final NicProfile nic, final Network network, final VirtualMachineProfile vm, final DeployDestination dest, final ReservationContext context)
            throws InsufficientVirtualNetworkCapacityException, InsufficientAddressCapacityException {
        if (!_sNwMgr.isStorageIpRangeAvailable(dest.getDataCenter().getId())) {
            super.reserve(nic, network, vm, dest, context);
            return;
        }

        final Pod pod = dest.getPod();
        Integer vlan = null;

        final StorageNetworkIpAddressVO ip = _sNwMgr.acquireIpAddress(pod.getId());
        if (ip == null) {
            throw new InsufficientAddressCapacityException("Unable to get a storage network ip address", Pod.class, pod.getId());
        }

        vlan = ip.getVlan();
        nic.setIPv4Address(ip.getIpAddress());
        nic.setMacAddress(NetUtils.long2Mac(NetUtils.createSequenceBasedMacAddress(ip.getMac())));
        nic.setFormat(AddressFormat.Ip4);
        nic.setIPv4Netmask(ip.getNetmask());
        nic.setBroadcastType(BroadcastDomainType.Storage);
        nic.setIPv4Gateway(ip.getGateway());
        if (vlan != null) {
            nic.setBroadcastUri(BroadcastDomainType.Storage.toUri(vlan));
        } else {
            nic.setBroadcastUri(null);
        }
        nic.setIsolationUri(null);
        s_logger.debug("Allocated a storage nic " + nic + " for " + vm);
    }

    @Override
    public boolean release(final NicProfile nic, final VirtualMachineProfile vm, final String reservationId) {
        final Network nw = _nwDao.findById(nic.getNetworkId());
        if (!_sNwMgr.isStorageIpRangeAvailable(nw.getDataCenterId())) {
            return super.release(nic, vm, reservationId);
        }

        _sNwMgr.releaseIpAddress(nic.getIPv4Address());
        s_logger.debug("Release an storage ip " + nic.getIPv4Address());
        nic.deallocate();
        return true;
    }

    @Override
    public void deallocate(final Network network, final NicProfile nic, final VirtualMachineProfile vm) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateNicProfile(final NicProfile profile, final Network network) {
        // TODO Auto-generated method stub

    }

    @Override
    public void shutdown(final NetworkProfile network, final NetworkOffering offering) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean trash(final Network network, final NetworkOffering offering) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void updateNetworkProfile(final NetworkProfile networkProfile) {
        // TODO Auto-generated method stub

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
