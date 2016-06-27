package com.cloud.vpc.dao;

import com.cloud.utils.db.GenericDaoBase;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.cloudstack.framework.config.impl.ConfigurationVO;

import java.util.HashMap;
import java.util.Map;

public class MockConfigurationDaoImpl extends GenericDaoBase<ConfigurationVO, String> implements ConfigurationDao {

    /* (non-Javadoc)
     * @see com.cloud.configuration.dao.ConfigurationDao#getConfiguration(java.lang.String, java.util.Map)
     */
    @Override
    public Map<String, String> getConfiguration(final String instance, final Map<String, ? extends Object> params) {
        return new HashMap<>();
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.dao.ConfigurationDao#getConfiguration(java.util.Map)
     */
    @Override
    public Map<String, String> getConfiguration(final Map<String, ? extends Object> params) {
        return new HashMap<>();
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.dao.ConfigurationDao#getConfiguration()
     */
    @Override
    public Map<String, String> getConfiguration() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.dao.ConfigurationDao#update(java.lang.String, java.lang.String)
     */
    @Override
    public boolean update(final String name, final String value) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.dao.ConfigurationDao#getValue(java.lang.String)
     */
    @Override
    public String getValue(final String name) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.dao.ConfigurationDao#getValueAndInitIfNotExist(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public String getValueAndInitIfNotExist(final String name, final String category, final String initValue) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.dao.ConfigurationDao#getValueAndInitIfNotExist(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public String getValueAndInitIfNotExist(final String name, final String category, final String initValue, final String desc) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.dao.ConfigurationDao#isPremium()
     */
    @Override
    public boolean isPremium() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.dao.ConfigurationDao#findByName(java.lang.String)
     */
    @Override
    public ConfigurationVO findByName(final String name) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.dao.ConfigurationDao#update(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public boolean update(final String name, final String category, final String value) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void invalidateCache() {
    }
}
