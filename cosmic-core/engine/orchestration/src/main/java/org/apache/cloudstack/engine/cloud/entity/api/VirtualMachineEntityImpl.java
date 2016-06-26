package org.apache.cloudstack.engine.cloud.entity.api;

import com.cloud.deploy.DeploymentPlan;
import com.cloud.deploy.DeploymentPlanner;
import com.cloud.deploy.DeploymentPlanner.ExcludeList;
import com.cloud.exception.AgentUnavailableException;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.OperationTimedoutException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.vm.VirtualMachineProfile;
import org.apache.cloudstack.engine.cloud.entity.api.db.VMEntityVO;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class VirtualMachineEntityImpl implements VirtualMachineEntity {

    @Inject
    private VMEntityManager manager;

    private VMEntityVO vmEntityVO;

    public VirtualMachineEntityImpl() {
    }

    public VirtualMachineEntityImpl(final String vmId, final String owner, final String hostName, final String displayName, final int cpu, final int speed, final long memory,
                                    final List<String> computeTags,
                                    final List<String> rootDiskTags, final List<String> networks, final VMEntityManager manager) {
        this(vmId, manager);
        this.vmEntityVO.setOwner(owner);
        this.vmEntityVO.setHostname(hostName);
        this.vmEntityVO.setDisplayname(displayName);
        this.vmEntityVO.setComputeTags(computeTags);
        this.vmEntityVO.setRootDiskTags(rootDiskTags);
        this.vmEntityVO.setNetworkIds(networks);

        manager.saveVirtualMachine(vmEntityVO);
    }

    public VirtualMachineEntityImpl(final String vmId, final VMEntityManager manager) {
        this.manager = manager;
        this.vmEntityVO = this.manager.loadVirtualMachine(vmId);
    }

    public void init(final String vmId, final String owner, final String hostName, final String displayName, final int cpu, final int speed, final long memory, final
    List<String> computeTags,
                     final List<String> rootDiskTags, final List<String> networks) {
        init(vmId);
        this.vmEntityVO.setOwner(owner);
        this.vmEntityVO.setHostname(hostName);
        this.vmEntityVO.setDisplayname(displayName);
        this.vmEntityVO.setComputeTags(computeTags);
        this.vmEntityVO.setRootDiskTags(rootDiskTags);
        this.vmEntityVO.setNetworkIds(networks);

        manager.saveVirtualMachine(vmEntityVO);
    }

    public void init(final String vmId) {
        this.vmEntityVO = this.manager.loadVirtualMachine(vmId);
    }

    @Override
    public String getUuid() {
        return vmEntityVO.getUuid();
    }

    @Override
    public long getId() {
        return vmEntityVO.getId();
    }

    @Override
    public String getCurrentState() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDesiredState() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Date getCreatedTime() {
        return vmEntityVO.getCreated();
    }

    @Override
    public Date getLastUpdatedTime() {
        return vmEntityVO.getUpdateTime();
    }

    @Override
    public String getOwner() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, String> getDetails() {
        return vmEntityVO.getDetails();
    }

    @Override
    public void addDetail(final String name, final String value) {
        vmEntityVO.setDetail(name, value);
    }

    @Override
    public void delDetail(final String name, final String value) {
        // TODO Auto-generated method stub
    }

    @Override
    public void updateDetail(final String name, final String value) {
        // TODO Auto-generated method stub
    }

    @Override
    public List<Method> getApplicableActions() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> listVolumeIds() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<VolumeEntity> listVolumes() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> listNicUuids() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<NicEntity> listNics() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TemplateEntity getTemplate() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> listTags() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addTag() {
        // TODO Auto-generated method stub

    }

    @Override
    public void delTag() {
        // TODO Auto-generated method stub

    }

    @Override
    public String reserve(final DeploymentPlanner plannerToUse, final DeploymentPlan plan, final ExcludeList exclude, final String caller) throws InsufficientCapacityException,
            ResourceUnavailableException {
        return manager.reserveVirtualMachine(this.vmEntityVO, plannerToUse, plan, exclude);
    }

    @Override
    public void migrateTo(final String reservationId, final String caller) {
        // TODO Auto-generated method stub

    }

    @Override
    public void deploy(final String reservationId, final String caller, final Map<VirtualMachineProfile.Param, Object> params) throws InsufficientCapacityException,
            ResourceUnavailableException {
        manager.deployVirtualMachine(reservationId, this.vmEntityVO, caller, params);
    }

    @Override
    public boolean stop(final String caller) throws ResourceUnavailableException {
        return manager.stopvirtualmachine(this.vmEntityVO, caller);
    }

    @Override
    public void cleanup() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean destroy(final String caller) throws AgentUnavailableException, OperationTimedoutException, ConcurrentOperationException {
        return manager.destroyVirtualMachine(this.vmEntityVO, caller);
    }

    @Override
    public VirtualMachineEntity duplicate(final String externalId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SnapshotEntity takeSnapshotOf() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void attach(final VolumeEntity volume, final short deviceId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void detach(final VolumeEntity volume) {
        // TODO Auto-generated method stub

    }

    @Override
    public void connectTo(final NetworkEntity network, final short nicId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void disconnectFrom(final NetworkEntity netowrk, final short nicId) {
        // TODO Auto-generated method stub

    }
}
