//

//

package com.cloud.network.nicira;

public class ControlClusterStatus {
    private String clusterStatus;
    private Stats nodeStats;
    private Stats queueStats;
    private Stats portStats;
    private Stats routerportStats;
    private Stats switchStats;
    private Stats zoneStats;
    private Stats routerStats;
    private Stats securityProfileStats;
    private ClusterRoleConfig[] configuredRoles;

    public ClusterRoleConfig[] getConfiguredRoles() {
        return configuredRoles;
    }

    public String getClusterStatus() {
        return clusterStatus;
    }

    public Stats getNodeStats() {
        return nodeStats;
    }

    public Stats getLqueueStats() {
        return queueStats;
    }

    public Stats getLportStats() {
        return portStats;
    }

    public Stats getLrouterportStats() {
        return routerportStats;
    }

    public Stats getLswitchStats() {
        return switchStats;
    }

    public Stats getZoneStats() {
        return zoneStats;
    }

    public Stats getLrouterStats() {
        return routerStats;
    }

    public Stats getSecurityProfileStats() {
        return securityProfileStats;
    }

    public class Stats {
        private int errorStateCount;
        private int registeredCount;
        private int activeCount;

        public int getErrorStateCount() {
            return errorStateCount;
        }

        public int getRegisteredCount() {
            return registeredCount;
        }

        public int getActiveCount() {
            return activeCount;
        }
    }

    public class ClusterRoleConfig {
        public String majorityVersion;
        public String role;

        public String getMajorityVersion() {
            return majorityVersion;
        }

        public String getRole() {
            return role;
        }
    }
}
