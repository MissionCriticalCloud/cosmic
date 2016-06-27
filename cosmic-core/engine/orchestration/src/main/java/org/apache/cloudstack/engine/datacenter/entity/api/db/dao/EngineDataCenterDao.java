package org.apache.cloudstack.engine.datacenter.entity.api.db.dao;

import com.cloud.utils.db.GenericDao;
import com.cloud.utils.fsm.StateDao;
import org.apache.cloudstack.engine.datacenter.entity.api.DataCenterResourceEntity;
import org.apache.cloudstack.engine.datacenter.entity.api.db.EngineDataCenterVO;

import java.util.List;

public interface EngineDataCenterDao extends GenericDao<EngineDataCenterVO, Long>,
        StateDao<DataCenterResourceEntity.State, DataCenterResourceEntity.State.Event, DataCenterResourceEntity> {
    EngineDataCenterVO findByName(String name);

    /**
     * @param id data center id
     * @return a pair of mac address strings.  The first one is private and second is public.
     */
    String[] getNextAvailableMacAddressPair(long id);

    String[] getNextAvailableMacAddressPair(long id, long mask);

    List<EngineDataCenterVO> findZonesByDomainId(Long domainId);

    List<EngineDataCenterVO> listPublicZones(String keyword);

    List<EngineDataCenterVO> findChildZones(Object[] ids, String keyword);

    void loadDetails(EngineDataCenterVO zone);

    void saveDetails(EngineDataCenterVO zone);

    List<EngineDataCenterVO> listDisabledZones();

    List<EngineDataCenterVO> listEnabledZones();

    EngineDataCenterVO findByToken(String zoneToken);

    EngineDataCenterVO findByTokenOrIdOrName(String tokenIdOrName);

    List<EngineDataCenterVO> findZonesByDomainId(Long domainId, String keyword);

    List<EngineDataCenterVO> findByKeyword(String keyword);
}
