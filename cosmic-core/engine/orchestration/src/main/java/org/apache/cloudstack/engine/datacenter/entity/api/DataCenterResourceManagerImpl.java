package org.apache.cloudstack.engine.datacenter.entity.api;

import com.cloud.exception.InvalidParameterValueException;
import com.cloud.utils.fsm.NoTransitionException;
import com.cloud.utils.fsm.StateMachine2;
import org.apache.cloudstack.engine.datacenter.entity.api.DataCenterResourceEntity.State;
import org.apache.cloudstack.engine.datacenter.entity.api.DataCenterResourceEntity.State.Event;
import org.apache.cloudstack.engine.datacenter.entity.api.db.EngineClusterVO;
import org.apache.cloudstack.engine.datacenter.entity.api.db.EngineDataCenterVO;
import org.apache.cloudstack.engine.datacenter.entity.api.db.EngineHostPodVO;
import org.apache.cloudstack.engine.datacenter.entity.api.db.EngineHostVO;
import org.apache.cloudstack.engine.datacenter.entity.api.db.dao.EngineClusterDao;
import org.apache.cloudstack.engine.datacenter.entity.api.db.dao.EngineDataCenterDao;
import org.apache.cloudstack.engine.datacenter.entity.api.db.dao.EngineHostDao;
import org.apache.cloudstack.engine.datacenter.entity.api.db.dao.EngineHostPodDao;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

@Component
public class DataCenterResourceManagerImpl implements DataCenterResourceManager {

    protected StateMachine2<State, Event, DataCenterResourceEntity> _stateMachine = DataCenterResourceEntity.State.s_fsm;
    @Inject
    EngineDataCenterDao _dataCenterDao;
    @Inject
    EngineHostPodDao _podDao;
    @Inject
    EngineClusterDao _clusterDao;
    @Inject
    EngineHostDao _hostDao;

    @Override
    public EngineDataCenterVO loadDataCenter(final String dataCenterId) {
        final EngineDataCenterVO dataCenterVO = _dataCenterDao.findByUuid(dataCenterId);
        if (dataCenterVO == null) {
            throw new InvalidParameterValueException("Zone does not exist");
        }
        return dataCenterVO;
    }

    @Override
    public void saveDataCenter(final EngineDataCenterVO dc) {
        _dataCenterDao.persist(dc);
    }

    @Override
    public void savePod(final EngineHostPodVO pod) {
        _podDao.persist(pod);
    }

    @Override
    public void saveCluster(final EngineClusterVO cluster) {
        _clusterDao.persist(cluster);
    }

    @Override
    public boolean changeState(final DataCenterResourceEntity entity, final Event event) throws NoTransitionException {

        if (entity instanceof ZoneEntity) {
            return _stateMachine.transitTo(entity, event, null, _dataCenterDao);
        } else if (entity instanceof PodEntity) {
            return _stateMachine.transitTo(entity, event, null, _podDao);
        } else if (entity instanceof ClusterEntity) {
            return _stateMachine.transitTo(entity, event, null, _clusterDao);
        } else if (entity instanceof HostEntity) {
            return _stateMachine.transitTo(entity, event, null, _hostDao);
        }

        return false;
    }

    @Override
    public EngineHostPodVO loadPod(final String uuid) {
        final EngineHostPodVO pod = _podDao.findByUuid(uuid);
        if (pod == null) {
            throw new InvalidParameterValueException("Pod does not exist");
        }
        return pod;
    }

    @Override
    public EngineClusterVO loadCluster(final String uuid) {
        final EngineClusterVO cluster = _clusterDao.findByUuid(uuid);
        if (cluster == null) {
            throw new InvalidParameterValueException("Pod does not exist");
        }
        return cluster;
    }

    @Override
    public EngineHostVO loadHost(final String uuid) {
        final EngineHostVO host = _hostDao.findByUuid(uuid);
        if (host == null) {
            throw new InvalidParameterValueException("Host does not exist");
        }
        return host;
    }

    @Override
    public void saveHost(final EngineHostVO hostVO) {
        _hostDao.persist(hostVO);
    }
}
