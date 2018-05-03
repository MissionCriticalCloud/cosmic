package com.cloud.network.guru;

import com.cloud.deploy.DeployDestination;
import com.cloud.deploy.DeploymentPlan;
import com.cloud.legacymodel.exceptions.InsufficientAddressCapacityException;
import com.cloud.legacymodel.exceptions.InsufficientVirtualNetworkCapacityException;
import com.cloud.legacymodel.network.Nic.ReservationStrategy;
import com.cloud.legacymodel.user.Account;
import com.cloud.model.enumeration.GuestType;
import com.cloud.model.enumeration.NetworkType;
import com.cloud.network.Network;
import com.cloud.network.Network.State;
import com.cloud.network.PhysicalNetwork;
import com.cloud.network.PhysicalNetwork.IsolationMethod;
import com.cloud.network.dao.NetworkVO;
import com.cloud.offering.NetworkOffering;
import com.cloud.utils.db.DB;
import com.cloud.vm.NicProfile;
import com.cloud.vm.ReservationContext;
import com.cloud.vm.VirtualMachineProfile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExternalGuestNetworkGuru extends GuestNetworkGuru {
    private static final Logger s_logger = LoggerFactory.getLogger(ExternalGuestNetworkGuru.class);

    public ExternalGuestNetworkGuru() {
        super();
        _isolationMethods = new IsolationMethod[]{IsolationMethod.L3, IsolationMethod.VLAN};
    }

    @Override
    public Network design(final NetworkOffering offering, final DeploymentPlan plan, final Network userSpecified, final Account owner) {

        if (_networkModel.areServicesSupportedByNetworkOffering(offering.getId(), Network.Service.Connectivity)) {
            return null;
        }

        final NetworkVO config = (NetworkVO) super.design(offering, plan, userSpecified, owner);
        if (config == null) {
            return null;
        }

        return config;
    }

    @Override
    protected boolean canHandle(final NetworkOffering offering, final NetworkType networkType, final PhysicalNetwork physicalNetwork) {
        s_logger.debug("Checking of guru can handle request");
        if (NetworkType.Advanced.equals(networkType) &&
                isMyTrafficType(offering.getTrafficType()) &&
                isMyIsolationMethod(physicalNetwork) &&
                GuestType.Isolated.equals(offering.getGuestType()) &&
                !offering.isSystemOnly()) {
            return true;
        } else if (NetworkType.Advanced.equals(networkType) &&
                isMyTrafficType(offering.getTrafficType()) &&
                GuestType.Sync.equals(offering.getGuestType())) {
            return true;
        } else {
            s_logger.trace("We only take care of Guest networks of type   " + GuestType.Isolated + " in zone of type " + NetworkType.Advanced);
            return false;
        }
    }

    @Override
    public Network implement(final Network config, final NetworkOffering offering, final DeployDestination dest, final ReservationContext context)
            throws InsufficientVirtualNetworkCapacityException {
        assert (config.getState() == State.Implementing) : "Why are we implementing " + config;

        if (_networkModel.areServicesSupportedInNetwork(config.getId(), Network.Service.Connectivity)) {
            return null;
        }

        return super.implement(config, offering, dest, context);
    }

    @Override
    public NicProfile allocate(final Network config, final NicProfile nic, final VirtualMachineProfile vm) throws InsufficientVirtualNetworkCapacityException,
            InsufficientAddressCapacityException {
        return super.allocate(config, nic, vm);
    }

    @Override
    public void reserve(final NicProfile nic, final Network config, final VirtualMachineProfile vm, final DeployDestination dest, final ReservationContext context)
            throws InsufficientVirtualNetworkCapacityException, InsufficientAddressCapacityException {
        assert (nic.getReservationStrategy() == ReservationStrategy.Start) : "What can I do for nics that are not allocated at start? ";

        super.reserve(nic, config, vm, dest, context);
    }

    @Override
    public boolean release(final NicProfile nic, final VirtualMachineProfile vm, final String reservationId) {
        return super.release(nic, vm, reservationId);
    }

    @Override
    @DB
    public void deallocate(final Network config, final NicProfile nic, final VirtualMachineProfile vm) {
        super.deallocate(config, nic, vm);
    }
}
