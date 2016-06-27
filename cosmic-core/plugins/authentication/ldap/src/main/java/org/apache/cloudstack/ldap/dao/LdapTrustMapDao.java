package org.apache.cloudstack.ldap.dao;

import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.ldap.LdapTrustMapVO;

public interface LdapTrustMapDao extends GenericDao<LdapTrustMapVO, Long> {
    LdapTrustMapVO findByDomainId(long domainId);
}
