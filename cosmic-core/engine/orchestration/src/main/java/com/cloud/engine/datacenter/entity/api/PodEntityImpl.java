package com.cloud.engine.datacenter.entity.api;

import com.cloud.engine.datacenter.entity.api.DataCenterResourceEntity.State.Event;
import com.cloud.engine.datacenter.entity.api.db.EngineHostPodVO;
import com.cloud.model.enumeration.AllocationState;
import com.cloud.utils.fsm.NoTransitionException;

import java.util.Date;
import java.util.Map;

public class PodEntityImpl implements PodEntity {

    private final DataCenterResourceManager manager;

    private final EngineHostPodVO podVO;

    public PodEntityImpl(final String uuid, final DataCenterResourceManager manager) {
        this.manager = manager;
        podVO = manager.loadPod(uuid);
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
        return podVO.getState();
    }

    @Override
    public void persist() {
        manager.savePod(podVO);
    }

    @Override
    public String getName() {
        return podVO.getName();
    }

    public void setName(final String name) {
        podVO.setName(name);
    }

    @Override
    public String getUuid() {
        return podVO.getUuid();
    }

    @Override
    public long getId() {
        return podVO.getId();
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
        return podVO.getCreated();
    }

    @Override
    public Date getLastUpdatedTime() {
        return podVO.getLastUpdated();
    }

    @Override
    public String getOwner() {
        return podVO.getOwner();
    }

    @Override
    public Map<String, String> getDetails() {
        return null;
    }

    @Override
    public void addDetail(final String name, final String value) {
        // TODO Auto-generated method stub

    }

    public void setOwner(final String owner) {
        podVO.setOwner(owner);
    }

    @Override
    public String getCidrAddress() {
        return podVO.getCidrAddress();
    }

    @Override
    public int getCidrSize() {
        return podVO.getCidrSize();
    }

    @Override
    public String getGateway() {
        return podVO.getGateway();
    }

    @Override
    public long getDataCenterId() {
        return podVO.getDataCenterId();
    }

    @Override
    public AllocationState getAllocationState() {
        return podVO.getAllocationState();
    }
}
