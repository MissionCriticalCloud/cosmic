package com.cloud.network.guru;

import com.cloud.agent.AgentManager;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.CreateLogicalSwitchAnswer;
import com.cloud.agent.api.CreateLogicalSwitchCommand;
import com.cloud.agent.api.DeleteLogicalSwitchAnswer;
import com.cloud.agent.api.DeleteLogicalSwitchCommand;
import com.cloud.agent.api.FindLogicalSwitchCommand;
import com.cloud.dc.DataCenter;
import com.cloud.deploy.DeployDestination;
import com.cloud.deploy.DeploymentPlan;
import com.cloud.exception.AgentUnavailableException;
import com.cloud.exception.InsufficientAddressCapacityException;
import com.cloud.exception.InsufficientVirtualNetworkCapacityException;
import com.cloud.exception.OperationTimedoutException;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.host.dao.HostDetailsDao;
import com.cloud.model.enumeration.NetworkType;
import com.cloud.network.Network;
import com.cloud.network.NetworkModel;
import com.cloud.network.NetworkProfile;
import com.cloud.network.Networks.BroadcastDomainType;
import com.cloud.network.NiciraNvpDeviceVO;
import com.cloud.network.PhysicalNetwork;
import com.cloud.network.PhysicalNetwork.IsolationMethod;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.dao.NetworkVO;
import com.cloud.network.dao.NiciraNvpDao;
import com.cloud.network.dao.PhysicalNetworkDao;
import com.cloud.network.dao.PhysicalNetworkVO;
import com.cloud.offering.NetworkOffering;
import com.cloud.offerings.dao.NetworkOfferingServiceMapDao;
import com.cloud.resource.ResourceManager;
import com.cloud.user.Account;
import com.cloud.user.dao.AccountDao;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.NicProfile;
import com.cloud.vm.ReservationContext;
import com.cloud.vm.VirtualMachineProfile;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NiciraNvpGuestNetworkGuru extends GuestNetworkGuru {
    private static final int MAX_NAME_LENGTH = 40;

    private static final Logger s_logger = LoggerFactory.getLogger(NiciraNvpGuestNetworkGuru.class);

    @Inject
    protected NetworkModel networkModel;
    @Inject
    protected NetworkDao networkDao;
    @Inject
    protected PhysicalNetworkDao physicalNetworkDao;
    @Inject
    protected AccountDao accountDao;
    @Inject
    protected NiciraNvpDao niciraNvpDao;
    @Inject
    protected HostDao hostDao;
    @Inject
    protected ResourceManager resourceMgr;
    @Inject
    protected AgentManager agentMgr;
    @Inject
    protected HostDetailsDao hostDetailsDao;
    @Inject
    protected NetworkOfferingServiceMapDao ntwkOfferingSrvcDao;

    public NiciraNvpGuestNetworkGuru() {
        super();
        _isolationMethods = new IsolationMethod[]{IsolationMethod.STT, IsolationMethod.VXLAN};
    }

    @Override
    protected boolean canHandle(final NetworkOffering offering, final NetworkType networkType, final PhysicalNetwork physicalNetwork) {
        s_logger.debug("Checking of guru can handle request");
        // This guru handles only Guest Isolated network that supports Source nat service
        if (networkType == NetworkType.Advanced &&
                isMyTrafficType(offering.getTrafficType()) &&
                offering.getGuestType() != Network.GuestType.Shared &&
                isMyIsolationMethod(physicalNetwork) &&
                ntwkOfferingSrvcDao.areServicesSupportedByNetworkOffering(offering.getId(), Network.Service.Connectivity)) {
            return true;
        } else {
            s_logger.debug("Cannot handle rquest. See GuestNetworkGuru message to check isolation methods. Details I have:\nNetwork type = " + networkType + "\nTraffic type = "
                    + offering.getTrafficType() + "\nGuest type = " + offering.getGuestType());
            return false;
        }
    }

    @Override
    public Network design(final NetworkOffering offering, final DeploymentPlan plan, final Network userSpecified, final Account owner) {
        // Check if the isolation type of the related physical network is supported
        PhysicalNetworkVO physnet = physicalNetworkDao.findById(plan.getPhysicalNetworkId());
        final DataCenter dc = _dcDao.findById(plan.getDataCenterId());
        if (physnet == null) {
            final List<PhysicalNetworkVO> physicalNetworks = physicalNetworkDao.listByZoneAndTrafficTypeAndIsolationMethod(
                    dc.getId(),
                    offering.getTrafficType(),
                    IsolationMethod.STT
            );

            if (!physicalNetworks.isEmpty()) {
                physnet = physicalNetworks.get(0);
                plan.setPhysicalNetworkId(physnet.getId());
            }
        }

        if (!canHandle(offering, dc.getNetworkType(), physnet)) {
            s_logger.debug(this.getClass().getSimpleName() + ": Refusing to design this network");
            return null;
        }

        final List<NiciraNvpDeviceVO> devices = niciraNvpDao.listByPhysicalNetwork(physnet.getId());
        if (devices.isEmpty()) {
            s_logger.error("No NiciraNvp Controller on physical network " + physnet.getName());
            return null;
        }
        final NiciraNvpDeviceVO niciraNvpDeviceVO = devices.get(0);
        s_logger.debug("Nicira Nvp " + niciraNvpDeviceVO.getUuid() + " found on physical network " + physnet.getId());

        checkThatLogicalSwitchExists(userSpecified, niciraNvpDeviceVO);

        s_logger.debug("Physical isolation type is supported, asking GuestNetworkGuru to design this network");
        final NetworkVO networkObject = (NetworkVO) super.design(offering, plan, userSpecified, owner);
        if (networkObject == null) {
            return null;
        }
        networkObject.setBroadcastDomainType(BroadcastDomainType.Lswitch);

        return networkObject;
    }

    private void checkThatLogicalSwitchExists(final Network userSpecified, final NiciraNvpDeviceVO niciraNvpDeviceVO) {
        final URI broadcastUri = userSpecified == null ? null : userSpecified.getBroadcastUri();
        if (broadcastUri != null) {
            final String lswitchUuid = broadcastUri.getRawSchemeSpecificPart();
            if (!lswitchExists(lswitchUuid, niciraNvpDeviceVO)) {
                throw new CloudRuntimeException("Refusing to design this network because the specified lswitch (" + lswitchUuid + ") does not exist.");
            }
        }
    }

    private boolean lswitchExists(final String lswitchUuid, final NiciraNvpDeviceVO niciraNvpDeviceVO) {
        try {
            final Answer answer = agentMgr.send(niciraNvpDeviceVO.getHostId(), new FindLogicalSwitchCommand(lswitchUuid));
            return answer.getResult();
        } catch (AgentUnavailableException | OperationTimedoutException e) {
            s_logger.warn("There was an error while trying to find logical switch " + lswitchUuid);
            return false;
        }
    }

    @Override
    public Network implement(final Network network, final NetworkOffering offering, final DeployDestination dest, final ReservationContext context)
            throws InsufficientVirtualNetworkCapacityException {
        assert network.getState() == Network.State.Implementing : "Why are we implementing " + network;

        final long zoneId = dest.getZone().getId();

        Long physicalNetworkId = network.getPhysicalNetworkId();

        // physical network id can be null in Guest Network in Basic zone, so locate the physical network
        if (physicalNetworkId == null) {
            physicalNetworkId = networkModel.findPhysicalNetworkId(zoneId, offering.getTags(), offering.getTrafficType());
        }

        final NetworkVO implemented = new NetworkVO(network.getTrafficType(), network.getMode(), network.getBroadcastDomainType(), network.getNetworkOfferingId(),
                Network.State.Allocated, network.getDataCenterId(), physicalNetworkId, offering.getRedundantRouter());

        if (network.getGateway() != null) {
            implemented.setGateway(network.getGateway());
        }

        if (network.getCidr() != null) {
            implemented.setCidr(network.getCidr());
        }

        // Name is either the given name or the uuid
        String name = network.getName();
        if (name == null || name.isEmpty()) {
            name = network.getUuid();
        }
        if (name.length() > MAX_NAME_LENGTH) {
            name = name.substring(0, MAX_NAME_LENGTH - 1);
        }

        final List<NiciraNvpDeviceVO> devices = niciraNvpDao.listByPhysicalNetwork(physicalNetworkId);
        if (devices.isEmpty()) {
            s_logger.error("No NiciraNvp Controller on physical network " + physicalNetworkId);
            return null;
        }
        final NiciraNvpDeviceVO niciraNvpDevice = devices.get(0);
        final HostVO niciraNvpHost = hostDao.findById(niciraNvpDevice.getHostId());
        hostDao.loadDetails(niciraNvpHost);
        final String transportzoneuuid = niciraNvpHost.getDetail("transportzoneuuid");
        final String transportzoneisotype = niciraNvpHost.getDetail("transportzoneisotype");

        final CreateLogicalSwitchCommand cmd =
                new CreateLogicalSwitchCommand(transportzoneuuid, transportzoneisotype, name, context.getDomain().getName() + "-" + context.getAccount()
                                                                                                                                           .getAccountName(), network.getId());
        final CreateLogicalSwitchAnswer answer = (CreateLogicalSwitchAnswer) agentMgr.easySend(niciraNvpHost.getId(), cmd);

        if (answer == null || !answer.getResult()) {
            s_logger.error("CreateLogicalSwitchCommand failed");
            return null;
        }

        try {
            implemented.setBroadcastUri(new URI("lswitch", answer.getLogicalSwitchUuid(), null));
            implemented.setBroadcastDomainType(BroadcastDomainType.Lswitch);
            s_logger.info("Implemented OK, network linked to  = " + implemented.getBroadcastUri().toString());
        } catch (final URISyntaxException e) {
            s_logger.error("Unable to store logical switch id in broadcast uri, uuid = " + implemented.getUuid(), e);
            return null;
        }

        return implemented;
    }

    @Override
    public void reserve(final NicProfile nic, final Network network, final VirtualMachineProfile vm, final DeployDestination dest, final ReservationContext context)
            throws InsufficientVirtualNetworkCapacityException, InsufficientAddressCapacityException {
        super.reserve(nic, network, vm, dest, context);
    }

    @Override
    public boolean release(final NicProfile nic, final VirtualMachineProfile vm, final String reservationId) {
        return super.release(nic, vm, reservationId);
    }

    @Override
    public void shutdown(final NetworkProfile profile, final NetworkOffering offering) {
        final NetworkVO networkObject = networkDao.findById(profile.getId());
        if (networkObject.getBroadcastDomainType() != BroadcastDomainType.Lswitch || networkObject.getBroadcastUri() == null) {
            s_logger.warn("BroadcastUri is empty or incorrect for guestnetwork " + networkObject.getDisplayText());
            return;
        }

        final List<NiciraNvpDeviceVO> devices = niciraNvpDao.listByPhysicalNetwork(networkObject.getPhysicalNetworkId());
        if (devices.isEmpty()) {
            s_logger.error("No NiciraNvp Controller on physical network " + networkObject.getPhysicalNetworkId());
            return;
        }
        final NiciraNvpDeviceVO niciraNvpDevice = devices.get(0);
        final HostVO niciraNvpHost = hostDao.findById(niciraNvpDevice.getHostId());

        final DeleteLogicalSwitchCommand cmd = new DeleteLogicalSwitchCommand(BroadcastDomainType.getValue(networkObject.getBroadcastUri()));
        final DeleteLogicalSwitchAnswer answer = (DeleteLogicalSwitchAnswer) agentMgr.easySend(niciraNvpHost.getId(), cmd);

        if (answer == null || !answer.getResult()) {
            s_logger.error("DeleteLogicalSwitchCommand failed");
        }

        super.shutdown(profile, offering);
    }

    @Override
    public boolean trash(final Network network, final NetworkOffering offering) {
        return super.trash(network, offering);
    }
}
