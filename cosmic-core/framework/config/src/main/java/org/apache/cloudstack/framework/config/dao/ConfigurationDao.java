package org.apache.cloudstack.framework.config.dao;

import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.framework.config.impl.ConfigurationVO;

import java.util.Map;

public interface ConfigurationDao extends GenericDao<ConfigurationVO, String> {

    /**
     * 1. params passed in.
     * 2. configuration for the instance.
     * 3. configuration for the DEFAULT instance.
     *
     * @param params parameters from the components.xml which will override the database values.
     * @return a consolidated look at the configuration parameters.
     */
    public Map<String, String> getConfiguration(String instance, Map<String, ? extends Object> params);

    public Map<String, String> getConfiguration(Map<String, ? extends Object> params);

    public Map<String, String> getConfiguration();

    /**
     * Updates a configuration value
     *
     * @param value the new value
     * @return true if success, false if failure
     */
    public boolean update(String name, String value);

    /**
     * Gets the value for the specified configuration name
     *
     * @return value
     */
    public String getValue(String name);

    public String getValueAndInitIfNotExist(String name, String category, String initValue);

    public String getValueAndInitIfNotExist(String name, String category, String initValue, String desc);

    /**
     * returns whether or not this is a premium configuration
     *
     * @return true if premium configuration, false otherwise
     */
    boolean isPremium();

    ConfigurationVO findByName(String name);

    boolean update(String name, String category, String value);

    void invalidateCache();
}
