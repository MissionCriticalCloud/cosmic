package com.cloud.network.element;

import com.cloud.agent.AgentManager;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.routing.SavePasswordCommand;
import com.cloud.agent.api.routing.VmDataCommand;
import com.cloud.agent.manager.Commands;
import com.cloud.configuration.ConfigurationManager;
import com.cloud.configuration.ZoneConfig;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.deploy.DeployDestination;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.IllegalVirtualMachineException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.OperationTimedoutException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.Network;
import com.cloud.network.Network.Capability;
import com.cloud.network.Network.Provider;
import com.cloud.network.Network.Service;
import com.cloud.network.NetworkModel;
import com.cloud.network.Networks.TrafficType;
import com.cloud.network.PhysicalNetworkServiceProvider;
import com.cloud.network.dao.NetworkDao;
import com.cloud.offering.NetworkOffering;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.utils.component.AdapterBase;
import com.cloud.vm.NicProfile;
import com.cloud.vm.ReservationContext;
import com.cloud.vm.UserVmManager;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachineProfile;
import com.cloud.vm.dao.DomainRouterDao;
import com.cloud.vm.dao.UserVmDao;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloudZonesNetworkElement extends AdapterBase implements NetworkElement, UserDataServiceProvider {
    private static final Logger s_logger = LoggerFactory.getLogger(CloudZonesNetworkElement.class);

    private static final Map<Service, Map<Capability, String>> capabilities = setCapabilities();

    @Inject
    NetworkDao _networkConfigDao;
    @Inject
    NetworkModel _networkMgr;
    @Inject
    UserVmManager _userVmMgr;
    @Inject
    UserVmDao _userVmDao;
    @Inject
    DomainRouterDao _routerDao;
    @Inject
    ConfigurationManager _configMgr;
    @Inject
    DataCenterDao _dcDao;
    @Inject
    AgentManager _agentManager;
    @Inject
    ServiceOfferingDao _serviceOfferingDao;

    private static Map<Service, Map<Capability, String>> setCapabilities() {
        final Map<Service, Map<Capability, String>> capabilities = new HashMap<>();

        capabilities.put(Service.UserData, null);

        return capabilities;
    }

    @Override
    public Map<Service, Map<Capability, String>> getCapabilities() {
        return capabilities;
    }

    @Override
    public Provider getProvider() {
        return Provider.ExternalDhcpServer;
    }

    @Override
    public boolean implement(final Network network, final NetworkOffering offering, final DeployDestination dest, final ReservationContext context) throws
            ResourceUnavailableException,
            ConcurrentOperationException, InsufficientCapacityException {
        if (!canHandle(dest, offering.getTrafficType())) {
            return false;
        }

        return true;
    }

    private boolean canHandle(final DeployDestination dest, final TrafficType trafficType) {
        final DataCenterVO dc = (DataCenterVO) dest.getDataCenter();

        if (dc.getDhcpProvider().equalsIgnoreCase(Provider.ExternalDhcpServer.getName())) {
            _dcDao.loadDetails(dc);
            final String dhcpStrategy = dc.getDetail(ZoneConfig.DhcpStrategy.key());
            if ("external".equalsIgnoreCase(dhcpStrategy)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean prepare(final Network network, final NicProfile nic, final VirtualMachineProfile vmProfile, final DeployDestination dest, final ReservationContext context)
            throws ConcurrentOperationException, InsufficientCapacityException, ResourceUnavailableException, IllegalVirtualMachineException {
        return true;
    }

    @Override
    public boolean release(final Network network, final NicProfile nic, final VirtualMachineProfile vm, final ReservationContext context) {
        return true;
    }

    @Override
    public boolean shutdown(final Network network, final ReservationContext context, final boolean cleanup) throws ConcurrentOperationException, ResourceUnavailableException {
        return false; // assume that the agent will remove userdata etc
    }

    @Override
    public boolean destroy(final Network config, final ReservationContext context) throws ConcurrentOperationException, ResourceUnavailableException {
        return false; // assume that the agent will remove userdata etc
    }

    @Override
    public boolean isReady(final PhysicalNetworkServiceProvider provider) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean shutdownProviderInstances(final PhysicalNetworkServiceProvider provider, final ReservationContext context) throws ConcurrentOperationException,
            ResourceUnavailableException {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean canEnableIndividualServices() {
        return false;
    }

    @Override
    public boolean verifyServicesCombination(final Set<Service> services) {
        return true;
    }

    @Override
    public boolean addPasswordAndUserdata(final Network network, final NicProfile nic, final VirtualMachineProfile vm, final DeployDestination dest, final ReservationContext
            context)
            throws ConcurrentOperationException, InsufficientCapacityException, ResourceUnavailableException {
        if (canHandle(dest, network.getTrafficType())) {

            if (vm.getType() != VirtualMachine.Type.User) {
                return false;
            }
            final
            UserVmVO uservm = _userVmDao.findById(vm.getId());
            _userVmDao.loadDetails(uservm);
            final String password = (String) vm.getParameter(VirtualMachineProfile.Param.VmPassword);
            final String userData = uservm.getUserData();
            final String sshPublicKey = uservm.getDetail("SSH.PublicKey");

            final Commands cmds = new Commands(Command.OnError.Continue);
            if (password != null && nic.isDefaultNic()) {
                final SavePasswordCommand cmd = new SavePasswordCommand(password, nic.getIPv4Address(), uservm.getHostName(), _networkMgr.getExecuteInSeqNtwkElmtCmd());
                cmds.addCommand("password", cmd);
            }
            final String serviceOffering = _serviceOfferingDao.findByIdIncludingRemoved(uservm.getServiceOfferingId()).getDisplayText();
            final String zoneName = _dcDao.findById(network.getDataCenterId()).getName();

            cmds.addCommand(
                    "vmdata",
                    generateVmDataCommand(nic.getIPv4Address(), userData, serviceOffering, zoneName, nic.getIPv4Address(), uservm.getHostName(), uservm.getInstanceName(),
                            uservm.getId(), uservm.getUuid(), sshPublicKey));
            try {
                _agentManager.send(dest.getHost().getId(), cmds);
            } catch (final OperationTimedoutException e) {
                s_logger.debug("Unable to send vm data command to host " + dest.getHost());
                return false;
            }
            final Answer dataAnswer = cmds.getAnswer("vmdata");
            if (dataAnswer != null && dataAnswer.getResult()) {
                s_logger.info("Sent vm data successfully to vm " + uservm.getInstanceName());
                return true;
            }
            s_logger.info("Failed to send vm data to vm " + uservm.getInstanceName());
            return false;
        }
        return false;
    }

    private VmDataCommand generateVmDataCommand(final String vmPrivateIpAddress, final String userData, final String serviceOffering, final String zoneName, final String
            guestIpAddress,
                                                final String vmName, final String vmInstanceName, final long vmId, final String vmUuid, final String publicKey) {
        final VmDataCommand cmd = new VmDataCommand(vmPrivateIpAddress, vmName, _networkMgr.getExecuteInSeqNtwkElmtCmd());
        // if you add new metadata files, also edit systemvm/patches/debian/config/var/www/html/latest/.htaccess
        cmd.addVmData("userdata", "user-data", userData);
        cmd.addVmData("metadata", "service-offering", serviceOffering);
        cmd.addVmData("metadata", "availability-zone", zoneName);
        cmd.addVmData("metadata", "local-ipv4", guestIpAddress);
        cmd.addVmData("metadata", "local-hostname", vmName);
        cmd.addVmData("metadata", "public-ipv4", guestIpAddress);
        cmd.addVmData("metadata", "public-hostname", guestIpAddress);
        if (vmUuid == null) {
            setVmInstanceId(vmInstanceName, vmId, cmd);
        } else {
            setVmInstanceId(vmUuid, cmd);
        }
        cmd.addVmData("metadata", "public-keys", publicKey);

        return cmd;
    }

    private void setVmInstanceId(final String vmInstanceName, final long vmId, final VmDataCommand cmd) {
        cmd.addVmData("metadata", "instance-id", vmInstanceName);
        cmd.addVmData("metadata", "vm-id", String.valueOf(vmId));
    }

    private void setVmInstanceId(final String vmUuid, final VmDataCommand cmd) {
        cmd.addVmData("metadata", "instance-id", vmUuid);
        cmd.addVmData("metadata", "vm-id", vmUuid);
    }

    @Override
    public boolean savePassword(final Network network, final NicProfile nic, final VirtualMachineProfile vm) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean saveUserData(final Network network, final NicProfile nic, final VirtualMachineProfile vm) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean saveSSHKey(final Network network, final NicProfile nic, final VirtualMachineProfile vm, final String sshPublicKey) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }
}
