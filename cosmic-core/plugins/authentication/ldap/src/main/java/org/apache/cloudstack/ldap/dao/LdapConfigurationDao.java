package org.apache.cloudstack.ldap.dao;

import com.cloud.utils.Pair;
import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.ldap.LdapConfigurationVO;

import java.util.List;

public interface LdapConfigurationDao extends GenericDao<LdapConfigurationVO, Long> {
    LdapConfigurationVO findByHostname(String hostname);

    Pair<List<LdapConfigurationVO>, Integer> searchConfigurations(String hostname, int port);
}
