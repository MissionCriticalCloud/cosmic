package org.apache.cloudstack.framework.config;

import java.util.List;

/**
 * Administrative interface to ConfigDepot
 */
public interface ConfigDepotAdmin {
    /**
     * Create configurations if there are new config parameters.
     * Update configurations if the parameter settings have been changed.
     * All configurations that have been updated/created will have the same timestamp in the updated field.
     * All previous configurations that should be obsolete will have a null updated field.
     *
     * @see Configuration
     */
    void populateConfigurations();

    void populateConfiguration(Configurable configurable);

    List<String> getComponentsInDepot();
}
