package com.cloud.cluster.dao;

import com.cloud.cluster.ManagementServerHost;
import com.cloud.cluster.ManagementServerHost.State;
import com.cloud.cluster.ManagementServerHostVO;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GenericDao;

import java.util.Date;
import java.util.List;

public interface ManagementServerHostDao extends GenericDao<ManagementServerHostVO, Long> {
    @Override
    boolean remove(Long id);

    ManagementServerHostVO findByMsid(long msid);

    int increaseAlertCount(long id);

    void update(long id, long runid, String name, String version, String serviceIP, int servicePort, Date lastUpdate);

    void update(long id, long runid, Date lastUpdate);

    List<ManagementServerHostVO> getActiveList(Date cutTime);

    List<ManagementServerHostVO> getInactiveList(Date cutTime);

    void invalidateRunSession(long id, long runid);

    void update(long id, long runId, State state, Date lastUpdate);

    List<ManagementServerHostVO> listBy(ManagementServerHost.State... states);

    public List<Long> listOrphanMsids();

    ManagementServerHostVO findOneInUpState(Filter filter);
}
