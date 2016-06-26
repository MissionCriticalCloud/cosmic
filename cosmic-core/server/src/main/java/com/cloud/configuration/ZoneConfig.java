package com.cloud.configuration;

import java.util.ArrayList;
import java.util.List;

public enum ZoneConfig {
    EnableSecStorageVm(Boolean.class, "enable.secstorage.vm", "true", "Enables secondary storage vm service", null),
    EnableConsoleProxyVm(Boolean.class, "enable.consoleproxy.vm", "true", "Enables console proxy vm service", null),
    MaxHosts(Long.class, "max.hosts", null, "Maximum number of hosts the Zone can have", null),
    MaxVirtualMachines(Long.class, "max.vms", null, "Maximum number of VMs the Zone can have", null),
    ZoneMode(String.class, "zone.mode", null, "Mode of the Zone", "Free,Basic,Advanced"),
    HasNoPublicIp(Boolean.class, "has.no.public.ip", "false", "True if Zone has no public IP", null),
    DhcpStrategy(String.class, "zone.dhcp.strategy", "cloudstack-systemvm", "Who controls DHCP", "cloudstack-systemvm,cloudstack-external,external"),
    DnsSearchOrder(String.class, "network.guestnetwork.dns.search.order", null, "Domains list to be used for domain search order", null);

    private static final List<String> ZoneConfigKeys = new ArrayList<>();

    static {
        // Add keys into List
        for (final ZoneConfig c : ZoneConfig.values()) {
            final String key = c.key();
            ZoneConfigKeys.add(key);
        }
    }

    private final Class<?> _type;
    private final String _name;
    private final String _defaultValue;
    private final String _description;
    private final String _range;

    private ZoneConfig(final Class<?> type, final String name, final String defaultValue, final String description, final String range) {

        _type = type;
        _name = name;
        _defaultValue = defaultValue;
        _description = description;
        _range = range;
    }

    public static boolean doesKeyExist(final String key) {
        return ZoneConfigKeys.contains(key);
    }

    public Class<?> getType() {
        return _type;
    }

    public String getName() {
        return _name;
    }

    public String getDefaultValue() {
        return _defaultValue;
    }

    public String getDescription() {
        return _description;
    }

    public String getRange() {
        return _range;
    }

    public String key() {
        return _name;
    }

}
