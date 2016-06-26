package org.apache.cloudstack.engine.datacenter.entity.api.db.dao;

import com.cloud.host.Host;
import com.cloud.utils.db.GenericDao;
import com.cloud.utils.fsm.StateDao;
import org.apache.cloudstack.engine.datacenter.entity.api.DataCenterResourceEntity;
import org.apache.cloudstack.engine.datacenter.entity.api.db.EngineHostVO;

import java.util.List;

/**
 * Data Access Object for server
 */
public interface EngineHostDao extends GenericDao<EngineHostVO, Long>,
        StateDao<DataCenterResourceEntity.State, DataCenterResourceEntity.State.Event, DataCenterResourceEntity> {

    void loadDetails(EngineHostVO host);

    void saveDetails(EngineHostVO host);

    void loadHostTags(EngineHostVO host);

    List<EngineHostVO> listByHostTag(Host.Type type, Long clusterId, Long podId, long dcId, String hostTag);
}
