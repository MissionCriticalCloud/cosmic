package org.apache.cloudstack.config;

import org.apache.cloudstack.framework.config.ConfigKey;
import org.apache.cloudstack.framework.config.Configurable;

import javax.ejb.Local;

@Local(value = {ApiServiceConfiguration.class})
public class ApiServiceConfiguration implements Configurable {
    public static final ConfigKey<String> ManagementHostIPAdr = new ConfigKey<>("Advanced", String.class, "host", "localhost", "The ip address of management server", true);
    public static final ConfigKey<String> ApiServletPath = new ConfigKey<>("Advanced", String.class, "endpointe.url", "http://localhost:8080/client/api",
            "API end point. Can be used by CS components/services deployed remotely, for sending CS API requests", true);
    public static final ConfigKey<Long> DefaultUIPageSize = new ConfigKey<>("Advanced", Long.class, "default.ui.page.size", "20",
            "The default pagesize to be used by UI and other clients when making list* API calls", true, ConfigKey.Scope.Global);

    @Override
    public String getConfigComponentName() {
        return ApiServiceConfiguration.class.getSimpleName();
    }

    @Override
    public ConfigKey<?>[] getConfigKeys() {
        return new ConfigKey<?>[]{ManagementHostIPAdr, ApiServletPath, DefaultUIPageSize};
    }
}
