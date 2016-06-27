package org.apache.cloudstack.framework.config;

/**
 * Configurable can be implemented by components to insert their own
 * configuration keys.
 * <p>
 * CloudStack will gather all of these configurations at startup and insert
 * them into the configuration table.
 */
public interface Configurable {

    /**
     * @return The name of the component that provided this configuration
     * variable.  This value is saved in the database so someone can easily
     * identify who provides this variable.
     **/
    String getConfigComponentName();

    /**
     * @return The list of config keys provided by this configuable.
     */
    ConfigKey<?>[] getConfigKeys();
}
