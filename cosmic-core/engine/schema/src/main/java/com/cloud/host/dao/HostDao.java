package com.cloud.host.dao;

import com.cloud.host.HostVO;
import com.cloud.info.RunningHostCountInfo;
import com.cloud.legacymodel.dc.Host;
import com.cloud.legacymodel.dc.HostStatus;
import com.cloud.legacymodel.resource.ResourceState;
import com.cloud.model.enumeration.Event;
import com.cloud.model.enumeration.HostType;
import com.cloud.utils.db.GenericDao;
import com.cloud.utils.fsm.StateDao;

import java.util.Date;
import java.util.List;

/**
 * Data Access Object for server
 */
public interface HostDao extends GenericDao<HostVO, Long>, StateDao<HostStatus, Event, Host> {
    long countBy(long clusterId, ResourceState... states);

    /**
     * Mark all hosts associated with a certain management server
     * as disconnected.
     *
     * @param msId management server id.
     */
    void markHostsAsDisconnected(long msId, long lastPing);

    List<HostVO> findLostHosts(long timeout);

    List<HostVO> findAndUpdateDirectAgentToLoad(long lastPingSecondsAfter, Long limit, long managementServerId);

    List<RunningHostCountInfo> getRunningHostCounts(Date cutTime);

    long getNextSequence(long hostId);

    void loadDetails(HostVO host);

    void saveDetails(HostVO host);

    void loadHostTags(HostVO host);

    List<HostVO> listByHostTag(HostType type, Long clusterId, Long podId, long dcId, String hostTag);

    long countRoutingHostsByDataCenter(long dcId);

    List<HostVO> findAndUpdateApplianceToLoad(long lastPingSecondsAfter, long managementServerId);

    boolean updateResourceState(ResourceState oldState, ResourceState.Event event, ResourceState newState, Host vo);

    HostVO findByGuid(String guid);

    HostVO findByTypeNameAndZoneId(long zoneId, String name, HostType type);

    List<HostVO> findHypervisorHostInCluster(long clusterId);

    /**
     * @param type
     * @param clusterId
     * @param podId
     * @param dcId
     * @param haTag     TODO
     * @return
     */
    List<HostVO> listAllUpAndEnabledNonHAHosts(HostType type, Long clusterId, Long podId, long dcId, String haTag);

    List<HostVO> findByPodId(Long podId);

    List<HostVO> findByClusterId(Long clusterId);

    List<HostVO> listByDataCenterId(long id);

    List<Long> listAllHosts(long zoneId);

    HostVO findByPublicIp(String publicIp);

    List<HostVO> listByType(HostType type);
}
