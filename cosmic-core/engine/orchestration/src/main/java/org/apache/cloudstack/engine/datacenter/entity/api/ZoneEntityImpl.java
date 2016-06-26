package org.apache.cloudstack.engine.datacenter.entity.api;

import com.cloud.utils.fsm.FiniteStateObject;
import com.cloud.utils.fsm.NoTransitionException;
import org.apache.cloudstack.engine.datacenter.entity.api.DataCenterResourceEntity.State.Event;
import org.apache.cloudstack.engine.datacenter.entity.api.db.EngineDataCenterVO;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Path("/zone/{id}")
public class ZoneEntityImpl implements ZoneEntity, FiniteStateObject<DataCenterResourceEntity.State, DataCenterResourceEntity.State.Event> {

    private final DataCenterResourceManager manager;

    private final EngineDataCenterVO dataCenterVO;

    public ZoneEntityImpl(final String dataCenterId, final DataCenterResourceManager manager) {
        this.manager = manager;
        this.dataCenterVO = this.manager.loadDataCenter(dataCenterId);
    }

    @Override
    @GET
    public String getUuid() {
        return dataCenterVO.getUuid();
    }

    @Override
    public long getId() {
        return dataCenterVO.getId();
    }

    @Override
    public String getCurrentState() {
        // TODO Auto-generated method stub
        return "state";
    }

    @Override
    public String getDesiredState() {
        // TODO Auto-generated method stub
        return "desired_state";
    }

    @Override
    public Date getCreatedTime() {
        return dataCenterVO.getCreated();
    }

    @Override
    public Date getLastUpdatedTime() {
        return dataCenterVO.getLastUpdated();
    }

    @Override
    public String getOwner() {
        return dataCenterVO.getOwner();
    }

    public void setOwner(final String owner) {
        dataCenterVO.setOwner(owner);
    }

    @Override
    public Map<String, String> getDetails() {
        return dataCenterVO.getDetails();
    }

    public void setDetails(final Map<String, String> details) {
        dataCenterVO.setDetails(details);
    }

    @Override
    public void addDetail(final String name, final String value) {
        dataCenterVO.setDetail(name, value);
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
        return dataCenterVO.getState();
    }

    @Override
    public void setState(final State state) {
        //use FSM to set state.
    }

    @Override
    public void persist() {
        manager.saveDataCenter(dataCenterVO);
    }

    @Override
    public String getName() {
        return dataCenterVO.getName();
    }

    public void setName(final String name) {
        dataCenterVO.setName(name);
    }

    @Override
    public List<PodEntity> listPods() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> listPodIds() {
        final List<String> podIds = new ArrayList<>();
        podIds.add("pod-uuid-1");
        podIds.add("pod-uuid-2");
        return podIds;
    }
}
