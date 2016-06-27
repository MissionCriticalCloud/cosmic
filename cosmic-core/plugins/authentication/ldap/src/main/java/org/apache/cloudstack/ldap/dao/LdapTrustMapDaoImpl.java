package org.apache.cloudstack.ldap.dao;

import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import org.apache.cloudstack.ldap.LdapTrustMapVO;

import org.springframework.stereotype.Component;

@Component
public class LdapTrustMapDaoImpl extends GenericDaoBase<LdapTrustMapVO, Long> implements LdapTrustMapDao {
    private final SearchBuilder<LdapTrustMapVO> domainIdSearch;

    public LdapTrustMapDaoImpl() {
        super();
        domainIdSearch = createSearchBuilder();
        domainIdSearch.and("domainId", domainIdSearch.entity().getDomainId(), SearchCriteria.Op.EQ);
        domainIdSearch.done();
    }

    @Override
    public LdapTrustMapVO findByDomainId(final long domainId) {
        final SearchCriteria<LdapTrustMapVO> sc = domainIdSearch.create();
        sc.setParameters("domainId", domainId);
        return findOneBy(sc);
    }
}
