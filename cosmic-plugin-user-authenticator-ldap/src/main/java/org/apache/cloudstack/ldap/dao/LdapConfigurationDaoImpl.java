package org.apache.cloudstack.ldap.dao;

import com.cloud.utils.Pair;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;
import org.apache.cloudstack.ldap.LdapConfigurationVO;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class LdapConfigurationDaoImpl extends GenericDaoBase<LdapConfigurationVO, Long> implements LdapConfigurationDao {
    private final SearchBuilder<LdapConfigurationVO> hostnameSearch;
    private final SearchBuilder<LdapConfigurationVO> listAllConfigurationsSearch;

    public LdapConfigurationDaoImpl() {
        super();
        hostnameSearch = createSearchBuilder();
        hostnameSearch.and("hostname", hostnameSearch.entity().getHostname(), SearchCriteria.Op.EQ);
        hostnameSearch.done();

        listAllConfigurationsSearch = createSearchBuilder();
        listAllConfigurationsSearch.and("hostname", listAllConfigurationsSearch.entity().getHostname(), Op.EQ);
        listAllConfigurationsSearch.and("port", listAllConfigurationsSearch.entity().getPort(), Op.EQ);
        listAllConfigurationsSearch.done();
    }

    @Override
    public LdapConfigurationVO findByHostname(final String hostname) {
        final SearchCriteria<LdapConfigurationVO> sc = hostnameSearch.create();
        sc.setParameters("hostname", hostname);
        return findOneBy(sc);
    }

    @Override
    public Pair<List<LdapConfigurationVO>, Integer> searchConfigurations(final String hostname, final int port) {
        final SearchCriteria<LdapConfigurationVO> sc = listAllConfigurationsSearch.create();
        if (hostname != null) {
            sc.setParameters("hostname", hostname);
        }
        return searchAndCount(sc, null);
    }
}
