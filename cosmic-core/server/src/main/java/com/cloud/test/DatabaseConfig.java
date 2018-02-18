package com.cloud.test;

import com.cloud.utils.db.TransactionLegacy;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class DatabaseConfig {
    private static final Map<String, String> s_configurationDescriptions = new HashMap<>();
    private static final Map<String, String> s_configurationComponents = new HashMap<>();
    private static final Map<String, String> s_defaultConfigurationValues = new HashMap<>();
    // Change to HashSet
    private static final HashSet<String> objectNames = new HashSet<>();
    private static final HashSet<String> fieldNames = new HashSet<>();

    static {
        // initialize the objectNames ArrayList
        objectNames.add("zone");
        objectNames.add("physicalNetwork");
        objectNames.add("vlan");
        objectNames.add("pod");
        objectNames.add("cluster");
        objectNames.add("storagePool");
        objectNames.add("secondaryStorage");
        objectNames.add("serviceOffering");
        objectNames.add("diskOffering");
        objectNames.add("user");
        objectNames.add("pricing");
        objectNames.add("configuration");
        objectNames.add("privateIpAddresses");
        objectNames.add("publicIpAddresses");
        objectNames.add("physicalNetworkServiceProvider");
        objectNames.add("virtualRouterProvider");

        // initialize the fieldNames ArrayList
        fieldNames.add("id");
        fieldNames.add("name");
        fieldNames.add("dns1");
        fieldNames.add("dns2");
        fieldNames.add("internalDns1");
        fieldNames.add("internalDns2");
        fieldNames.add("guestNetworkCidr");
        fieldNames.add("gateway");
        fieldNames.add("netmask");
        fieldNames.add("vncConsoleIp");
        fieldNames.add("zoneId");
        fieldNames.add("vlanId");
        fieldNames.add("cpu");
        fieldNames.add("ramSize");
        fieldNames.add("useLocalStorage");
        fieldNames.add("hypervisorType");
        fieldNames.add("diskSpace");
        fieldNames.add("nwRate");
        fieldNames.add("mcRate");
        fieldNames.add("price");
        fieldNames.add("username");
        fieldNames.add("password");
        fieldNames.add("firstname");
        fieldNames.add("lastname");
        fieldNames.add("email");
        fieldNames.add("priceUnit");
        fieldNames.add("type");
        fieldNames.add("value");
        fieldNames.add("podId");
        fieldNames.add("podName");
        fieldNames.add("ipAddressRange");
        fieldNames.add("vlanType");
        fieldNames.add("vlanName");
        fieldNames.add("cidr");
        fieldNames.add("vnet");
        fieldNames.add("mirrored");
        fieldNames.add("enableHA");
        fieldNames.add("displayText");
        fieldNames.add("domainId");
        fieldNames.add("hostAddress");
        fieldNames.add("hostPath");
        fieldNames.add("guestIpType");
        fieldNames.add("url");
        fieldNames.add("storageType");
        fieldNames.add("category");
        fieldNames.add("tags");
        fieldNames.add("networktype");
        fieldNames.add("clusterId");
        fieldNames.add("physicalNetworkId");
        fieldNames.add("destPhysicalNetworkId");
        fieldNames.add("providerName");
        fieldNames.add("vpn");
        fieldNames.add("dhcp");
        fieldNames.add("dns");
        fieldNames.add("firewall");
        fieldNames.add("sourceNat");
        fieldNames.add("loadBalance");
        fieldNames.add("staticNat");
        fieldNames.add("portForwarding");
        fieldNames.add("userData");
        fieldNames.add("nspId");

        s_configurationDescriptions.put("host.stats.interval", "the interval in milliseconds when host stats are retrieved from agents");
        s_configurationDescriptions.put("storage.stats.interval", "the interval in milliseconds when storage stats (per host) are retrieved from agents");
        s_configurationDescriptions.put("volume.stats.interval", "the interval in milliseconds when volume stats are retrieved from agents");
        s_configurationDescriptions.put("host", "host address to listen on for agent connection");
        s_configurationDescriptions.put("port", "port to listen on for agent connection");
        s_configurationDescriptions.put("guest.domain.suffix", "domain suffix for users");
        s_configurationDescriptions.put("instance.name", "Name of the deployment instance");
        s_configurationDescriptions.put("storage.overprovisioning.factor", "Storage Allocator overprovisioning factor");
        s_configurationDescriptions.put("retries.per.host", "The number of times each command sent to a host should be retried in case of failure.");
        s_configurationDescriptions.put("integration.api.port", "internal port used by the management server for servicing Integration API requests");
        s_configurationDescriptions.put("usage.stats.job.exec.time",
                "the time at which the usage statistics aggregation job will run as an HH24:MM time, e.g. 00:30 to run at 12:30am");
        s_configurationDescriptions.put("usage.stats.job.aggregation.range",
                "the range of time for aggregating the user statistics specified in minutes (e.g. 1440 for daily, 60 for hourly)");
        s_configurationDescriptions.put("consoleproxy.domP.enable", "Obsolete");
        s_configurationDescriptions.put("consoleproxy.port", "Obsolete");
        s_configurationDescriptions.put("consoleproxy.url.port", "Console proxy port for AJAX viewer");
        s_configurationDescriptions.put("consoleproxy.ram.size", "RAM size (in MB) used to create new console proxy VMs");
        s_configurationDescriptions.put("consoleproxy.cmd.port", "Console proxy command port that is used to communicate with management server");
        s_configurationDescriptions.put("consoleproxy.loadscan.interval", "The time interval(in milliseconds) to scan console proxy working-load info");
        s_configurationDescriptions.put("consoleproxy.capacityscan.interval",
                "The time interval(in millisecond) to scan whether or not system needs more console proxy to ensure minimal standby capacity");
        s_configurationDescriptions.put("consoleproxy.capacity.standby",
                "The minimal number of console proxy viewer sessions that system is able to serve immediately(standby capacity)");
        s_configurationDescriptions.put("alert.email.addresses", "comma seperated list of email addresses used for sending alerts");
        s_configurationDescriptions.put("alert.smtp.host", "SMTP hostname used for sending out email alerts");
        s_configurationDescriptions.put("alert.smtp.port", "port the SMTP server is listening on (default is 25)");
        s_configurationDescriptions.put("alert.smtp.useAuth",
                "If true, use SMTP authentication when sending emails.  If false, do not use SMTP authentication when sending emails.");
        s_configurationDescriptions.put("alert.smtp.username", "username for SMTP authentication (applies only if alert.smtp.useAuth is true)");
        s_configurationDescriptions.put("alert.smtp.password", "password for SMTP authentication (applies only if alert.smtp.useAuth is true)");
        s_configurationDescriptions.put("alert.email.sender", "sender of alert email (will be in the From header of the email)");
        s_configurationDescriptions.put("memory.capacity.threshold",
                "percentage (as a value between 0 and 1) of memory utilization above which alerts will be sent about low memory available");
        s_configurationDescriptions.put("cpu.capacity.threshold",
                "percentage (as a value between 0 and 1) of cpu utilization above which alerts will be sent about low cpu available");
        s_configurationDescriptions.put("storage.capacity.threshold",
                "percentage (as a value between 0 and 1) of storage utilization above which alerts will be sent about low storage available");
        s_configurationDescriptions.put("public.ip.capacity.threshold",
                "percentage (as a value between 0 and 1) of public IP address space utilization above which alerts will be sent");
        s_configurationDescriptions.put("private.ip.capacity.threshold",
                "percentage (as a value between 0 and 1) of private IP address space utilization above which alerts will be sent");
        s_configurationDescriptions.put("expunge.interval", "the interval to wait before running the expunge thread");
        s_configurationDescriptions.put("network.throttling.rate", "default data transfer rate in megabits per second allowed per user");
        s_configurationDescriptions.put("multicast.throttling.rate", "default multicast rate in megabits per second allowed");
        s_configurationDescriptions.put("system.vm.use.local.storage", "Indicates whether to use local storage pools or shared storage pools for system VMs.");
        s_configurationDescriptions.put("snapshot.poll.interval", "The time interval in seconds when the management server polls for snapshots to be scheduled.");
        s_configurationDescriptions.put("snapshot.max.hourly", "Maximum hourly snapshots for a volume");
        s_configurationDescriptions.put("snapshot.max.daily", "Maximum daily snapshots for a volume");
        s_configurationDescriptions.put("snapshot.max.weekly", "Maximum weekly snapshots for a volume");
        s_configurationDescriptions.put("snapshot.max.monthly", "Maximum monthly snapshots for a volume");
        s_configurationDescriptions.put("snapshot.delta.max", "max delta snapshots between two full snapshots.");
        s_configurationDescriptions.put("hypervisor.type", "The type of hypervisor that this deployment will use.");
        s_configurationDescriptions.put("publish.action.events", "enable or disable to control the publishing of action events on the event bus");
        s_configurationDescriptions.put("publish.alert.events", "enable or disable to control the publishing of alert events on the event bus");
        s_configurationDescriptions.put("publish.resource.state.events", "enable or disable to control the publishing of resource state events on the event bus");
        s_configurationDescriptions.put("publish.usage.events", "enable or disable to control the publishing of usage events on the event bus");
        s_configurationDescriptions.put("publish.async.job.events", "enable or disable to control the publishing of async job events on the event bus");

        s_configurationComponents.put("host.stats.interval", "management-server");
        s_configurationComponents.put("storage.stats.interval", "management-server");
        s_configurationComponents.put("volume.stats.interval", "management-server");
        s_configurationComponents.put("integration.api.port", "management-server");
        s_configurationComponents.put("usage.stats.job.exec.time", "management-server");
        s_configurationComponents.put("usage.stats.job.aggregation.range", "management-server");
        s_configurationComponents.put("consoleproxy.domP.enable", "management-server");
        s_configurationComponents.put("consoleproxy.port", "management-server");
        s_configurationComponents.put("consoleproxy.url.port", "management-server");
        s_configurationComponents.put("alert.email.addresses", "management-server");
        s_configurationComponents.put("alert.smtp.host", "management-server");
        s_configurationComponents.put("alert.smtp.port", "management-server");
        s_configurationComponents.put("alert.smtp.useAuth", "management-server");
        s_configurationComponents.put("alert.smtp.username", "management-server");
        s_configurationComponents.put("alert.smtp.password", "management-server");
        s_configurationComponents.put("alert.email.sender", "management-server");
        s_configurationComponents.put("memory.capacity.threshold", "management-server");
        s_configurationComponents.put("cpu.capacity.threshold", "management-server");
        s_configurationComponents.put("storage.capacity.threshold", "management-server");
        s_configurationComponents.put("public.ip.capacity.threshold", "management-server");
        s_configurationComponents.put("private.ip.capacity.threshold", "management-server");
        s_configurationComponents.put("capacity.check.period", "management-server");
        s_configurationComponents.put("network.throttling.rate", "management-server");
        s_configurationComponents.put("multicast.throttling.rate", "management-server");
        s_configurationComponents.put("event.purge.interval", "management-server");
        s_configurationComponents.put("account.cleanup.interval", "management-server");
        s_configurationComponents.put("expunge.delay", "UserVmManager");
        s_configurationComponents.put("expunge.interval", "UserVmManager");
        s_configurationComponents.put("host", "AgentManager");
        s_configurationComponents.put("port", "AgentManager");
        s_configurationComponents.put("domain", "AgentManager");
        s_configurationComponents.put("instance.name", "AgentManager");
        s_configurationComponents.put("storage.overprovisioning.factor", "StorageAllocator");
        s_configurationComponents.put("retries.per.host", "AgentManager");
        s_configurationComponents.put("start.retry", "AgentManager");
        s_configurationComponents.put("wait", "AgentManager");
        s_configurationComponents.put("ping.timeout", "AgentManager");
        s_configurationComponents.put("ping.interval", "AgentManager");
        s_configurationComponents.put("alert.wait", "AgentManager");
        s_configurationComponents.put("update.wait", "AgentManager");
        s_configurationComponents.put("guest.domain.suffix", "AgentManager");
        s_configurationComponents.put("consoleproxy.ram.size", "AgentManager");
        s_configurationComponents.put("consoleproxy.cmd.port", "AgentManager");
        s_configurationComponents.put("consoleproxy.loadscan.interval", "AgentManager");
        s_configurationComponents.put("consoleproxy.capacityscan.interval", "AgentManager");
        s_configurationComponents.put("consoleproxy.capacity.standby", "AgentManager");
        s_configurationComponents.put("consoleproxy.session.max", "AgentManager");
        s_configurationComponents.put("consoleproxy.session.timeout", "AgentManager");
        s_configurationComponents.put("expunge.workers", "UserVmManager");
        s_configurationComponents.put("extract.url.cleanup.interval", "management-server");
        s_configurationComponents.put("stop.retry.interval", "HighAvailabilityManager");
        s_configurationComponents.put("restart.retry.interval", "HighAvailabilityManager");
        s_configurationComponents.put("investigate.retry.interval", "HighAvailabilityManager");
        s_configurationComponents.put("migrate.retry.interval", "HighAvailabilityManager");
        s_configurationComponents.put("storage.overwrite.provisioning", "UserVmManager");
        s_configurationComponents.put("init", "none");
        s_configurationComponents.put("system.vm.use.local.storage", "ManagementServer");
        s_configurationComponents.put("snapshot.poll.interval", "SnapshotManager");
        s_configurationComponents.put("snapshot.max.hourly", "SnapshotManager");
        s_configurationComponents.put("snapshot.max.daily", "SnapshotManager");
        s_configurationComponents.put("snapshot.max.weekly", "SnapshotManager");
        s_configurationComponents.put("snapshot.max.monthly", "SnapshotManager");
        s_configurationComponents.put("snapshot.delta.max", "SnapshotManager");
        s_configurationComponents.put("hypervisor.type", "ManagementServer");
        s_configurationComponents.put("publish.action.events", "management-server");
        s_configurationComponents.put("publish.alert.events", "management-server");
        s_configurationComponents.put("publish.resource.state.events", "management-server");
        s_configurationComponents.put("publish.async.job.events", "management-server");

        s_defaultConfigurationValues.put("host.stats.interval", "60000");
        s_defaultConfigurationValues.put("storage.stats.interval", "60000");
        s_defaultConfigurationValues.put("port", "8250");
        s_defaultConfigurationValues.put("integration.api.port", "8096");
        s_defaultConfigurationValues.put("usage.stats.job.exec.time", "00:15"); // run at 12:15am
        s_defaultConfigurationValues.put("usage.stats.job.aggregation.range", "1440"); // do a daily aggregation
        s_defaultConfigurationValues.put("storage.overprovisioning.factor", "2");
        s_defaultConfigurationValues.put("retries.per.host", "2");
        s_defaultConfigurationValues.put("ping.timeout", "2.5");
        s_defaultConfigurationValues.put("ping.interval", "60");
        s_defaultConfigurationValues.put("snapshot.poll.interval", "300");
        s_defaultConfigurationValues.put("snapshot.max.hourly", "8");
        s_defaultConfigurationValues.put("snapshot.max.daily", "8");
        s_defaultConfigurationValues.put("snapshot.max.weekly", "8");
        s_defaultConfigurationValues.put("snapshot.max.monthly", "8");
        s_defaultConfigurationValues.put("snapshot.delta.max", "16");
        s_defaultConfigurationValues.put("alert.wait", "1800");
        s_defaultConfigurationValues.put("update.wait", "600");
        s_defaultConfigurationValues.put("expunge.interval", "86400");
        s_defaultConfigurationValues.put("extract.url.cleanup.interval", "120");
        s_defaultConfigurationValues.put("instance.name", "VM");
        s_defaultConfigurationValues.put("expunge.workers", "1");
        s_defaultConfigurationValues.put("stop.retry.interval", "600");
        s_defaultConfigurationValues.put("restart.retry.interval", "600");
        s_defaultConfigurationValues.put("investigate.retry.interval", "60");
        s_defaultConfigurationValues.put("migrate.retry.interval", "120");
        s_defaultConfigurationValues.put("event.purge.interval", "86400");
        s_defaultConfigurationValues.put("account.cleanup.interval", "86400");
        s_defaultConfigurationValues.put("system.vm.use.local.storage", "false");
        s_defaultConfigurationValues.put("init", "false");
        s_defaultConfigurationValues.put("cpu.overprovisioning.factor", "1");
        s_defaultConfigurationValues.put("mem.overprovisioning.factor", "1");
        s_defaultConfigurationValues.put("publish.action.events", "true");
        s_defaultConfigurationValues.put("publish.alert.events", "true");
        s_defaultConfigurationValues.put("publish.resource.state.events", "true");
        s_defaultConfigurationValues.put("publish.async.job.events", "true");
    }

    public static void printError(final String message) {
        System.out.println(message);
        System.exit(1);
    }

    public static String getDatabaseValueString(final String selectSql, final String name, final String errorMsg) {
        final TransactionLegacy txn = TransactionLegacy.open("getDatabaseValueString");
        PreparedStatement stmt = null;

        try {
            stmt = txn.prepareAutoCloseStatement(selectSql);
            final ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                final String value = rs.getString(name);
                return value;
            } else {
                return null;
            }
        } catch (final SQLException e) {
            System.out.println("Exception: " + e.getMessage());
            printError(errorMsg);
        } finally {
            txn.close();
        }
        return null;
    }

    public static long getDatabaseValueLong(final String selectSql, final String name, final String errorMsg) {
        final TransactionLegacy txn = TransactionLegacy.open("getDatabaseValueLong");
        PreparedStatement stmt = null;

        try {
            stmt = txn.prepareAutoCloseStatement(selectSql);
            final ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(name);
            } else {
                return -1;
            }
        } catch (final SQLException e) {
            System.out.println("Exception: " + e.getMessage());
            printError(errorMsg);
        } finally {
            txn.close();
        }
        return -1;
    }
}
