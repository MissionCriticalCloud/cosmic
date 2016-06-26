package org.apache.cloudstack.engine.datacenter.entity.api;

import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.org.Cluster.ClusterType;
import com.cloud.org.Grouping.AllocationState;
import com.cloud.org.Managed.ManagedState;
import com.cloud.utils.fsm.NoTransitionException;
import org.apache.cloudstack.engine.datacenter.entity.api.DataCenterResourceEntity.State.Event;
import org.apache.cloudstack.engine.datacenter.entity.api.db.EngineClusterVO;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ClusterEntityImpl implements ClusterEntity {

    private final DataCenterResourceManager manager;

    private final EngineClusterVO clusterVO;

    public ClusterEntityImpl(final String clusterId, final DataCenterResourceManager manager) {
        this.manager = manager;
        this.clusterVO = this.manager.loadCluster(clusterId);
    }

    @Override
    public boolean enable() {
        try {
            manager.changeState(this, Event.EnableRequest);
        } catch (final NoTransitionException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean disable() {
        try {
            manager.changeState(this, Event.DisableRequest);
        } catch (final NoTransitionException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean deactivate() {
        try {
            manager.changeState(this, Event.DeactivateRequest);
        } catch (final NoTransitionException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean reactivate() {
        try {
            manager.changeState(this, Event.ActivatedRequest);
        } catch (final NoTransitionException e) {
            return false;
        }
        return true;
    }

    @Override
    public State getState() {
        return clusterVO.getState();
    }

    @Override
    public void persist() {
        manager.saveCluster(clusterVO);
    }

    @Override
    public String getName() {
        return clusterVO.getName();
    }

    public void setName(final String name) {
        clusterVO.setName(name);
    }

    @Override
    public String getUuid() {
        return clusterVO.getUuid();
    }

    @Override
    public long getId() {
        return clusterVO.getId();
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
        return clusterVO.getCreated();
    }

    @Override
    public Date getLastUpdatedTime() {
        return clusterVO.getLastUpdated();
    }

    @Override
    public String getOwner() {
        return clusterVO.getOwner();
    }

    @Override
    public Map<String, String> getDetails() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addDetail(final String name, final String value) {
        // TODO Auto-generated method stub

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

    public void setOwner(final String owner) {
        clusterVO.setOwner(owner);
    }

    @Override
    public long getDataCenterId() {
        return clusterVO.getDataCenterId();
    }

    @Override
    public long getPodId() {
        return clusterVO.getPodId();
    }

    @Override
    public HypervisorType getHypervisorType() {
        return clusterVO.getHypervisorType();
    }

    @Override
    public ClusterType getClusterType() {
        return clusterVO.getClusterType();
    }

    @Override
    public AllocationState getAllocationState() {
        return clusterVO.getAllocationState();
    }

    @Override
    public ManagedState getManagedState() {
        return clusterVO.getManagedState();
    }
}
