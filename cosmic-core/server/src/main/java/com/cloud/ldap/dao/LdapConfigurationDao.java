package com.cloud.ldap.dao;

import com.cloud.ldap.LdapConfigurationVO;
import com.cloud.legacymodel.utils.Pair;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface LdapConfigurationDao extends GenericDao<LdapConfigurationVO, Long> {
    LdapConfigurationVO findByHostname(String hostname);

    Pair<List<LdapConfigurationVO>, Integer> searchConfigurations(String hostname, int port);
}
