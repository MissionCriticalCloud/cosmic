package com.cloud.gpu.dao;

import com.cloud.gpu.HostGpuGroupsVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HostGpuGroupsDaoImpl extends GenericDaoBase<HostGpuGroupsVO, Long> implements HostGpuGroupsDao {
    private static final Logger s_logger = LoggerFactory.getLogger(HostGpuGroupsDaoImpl.class);

    private final SearchBuilder<HostGpuGroupsVO> _hostIdGroupNameSearch;
    private final SearchBuilder<HostGpuGroupsVO> _searchByHostId;
    private final GenericSearchBuilder<HostGpuGroupsVO, Long> _searchHostIds;

    public HostGpuGroupsDaoImpl() {

        _hostIdGroupNameSearch = createSearchBuilder();
        _hostIdGroupNameSearch.and("hostId", _hostIdGroupNameSearch.entity().getHostId(), SearchCriteria.Op.EQ);
        _hostIdGroupNameSearch.and("groupName", _hostIdGroupNameSearch.entity().getGroupName(), SearchCriteria.Op.EQ);
        _hostIdGroupNameSearch.done();

        _searchByHostId = createSearchBuilder();
        _searchByHostId.and("hostId", _searchByHostId.entity().getHostId(), SearchCriteria.Op.EQ);
        _searchByHostId.done();

        _searchHostIds = createSearchBuilder(Long.class);
        _searchHostIds.selectFields(_searchHostIds.entity().getHostId());
        _searchHostIds.done();
    }

    @Override
    public HostGpuGroupsVO findByHostIdGroupName(final long hostId, final String groupName) {
        final SearchCriteria<HostGpuGroupsVO> sc = _hostIdGroupNameSearch.create();
        sc.setParameters("hostId", hostId);
        sc.setParameters("groupName", groupName);
        return findOneBy(sc);
    }

    @Override
    public List<Long> listHostIds() {
        final SearchCriteria<Long> sc = _searchHostIds.create();
        return customSearch(sc, null);
    }

    @Override
    public List<HostGpuGroupsVO> listByHostId(final long hostId) {
        final SearchCriteria<HostGpuGroupsVO> sc = _searchByHostId.create();
        sc.setParameters("hostId", hostId);
        return listBy(sc);
    }

    @Override
    public void persist(final long hostId, final List<String> gpuGroups) {
        for (final String groupName : gpuGroups) {
            if (findByHostIdGroupName(hostId, groupName) == null) {
                final HostGpuGroupsVO record = new HostGpuGroupsVO(hostId, groupName);
                persist(record);
            }
        }
    }

    @Override
    public void deleteGpuEntries(final long hostId) {
        final SearchCriteria<HostGpuGroupsVO> sc = _searchByHostId.create();
        sc.setParameters("hostId", hostId);
        remove(sc);
    }
}
