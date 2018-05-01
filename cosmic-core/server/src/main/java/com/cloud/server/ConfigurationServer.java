package com.cloud.server;

import com.cloud.framework.config.impl.ConfigurationVO;
import com.cloud.legacymodel.exceptions.InternalErrorException;

import java.util.List;

/**
 */
public interface ConfigurationServer {
    public static final String Name = "configuration-server";

    /**
     * Persists default values for the configuration table, pods/zones, and VLANs
     *
     * @return
     */
    public void persistDefaultValues() throws InternalErrorException;

    public void updateKeyPairs();

    public List<ConfigurationVO> getConfigListByScope(String scope, Long resourceId);
}
