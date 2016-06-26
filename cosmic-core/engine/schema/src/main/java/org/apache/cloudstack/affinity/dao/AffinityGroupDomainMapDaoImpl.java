package org.apache.cloudstack.affinity.dao;

import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;
import org.apache.cloudstack.affinity.AffinityGroupDomainMapVO;

import javax.annotation.PostConstruct;
import java.util.List;

public class AffinityGroupDomainMapDaoImpl extends GenericDaoBase<AffinityGroupDomainMapVO, Long> implements AffinityGroupDomainMapDao {

    private SearchBuilder<AffinityGroupDomainMapVO> ListByAffinityGroup;

    private SearchBuilder<AffinityGroupDomainMapVO> DomainsSearch;

    public AffinityGroupDomainMapDaoImpl() {
    }

    @PostConstruct
    protected void init() {
        ListByAffinityGroup = createSearchBuilder();
        ListByAffinityGroup.and("affinityGroupId", ListByAffinityGroup.entity().getAffinityGroupId(), SearchCriteria.Op.EQ);
        ListByAffinityGroup.done();

        DomainsSearch = createSearchBuilder();
        DomainsSearch.and("domainId", DomainsSearch.entity().getDomainId(), Op.IN);
        DomainsSearch.done();
    }

    @Override
    public AffinityGroupDomainMapVO findByAffinityGroup(final long affinityGroupId) {
        final SearchCriteria<AffinityGroupDomainMapVO> sc = ListByAffinityGroup.create();
        sc.setParameters("affinityGroupId", affinityGroupId);
        return findOneBy(sc);
    }

    @Override
    public List<AffinityGroupDomainMapVO> listByDomain(final Object... domainId) {
        final SearchCriteria<AffinityGroupDomainMapVO> sc = DomainsSearch.create();
        sc.setParameters("domainId", domainId);

        return listBy(sc);
    }
}
