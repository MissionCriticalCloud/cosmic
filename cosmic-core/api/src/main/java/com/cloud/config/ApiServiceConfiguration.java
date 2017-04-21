package com.cloud.config;

import com.cloud.framework.config.ConfigKey;
import com.cloud.framework.config.Configurable;

import javax.ejb.Local;

@Local(value = {ApiServiceConfiguration.class})
public class ApiServiceConfiguration implements Configurable {
    public static final ConfigKey<String> ManagementHostIPAdr = new ConfigKey<>("Advanced", String.class, "host", "localhost", "The ip address of management server", true);
    public static final ConfigKey<String> ApiServletPath = new ConfigKey<>("Advanced", String.class, "endpointe.url", "http://localhost:8080/client/api",
            "API end point. Can be used by CS components/services deployed remotely, for sending CS API requests", true);
    public static final ConfigKey<Long> DefaultUIPageSize = new ConfigKey<>("Advanced", Long.class, "default.ui.page.size", "20",
            "The default pagesize to be used by UI and other clients when making list* API calls", true, ConfigKey.Scope.Global);
    public static final ConfigKey<Boolean> ApiSourceCidrChecksEnabled = new ConfigKey<>("Advanced", Boolean.class, "api.source.cidr.checks.enabled",
            "true", "Are the source checks on API calls enabled (true) or not (false)? See api.allowed.source.cidr.list", true, ConfigKey.Scope.Global);
    public static final ConfigKey<String> ApiAllowedSourceCidrList = new ConfigKey<String>("Advanced", String.class, "api.allowed.source.cidr.list",
            "0.0.0.0/0,::/0", "Comma separated list of IPv4/IPv6 CIDRs from which API calls can be performed. Can be set on Global and Account levels.", true, ConfigKey.Scope.Account);

    @Override
    public String getConfigComponentName() {
        return ApiServiceConfiguration.class.getSimpleName();
    }

    @Override
    public ConfigKey<?>[] getConfigKeys() {
        return new ConfigKey<?>[]{ManagementHostIPAdr, ApiServletPath, DefaultUIPageSize, ApiSourceCidrChecksEnabled, ApiAllowedSourceCidrList};
    }
}
