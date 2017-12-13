package com.cloud.agent.api.to;

import com.cloud.network.lb.LoadBalancingRule.LbDestination;
import com.cloud.network.lb.LoadBalancingRule.LbHealthCheckPolicy;
import com.cloud.network.lb.LoadBalancingRule.LbSslCert;
import com.cloud.network.lb.LoadBalancingRule.LbStickinessPolicy;
import com.cloud.utils.Pair;

import java.util.ArrayList;
import java.util.List;

public class LoadBalancerTO {
    final static int MAX_STICKINESS_POLICIES = 1;
    final static int MAX_HEALTHCHECK_POLICIES = 1;
    String uuid;
    String srcIp;
    int srcPort;
    String protocol;
    String lbProtocol;
    String algorithm;
    boolean revoked;
    boolean alreadyAdded;
    boolean inline;
    DestinationTO[] destinations;
    private StickinessPolicyTO[] stickinessPolicies;
    private HealthCheckPolicyTO[] healthCheckPolicies;
    private LbSslCert sslCert; /* XXX: Should this be SslCertTO?  */
    private int clientTimeout;
    private int serverTimeout;

    public LoadBalancerTO(final String id, final String srcIp, final int srcPort, final String protocol, final String algorithm, final boolean revoked,
                          final boolean alreadyAdded, final boolean inline, final List<LbDestination> argDestinations, final List<LbStickinessPolicy> stickinessPolicies,
                          final int clientTimeout, final int serverTimeout) {

        this(id, srcIp, srcPort, protocol, algorithm, revoked, alreadyAdded, inline, argDestinations, stickinessPolicies, null, null, null, clientTimeout, serverTimeout);
    }

    public LoadBalancerTO(final String id, final String srcIp, final int srcPort, final String protocol, final String algorithm, final boolean revoked,
                          final boolean alreadyAdded, final boolean inline, final List<LbDestination> argDestinations, final List<LbStickinessPolicy> stickinessPolicies,
                          final List<LbHealthCheckPolicy> healthCheckPolicies, final LbSslCert sslCert, final String lbProtocol, final int clientTimeout, final int serverTimeout) {
        this(id, srcIp, srcPort, protocol, algorithm, revoked, alreadyAdded, inline, argDestinations, clientTimeout, serverTimeout);
        this.stickinessPolicies = null;
        this.healthCheckPolicies = null;
        this.clientTimeout = clientTimeout;
        this.serverTimeout = serverTimeout;
        if (stickinessPolicies != null && stickinessPolicies.size() > 0) {
            this.stickinessPolicies = new StickinessPolicyTO[MAX_STICKINESS_POLICIES];
            int index = 0;
            for (final LbStickinessPolicy stickinesspolicy : stickinessPolicies) {
                if (!stickinesspolicy.isRevoked()) {
                    this.stickinessPolicies[index] = new StickinessPolicyTO(stickinesspolicy.getMethodName(), stickinesspolicy.getParams());
                    index++;
                    if (index == MAX_STICKINESS_POLICIES) {
                        break;
                    }
                }
            }
            if (index == 0) {
                this.stickinessPolicies = null;
            }
        }

        if (healthCheckPolicies != null && healthCheckPolicies.size() > 0) {
            this.healthCheckPolicies = new HealthCheckPolicyTO[MAX_HEALTHCHECK_POLICIES];
            int index = 0;
            for (final LbHealthCheckPolicy hcp : healthCheckPolicies) {
                this.healthCheckPolicies[0] =
                        new HealthCheckPolicyTO(hcp.getpingpath(), hcp.getDescription(), hcp.getResponseTime(), hcp.getHealthcheckInterval(), hcp.getHealthcheckThresshold(),
                                hcp.getUnhealthThresshold(), hcp.isRevoked());
                index++;
                if (index == MAX_HEALTHCHECK_POLICIES) {
                    break;
                }
            }

            if (index == 0) {
                this.healthCheckPolicies = null;
            }
        }

        this.sslCert = sslCert;
        this.lbProtocol = lbProtocol;
    }

    public LoadBalancerTO(final String uuid, final String srcIp, final int srcPort, final String protocol, final String algorithm, final boolean revoked,
                          final boolean alreadyAdded, final boolean inline, List<LbDestination> destinations, final Integer clientTimeout, final Integer serverTimeout) {
        if (destinations == null) {
            destinations = new ArrayList<>();
        }
        this.uuid = uuid;
        this.srcIp = srcIp;
        this.srcPort = srcPort;
        this.protocol = protocol;
        this.algorithm = algorithm;
        this.revoked = revoked;
        this.alreadyAdded = alreadyAdded;
        this.inline = inline;
        this.destinations = new DestinationTO[destinations.size()];
        this.stickinessPolicies = null;
        this.sslCert = null;
        this.lbProtocol = null;
        this.clientTimeout = clientTimeout;
        this.serverTimeout = serverTimeout;
        int i = 0;
        for (final LbDestination destination : destinations) {
            this.destinations[i++] = new DestinationTO(destination.getIpAddress(), destination.getDestinationPortStart(), destination.isRevoked(), false);
        }
    }

    protected LoadBalancerTO() {
    }

    public String getUuid() {
        return uuid;
    }

    public String getSrcIp() {
        return srcIp;
    }

    public int getSrcPort() {
        return srcPort;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getLbProtocol() {
        return lbProtocol;
    }

    public void setLbProtocol(final String lbProtocol) {
        this.lbProtocol = lbProtocol;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public boolean isAlreadyAdded() {
        return alreadyAdded;
    }

    public boolean isInline() {
        return inline;
    }

    public StickinessPolicyTO[] getStickinessPolicies() {
        return stickinessPolicies;
    }

    public HealthCheckPolicyTO[] getHealthCheckPolicies() {
        return healthCheckPolicies;
    }

    public DestinationTO[] getDestinations() {
        return destinations;
    }

    public LbSslCert getSslCert() {
        return this.sslCert;
    }

    public int getClientTimeout() {
        return clientTimeout;
    }

    public void setClientTimeout(final int clientTimeout) {
        this.clientTimeout = clientTimeout;
    }

    public int getServerTimeout() {
        return serverTimeout;
    }

    public void setServerTimeout(final int serverTimeout) {
        this.serverTimeout = serverTimeout;
    }

    public static class StickinessPolicyTO {
        private final String _methodName;
        private final List<Pair<String, String>> _paramsList;

        public StickinessPolicyTO(final String methodName, final List<Pair<String, String>> paramsList) {
            this._methodName = methodName;
            this._paramsList = paramsList;
        }

        public String getMethodName() {
            return _methodName;
        }

        public List<Pair<String, String>> getParams() {
            return _paramsList;
        }
    }

    public static class HealthCheckPolicyTO {
        private String pingPath;
        private String description;
        private int responseTime;
        private int healthcheckInterval;
        private int healthcheckThresshold;
        private int unhealthThresshold;
        private boolean revoke = false;

        public HealthCheckPolicyTO(final String pingPath, final String description, final int responseTime, final int healthcheckInterval, final int healthcheckThresshold, final
        int unhealthThresshold,
                                   final boolean revoke) {

            this.description = description;
            this.pingPath = pingPath;
            this.responseTime = responseTime;
            this.healthcheckInterval = healthcheckInterval;
            this.healthcheckThresshold = healthcheckThresshold;
            this.unhealthThresshold = unhealthThresshold;
            this.revoke = revoke;
        }

        public HealthCheckPolicyTO() {

        }

        public String getpingPath() {
            return pingPath;
        }

        public String getDescription() {
            return description;
        }

        public int getResponseTime() {
            return responseTime;
        }

        public int getHealthcheckInterval() {
            return healthcheckInterval;
        }

        public int getHealthcheckThresshold() {
            return healthcheckThresshold;
        }

        public int getUnhealthThresshold() {
            return unhealthThresshold;
        }

        public void setRevoke(final boolean revoke) {
            this.revoke = revoke;
        }

        public boolean isRevoked() {
            return revoke;
        }
    }

    public static class DestinationTO {
        String destIp;
        int destPort;
        boolean revoked;
        boolean alreadyAdded;
        String monitorState;

        public DestinationTO(final String destIp, final int destPort, final boolean revoked, final boolean alreadyAdded) {
            this.destIp = destIp;
            this.destPort = destPort;
            this.revoked = revoked;
            this.alreadyAdded = alreadyAdded;
        }

        protected DestinationTO() {
        }

        public String getDestIp() {
            return destIp;
        }

        public int getDestPort() {
            return destPort;
        }

        public boolean isRevoked() {
            return revoked;
        }

        public boolean isAlreadyAdded() {
            return alreadyAdded;
        }

        public String getMonitorState() {
            return monitorState;
        }

        public void setMonitorState(final String state) {
            this.monitorState = state;
        }
    }
}
