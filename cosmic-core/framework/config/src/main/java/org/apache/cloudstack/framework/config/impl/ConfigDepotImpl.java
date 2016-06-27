package org.apache.cloudstack.framework.config.impl;

import com.cloud.utils.Pair;
import com.cloud.utils.exception.CloudRuntimeException;
import org.apache.cloudstack.framework.config.ConfigDepot;
import org.apache.cloudstack.framework.config.ConfigDepotAdmin;
import org.apache.cloudstack.framework.config.ConfigKey;
import org.apache.cloudstack.framework.config.Configurable;
import org.apache.cloudstack.framework.config.ScopedConfigStorage;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ConfigDepotImpl implements the ConfigDepot and ConfigDepotAdmin interface.
 * Its functionalities include:
 * - Control how dynamic config values are cached and refreshed.
 * - Control how scoped config values are stored.
 * - Gather all of the Configurable interfaces and insert their config
 * variables into the config table.
 * - Hide the data source where configs are stored and retrieved.
 * <p>
 * When dealing with this class, we must be very careful on cluster situations.
 * <p>
 * TODO:
 * - Move the rest of the changes to the config table to here.
 * - Add the code to mark the rows in configuration table without
 * the corresponding keys to be null.
 * - Move all of the configurations to using ConfigDepot
 * - Completely eliminate Config.java
 * - Figure out the correct categories.
 * - Add a scope for management server, where if the scope is management server
 * then the override is retrieved from a properties file.  Imagine adding a
 * new management server node and it is much more capable system than previous
 * management servers, you want the adjustments to thread pools etc to be
 * very different than other management serves.
 * - Add validation methods to ConfigKey<?>.  If a validation class is declared
 * when constructing a ConfigKey then configuration server should use the
 * validation class to validate the value the admin input for the key.
 */
public class ConfigDepotImpl implements ConfigDepot, ConfigDepotAdmin {
    private final static Logger s_logger = LoggerFactory.getLogger(ConfigDepotImpl.class);
    @Inject
    ConfigurationDao _configDao;
    List<Configurable> _configurables;
    List<ScopedConfigStorage> _scopedStorages;
    Set<Configurable> _configured = Collections.synchronizedSet(new HashSet<Configurable>());

    HashMap<String, Pair<String, ConfigKey<?>>> _allKeys = new HashMap<>(1007);

    HashMap<ConfigKey.Scope, Set<ConfigKey<?>>> _scopeLevelConfigsMap = new HashMap<>();

    public ConfigDepotImpl() {
        ConfigKey.init(this);
        _scopeLevelConfigsMap.put(ConfigKey.Scope.Zone, new HashSet<>());
        _scopeLevelConfigsMap.put(ConfigKey.Scope.Cluster, new HashSet<>());
        _scopeLevelConfigsMap.put(ConfigKey.Scope.StoragePool, new HashSet<>());
        _scopeLevelConfigsMap.put(ConfigKey.Scope.Account, new HashSet<>());
    }

    @Override
    public ConfigKey<?> get(final String key) {
        final Pair<String, ConfigKey<?>> value = _allKeys.get(key);
        return value != null ? value.second() : null;
    }

    @Override
    public Set<ConfigKey<?>> getConfigListByScope(final String scope) {
        return _scopeLevelConfigsMap.get(ConfigKey.Scope.valueOf(scope));
    }

    @Override
    public <T> void set(final ConfigKey<T> key, final T value) {
        _configDao.update(key.key(), value.toString());
    }

    @Override
    public <T> void createOrUpdateConfigObject(final String componentName, final ConfigKey<T> key, final String value) {
        createOrupdateConfigObject(new Date(), componentName, key, value);
    }

    private void createOrupdateConfigObject(final Date date, final String componentName, final ConfigKey<?> key, final String value) {
        ConfigurationVO vo = _configDao.findById(key.key());
        if (vo == null) {
            vo = new ConfigurationVO(componentName, key);
            vo.setUpdated(date);
            if (value != null) {
                vo.setValue(value);
            }
            _configDao.persist(vo);
        } else {
            if (vo.isDynamic() != key.isDynamic() || !ObjectUtils.equals(vo.getDescription(), key.description()) || !ObjectUtils.equals(vo.getDefaultValue(), key.defaultValue()) ||
                    !ObjectUtils.equals(vo.getScope(), key.scope().toString()) ||
                    !ObjectUtils.equals(vo.getComponent(), componentName)) {
                vo.setDynamic(key.isDynamic());
                vo.setDescription(key.description());
                vo.setDefaultValue(key.defaultValue());
                vo.setScope(key.scope().toString());
                vo.setComponent(componentName);
                vo.setUpdated(date);
                _configDao.persist(vo);
            }
        }
    }

    @PostConstruct
    @Override
    public void populateConfigurations() {
        final Date date = new Date();
        for (final Configurable configurable : _configurables) {
            populateConfiguration(date, configurable);
        }
    }

    protected void populateConfiguration(final Date date, final Configurable configurable) {
        if (_configured.contains(configurable)) {
            return;
        }

        s_logger.debug("Retrieving keys from " + configurable.getClass().getSimpleName());

        for (final ConfigKey<?> key : configurable.getConfigKeys()) {
            final Pair<String, ConfigKey<?>> previous = _allKeys.get(key.key());
            if (previous != null && !previous.first().equals(configurable.getConfigComponentName())) {
                throw new CloudRuntimeException("Configurable " + configurable.getConfigComponentName() + " is adding a key that has been added before by " +
                        previous.first() + ": " + key.toString());
            }
            _allKeys.put(key.key(), new Pair<>(configurable.getConfigComponentName(), key));

            createOrupdateConfigObject(date, configurable.getConfigComponentName(), key, null);

            if ((key.scope() != null) && (key.scope() != ConfigKey.Scope.Global)) {
                final Set<ConfigKey<?>> currentConfigs = _scopeLevelConfigsMap.get(key.scope());
                currentConfigs.add(key);
            }
        }

        _configured.add(configurable);
    }

    @Override
    public void populateConfiguration(final Configurable configurable) {
        populateConfiguration(new Date(), configurable);
    }

    @Override
    public List<String> getComponentsInDepot() {
        return new ArrayList<>();
    }

    public ConfigurationDao global() {
        return _configDao;
    }

    public ScopedConfigStorage scoped(final ConfigKey<?> config) {
        for (final ScopedConfigStorage storage : _scopedStorages) {
            if (storage.getScope() == config.scope()) {
                return storage;
            }
        }

        throw new CloudRuntimeException("Unable to find config storage for this scope: " + config.scope() + " for " + config.key());
    }

    public List<ScopedConfigStorage> getScopedStorages() {
        return _scopedStorages;
    }

    @Inject
    public void setScopedStorages(final List<ScopedConfigStorage> scopedStorages) {
        _scopedStorages = scopedStorages;
    }

    public List<Configurable> getConfigurables() {
        return _configurables;
    }

    @Inject
    public void setConfigurables(final List<Configurable> configurables) {
        _configurables = configurables;
    }
}
