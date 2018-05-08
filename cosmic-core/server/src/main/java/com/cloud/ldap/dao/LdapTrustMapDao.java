package com.cloud.ldap.dao;

import com.cloud.ldap.LdapTrustMapVO;
import com.cloud.utils.db.GenericDao;

public interface LdapTrustMapDao extends GenericDao<LdapTrustMapVO, Long> {
    LdapTrustMapVO findByDomainId(long domainId);
}
