package com.cloud.hypervisor.dao;

import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.hypervisor.HypervisorCapabilitiesVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HypervisorCapabilitiesDaoImpl extends GenericDaoBase<HypervisorCapabilitiesVO, Long> implements HypervisorCapabilitiesDao {

    private static final Logger s_logger = LoggerFactory.getLogger(HypervisorCapabilitiesDaoImpl.class);
    private static final String DEFAULT_VERSION = "default";
    protected final SearchBuilder<HypervisorCapabilitiesVO> HypervisorTypeSearch;
    protected final SearchBuilder<HypervisorCapabilitiesVO> HypervisorTypeAndVersionSearch;

    protected HypervisorCapabilitiesDaoImpl() {
        HypervisorTypeSearch = createSearchBuilder();
        HypervisorTypeSearch.and("hypervisorType", HypervisorTypeSearch.entity().getHypervisorType(), SearchCriteria.Op.EQ);
        HypervisorTypeSearch.done();

        HypervisorTypeAndVersionSearch = createSearchBuilder();
        HypervisorTypeAndVersionSearch.and("hypervisorType", HypervisorTypeAndVersionSearch.entity().getHypervisorType(), SearchCriteria.Op.EQ);
        HypervisorTypeAndVersionSearch.and("hypervisorVersion", HypervisorTypeAndVersionSearch.entity().getHypervisorVersion(), SearchCriteria.Op.EQ);
        HypervisorTypeAndVersionSearch.done();
    }

    @Override
    public List<HypervisorCapabilitiesVO> listAllByHypervisorType(final HypervisorType hypervisorType) {
        final SearchCriteria<HypervisorCapabilitiesVO> sc = HypervisorTypeSearch.create();
        sc.setParameters("hypervisorType", hypervisorType);
        return search(sc, null);
    }

    @Override
    public HypervisorCapabilitiesVO findByHypervisorTypeAndVersion(final HypervisorType hypervisorType, final String hypervisorVersion) {
        final SearchCriteria<HypervisorCapabilitiesVO> sc = HypervisorTypeAndVersionSearch.create();
        sc.setParameters("hypervisorType", hypervisorType);
        sc.setParameters("hypervisorVersion", hypervisorVersion);
        return findOneBy(sc);
    }

    @Override
    public Long getMaxGuestsLimit(final HypervisorType hypervisorType, final String hypervisorVersion) {
        final Long defaultLimit = new Long(50);
        final HypervisorCapabilitiesVO result = getCapabilities(hypervisorType, hypervisorVersion);
        if (result == null) {
            return defaultLimit;
        }
        final Long limit = result.getMaxGuestsLimit();
        if (limit == null) {
            return defaultLimit;
        }
        return limit;
    }

    HypervisorCapabilitiesVO getCapabilities(final HypervisorType hypervisorType, final String hypervisorVersion) {
        HypervisorCapabilitiesVO result = findByHypervisorTypeAndVersion(hypervisorType, hypervisorVersion);
        if (result == null) { // if data is not available for a specific version then use 'default' as version
            result = findByHypervisorTypeAndVersion(hypervisorType, DEFAULT_VERSION);
        }
        return result;
    }

    @Override
    public Integer getMaxDataVolumesLimit(final HypervisorType hypervisorType, final String hypervisorVersion) {
        final HypervisorCapabilitiesVO result = getCapabilities(hypervisorType, hypervisorVersion);
        return result.getMaxDataVolumesLimit();
    }

    @Override
    public Integer getMaxHostsPerCluster(final HypervisorType hypervisorType, final String hypervisorVersion) {
        final HypervisorCapabilitiesVO result = getCapabilities(hypervisorType, hypervisorVersion);
        return result.getMaxHostsPerCluster();
    }

    @Override
    public Boolean isVmSnapshotEnabled(final HypervisorType hypervisorType, final String hypervisorVersion) {
        final HypervisorCapabilitiesVO result = getCapabilities(hypervisorType, hypervisorVersion);
        return result.getVmSnapshotEnabled();
    }
}
