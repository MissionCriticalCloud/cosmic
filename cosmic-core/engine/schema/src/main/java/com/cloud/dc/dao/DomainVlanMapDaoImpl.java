package com.cloud.dc.dao;

import com.cloud.dc.DomainVlanMapVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

import javax.ejb.Local;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
@Local(value = {DomainVlanMapDao.class})
public class DomainVlanMapDaoImpl extends GenericDaoBase<DomainVlanMapVO, Long> implements DomainVlanMapDao {
    protected SearchBuilder<DomainVlanMapVO> DomainSearch;
    protected SearchBuilder<DomainVlanMapVO> VlanSearch;
    protected SearchBuilder<DomainVlanMapVO> DomainVlanSearch;

    public DomainVlanMapDaoImpl() {
        DomainSearch = createSearchBuilder();
        DomainSearch.and("domainId", DomainSearch.entity().getDomainId(), SearchCriteria.Op.EQ);
        DomainSearch.done();

        VlanSearch = createSearchBuilder();
        VlanSearch.and("vlanDbId", VlanSearch.entity().getVlanDbId(), SearchCriteria.Op.EQ);
        VlanSearch.done();

        DomainVlanSearch = createSearchBuilder();
        DomainVlanSearch.and("domainId", DomainVlanSearch.entity().getDomainId(), SearchCriteria.Op.EQ);
        DomainVlanSearch.and("vlanDbId", DomainVlanSearch.entity().getVlanDbId(), SearchCriteria.Op.EQ);
        DomainVlanSearch.done();
    }

    @Override
    public List<DomainVlanMapVO> listDomainVlanMapsByDomain(final long domainId) {
        final SearchCriteria<DomainVlanMapVO> sc = DomainSearch.create();
        sc.setParameters("domainId", domainId);
        return listIncludingRemovedBy(sc);
    }

    @Override
    public List<DomainVlanMapVO> listDomainVlanMapsByVlan(final long vlanDbId) {
        final SearchCriteria<DomainVlanMapVO> sc = VlanSearch.create();
        sc.setParameters("vlanDbId", vlanDbId);
        return listIncludingRemovedBy(sc);
    }

    @Override
    public DomainVlanMapVO findDomainVlanMap(final long domainId, final long vlanDbId) {
        final SearchCriteria<DomainVlanMapVO> sc = DomainVlanSearch.create();
        sc.setParameters("domainId", domainId);
        sc.setParameters("vlanDbId", vlanDbId);
        return findOneIncludingRemovedBy(sc);
    }
}
