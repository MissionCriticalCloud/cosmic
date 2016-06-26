package com.cloud.network.guru;

import com.cloud.configuration.Config;
import com.cloud.dc.DataCenter;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.deploy.DeployDestination;
import com.cloud.deploy.DeploymentPlan;
import com.cloud.exception.InsufficientAddressCapacityException;
import com.cloud.exception.InsufficientVirtualNetworkCapacityException;
import com.cloud.network.Network;
import com.cloud.network.NetworkModel;
import com.cloud.network.NetworkProfile;
import com.cloud.network.Networks.AddressFormat;
import com.cloud.network.Networks.BroadcastDomainType;
import com.cloud.network.Networks.Mode;
import com.cloud.network.Networks.TrafficType;
import com.cloud.network.dao.NetworkVO;
import com.cloud.offering.NetworkOffering;
import com.cloud.user.Account;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.net.NetUtils;
import com.cloud.vm.Nic;
import com.cloud.vm.NicProfile;
import com.cloud.vm.ReservationContext;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachineProfile;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlNetworkGuru extends PodBasedNetworkGuru implements NetworkGuru {
    private static final Logger s_logger = LoggerFactory.getLogger(ControlNetworkGuru.class);
    private static final TrafficType[] TrafficTypes = {TrafficType.Control};
    @Inject
    DataCenterDao _dcDao;
    @Inject
    ConfigurationDao _configDao;
    @Inject
    NetworkModel _networkMgr;
    String _cidr;
    String _gateway;

    protected ControlNetworkGuru() {
        super();
    }

    protected boolean isRouterVm(final VirtualMachineProfile vm) {
        return vm.getType() == VirtualMachine.Type.DomainRouter || vm.getType() == VirtualMachine.Type.InternalLoadBalancerVm;
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        super.configure(name, params);

        final Map<String, String> dbParams = _configDao.getConfiguration(params);

        _cidr = dbParams.get(Config.ControlCidr.toString());
        if (_cidr == null) {
            _cidr = "169.254.0.0/16";
        }

        _gateway = dbParams.get(Config.ControlGateway.toString());
        if (_gateway == null) {
            _gateway = NetUtils.getLinkLocalGateway();
        }

        s_logger.info("Control network setup: cidr=" + _cidr + "; gateway = " + _gateway);

        return true;
    }

    @Override
    public Network design(final NetworkOffering offering, final DeploymentPlan plan, final Network specifiedConfig, final Account owner) {
        if (!canHandle(offering)) {
            return null;
        }

        final NetworkVO config =
                new NetworkVO(offering.getTrafficType(), Mode.Static, BroadcastDomainType.LinkLocal, offering.getId(), Network.State.Setup, plan.getDataCenterId(),
                        plan.getPhysicalNetworkId(), offering.getRedundantRouter());
        config.setCidr(_cidr);
        config.setGateway(_gateway);

        return config;
    }

    protected boolean canHandle(final NetworkOffering offering) {
        if (offering.isSystemOnly() && isMyTrafficType(offering.getTrafficType())) {
            return true;
        } else {
            s_logger.trace("We only care about System only Control network");
            return false;
        }
    }

    @Override
    public Network implement(final Network config, final NetworkOffering offering, final DeployDestination destination, final ReservationContext context)
            throws InsufficientVirtualNetworkCapacityException {
        assert config.getTrafficType() == TrafficType.Control : "Why are you sending this configuration to me " + config;
        return config;
    }

    @Override
    public NicProfile allocate(final Network config, final NicProfile nic, final VirtualMachineProfile vm) throws InsufficientVirtualNetworkCapacityException,
            InsufficientAddressCapacityException {

        if (nic != null) {
            throw new CloudRuntimeException("Does not support nic specification at this time: " + nic);
        }

        return new NicProfile(Nic.ReservationStrategy.Start, null, null, null, null);
    }

    @Override
    public void reserve(final NicProfile nic, final Network config, final VirtualMachineProfile vm, final DeployDestination dest, final ReservationContext context)
            throws InsufficientVirtualNetworkCapacityException, InsufficientAddressCapacityException {
        assert nic.getTrafficType() == TrafficType.Control;

        final String ip = _dcDao.allocateLinkLocalIpAddress(dest.getDataCenter().getId(), dest.getPod().getId(), nic.getId(), context.getReservationId());
        if (ip == null) {
            throw new InsufficientAddressCapacityException("Insufficient link local address capacity", DataCenter.class, dest.getDataCenter().getId());
        }
        nic.setIPv4Address(ip);
        nic.setMacAddress(NetUtils.long2Mac(NetUtils.ip2Long(ip) | 14l << 40));
        nic.setIPv4Netmask("255.255.0.0");
        nic.setFormat(AddressFormat.Ip4);
        nic.setIPv4Gateway(NetUtils.getLinkLocalGateway());
    }

    @Override
    public boolean release(final NicProfile nic, final VirtualMachineProfile vm, final String reservationId) {
        assert nic.getTrafficType() == TrafficType.Control;

        _dcDao.releaseLinkLocalIpAddress(nic.getId(), reservationId);

        nic.deallocate();
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Released nic: " + nic);
        }

        return true;
    }

    @Override
    public void deallocate(final Network config, final NicProfile nic, final VirtualMachineProfile vm) {
    }

    @Override
    public void shutdown(final NetworkProfile config, final NetworkOffering offering) {
        assert false : "Destroying a link local...Either you're out of your mind or something has changed.";
    }

    @Override
    public boolean trash(final Network config, final NetworkOffering offering) {
        return true;
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
