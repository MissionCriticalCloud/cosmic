package org.apache.cloudstack.framework.config.dao;

import com.cloud.utils.component.ComponentLifecycle;
import com.cloud.utils.crypt.DBEncryptionUtil;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.TransactionLegacy;
import com.cloud.utils.exception.CloudRuntimeException;
import org.apache.cloudstack.framework.config.impl.ConfigurationVO;

import javax.annotation.PostConstruct;
import javax.naming.ConfigurationException;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ConfigurationDaoImpl extends GenericDaoBase<ConfigurationVO, String> implements ConfigurationDao {
    public static final String UPDATE_CONFIGURATION_SQL = "UPDATE configuration SET value = ? WHERE name = ?";
    private static final Logger s_logger = LoggerFactory.getLogger(ConfigurationDaoImpl.class);
    final SearchBuilder<ConfigurationVO> InstanceSearch;
    final SearchBuilder<ConfigurationVO> NameSearch;
    private Map<String, String> _configs = null;
    private boolean _premium;

    public ConfigurationDaoImpl() {
        InstanceSearch = createSearchBuilder();
        InstanceSearch.and("instance", InstanceSearch.entity().getInstance(), SearchCriteria.Op.EQ);

        NameSearch = createSearchBuilder();
        NameSearch.and("name", NameSearch.entity().getName(), SearchCriteria.Op.EQ);
        setRunLevel(ComponentLifecycle.RUN_LEVEL_SYSTEM_BOOTSTRAP);
    }

    @PostConstruct
    public void init() throws ConfigurationException {
        /* This bean is loaded in bootstrap and beans
         * in bootstrap don't go through the CloudStackExtendedLifeCycle
         */
        configure(getName(), getConfigParams());
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        super.configure(name, params);

        final Object premium = params.get("premium");
        _premium = (premium != null) && ((String) premium).equals("true");

        return true;
    }

    @Override
    public boolean isPremium() {
        return _premium;
    }

    @Override
    public void invalidateCache() {
        _configs = null;
    }

    @Override
    public Map<String, String> getConfiguration(final String instance, final Map<String, ? extends Object> params) {
        if (_configs == null) {
            _configs = new HashMap<>();

            SearchCriteria<ConfigurationVO> sc = InstanceSearch.create();
            sc.setParameters("instance", "DEFAULT");

            List<ConfigurationVO> configurations = listIncludingRemovedBy(sc);

            for (final ConfigurationVO config : configurations) {
                if (config.getValue() != null) {
                    _configs.put(config.getName(), config.getValue());
                }
            }

            if (!"DEFAULT".equals(instance)) {
                //Default instance params are already added, need not add again
                sc = InstanceSearch.create();
                sc.setParameters("instance", instance);

                configurations = listIncludingRemovedBy(sc);

                for (final ConfigurationVO config : configurations) {
                    if (config.getValue() != null) {
                        _configs.put(config.getName(), config.getValue());
                    }
                }
            }
        }

        mergeConfigs(_configs, params);
        return _configs;
    }

    @Override
    public Map<String, String> getConfiguration(final Map<String, ? extends Object> params) {
        return getConfiguration("DEFAULT", params);
    }

    @Override
    public Map<String, String> getConfiguration() {
        return getConfiguration("DEFAULT", new HashMap<>());
    }

    protected void mergeConfigs(final Map<String, String> dbParams, final Map<String, ? extends Object> xmlParams) {
        for (final Map.Entry<String, ? extends Object> param : xmlParams.entrySet()) {
            dbParams.put(param.getKey(), (String) param.getValue());
        }
    }

    //Use update method with category instead
    @Override
    @Deprecated
    public boolean update(final String name, final String value) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        try (PreparedStatement stmt = txn.prepareStatement(UPDATE_CONFIGURATION_SQL)) {
            stmt.setString(1, value);
            stmt.setString(2, name);
            stmt.executeUpdate();
            return true;
        } catch (final Exception e) {
            s_logger.warn("Unable to update Configuration Value", e);
        }
        return false;
    }

    @Override
    public boolean update(final String name, final String category, String value) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        try {
            value = ("Hidden".equals(category) || "Secure".equals(category)) ? DBEncryptionUtil.encrypt(value) : value;
            try (PreparedStatement stmt = txn.prepareStatement(UPDATE_CONFIGURATION_SQL)) {
                stmt.setString(1, value);
                stmt.setString(2, name);
                stmt.executeUpdate();
                return true;
            }
        } catch (final Exception e) {
            s_logger.warn("Unable to update Configuration Value", e);
        }
        return false;
    }

    @Override
    public String getValue(final String name) {
        final ConfigurationVO config = findByName(name);
        return (config == null) ? null : config.getValue();
    }

    @Override
    public String getValueAndInitIfNotExist(final String name, final String category, final String initValue) {
        return getValueAndInitIfNotExist(name, category, initValue, "");
    }

    @Override
    @DB
    public String getValueAndInitIfNotExist(final String name, final String category, final String initValue, final String desc) {
        String returnValue = initValue;
        try {
            final ConfigurationVO config = findByName(name);
            if (config != null) {
                if (config.getValue() != null) {
                    returnValue = config.getValue();
                } else {
                    update(name, category, initValue);
                }
            } else {
                final ConfigurationVO newConfig = new ConfigurationVO(category, "DEFAULT", "management-server", name, initValue, desc);
                persist(newConfig);
            }
            return returnValue;
        } catch (final Exception e) {
            s_logger.warn("Unable to update Configuration Value", e);
            throw new CloudRuntimeException("Unable to initialize configuration variable: " + name);
        }
    }

    @Override
    public ConfigurationVO findByName(final String name) {
        final SearchCriteria<ConfigurationVO> sc = NameSearch.create();
        sc.setParameters("name", name);
        return findOneIncludingRemovedBy(sc);
    }
}
