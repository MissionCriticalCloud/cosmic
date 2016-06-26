package com.cloud.agent.api.to;

import com.cloud.network.as.AutoScalePolicy;
import com.cloud.network.as.AutoScaleVmGroup;
import com.cloud.network.as.AutoScaleVmProfile;
import com.cloud.network.as.Condition;
import com.cloud.network.as.Counter;
import com.cloud.network.lb.LoadBalancingRule.LbAutoScalePolicy;
import com.cloud.network.lb.LoadBalancingRule.LbAutoScaleVmGroup;
import com.cloud.network.lb.LoadBalancingRule.LbAutoScaleVmProfile;
import com.cloud.network.lb.LoadBalancingRule.LbCondition;
import com.cloud.network.lb.LoadBalancingRule.LbDestination;
import com.cloud.network.lb.LoadBalancingRule.LbHealthCheckPolicy;
import com.cloud.network.lb.LoadBalancingRule.LbSslCert;
import com.cloud.network.lb.LoadBalancingRule.LbStickinessPolicy;
import com.cloud.utils.Pair;

import java.io.Serializable;
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
    private AutoScaleVmGroupTO autoScaleVmGroupTO;

    public LoadBalancerTO(final String id, final String srcIp, final int srcPort, final String protocol, final String algorithm, final boolean revoked, final boolean
            alreadyAdded, final boolean inline,
                          final List<LbDestination> argDestinations, final List<LbStickinessPolicy> stickinessPolicies) {

        this(id, srcIp, srcPort, protocol, algorithm, revoked, alreadyAdded, inline, argDestinations, stickinessPolicies, null, null, null);
    }

    public LoadBalancerTO(final String id, final String srcIp, final int srcPort, final String protocol, final String algorithm, final boolean revoked, final boolean
            alreadyAdded, final boolean inline,
                          final List<LbDestination> argDestinations, final List<LbStickinessPolicy> stickinessPolicies, final List<LbHealthCheckPolicy> healthCheckPolicies,
                          final LbSslCert sslCert,
                          final String lbProtocol) {
        this(id, srcIp, srcPort, protocol, algorithm, revoked, alreadyAdded, inline, argDestinations);
        this.stickinessPolicies = null;
        this.healthCheckPolicies = null;
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

    public LoadBalancerTO(final String uuid, final String srcIp, final int srcPort, final String protocol, final String algorithm, final boolean revoked, final boolean
            alreadyAdded, final boolean inline,
                          List<LbDestination> destinations) {
        if (destinations == null) { // for autoscaleconfig destinations will be null;
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

    public AutoScaleVmGroupTO getAutoScaleVmGroupTO() {
        return autoScaleVmGroupTO;
    }

    public boolean isAutoScaleVmGroupTO() {
        return this.autoScaleVmGroupTO != null;
    }

    public void setAutoScaleVmGroupTO(final AutoScaleVmGroupTO autoScaleVmGroupTO) {
        this.autoScaleVmGroupTO = autoScaleVmGroupTO;
    }

    public LbSslCert getSslCert() {
        return this.sslCert;
    }

    public void setAutoScaleVmGroup(final LbAutoScaleVmGroup lbAutoScaleVmGroup) {
        final List<LbAutoScalePolicy> lbAutoScalePolicies = lbAutoScaleVmGroup.getPolicies();
        final List<AutoScalePolicyTO> autoScalePolicyTOs = new ArrayList<>(lbAutoScalePolicies.size());
        for (final LbAutoScalePolicy lbAutoScalePolicy : lbAutoScalePolicies) {
            final List<LbCondition> lbConditions = lbAutoScalePolicy.getConditions();
            final List<ConditionTO> conditionTOs = new ArrayList<>(lbConditions.size());
            for (final LbCondition lbCondition : lbConditions) {
                final Counter counter = lbCondition.getCounter();
                final CounterTO counterTO = new CounterTO(counter.getName(), counter.getSource().toString(), "" + counter.getValue());
                final Condition condition = lbCondition.getCondition();
                final ConditionTO conditionTO = new ConditionTO(condition.getThreshold(), condition.getRelationalOperator().toString(), counterTO);
                conditionTOs.add(conditionTO);
            }
            final AutoScalePolicy autoScalePolicy = lbAutoScalePolicy.getPolicy();
            autoScalePolicyTOs.add(new AutoScalePolicyTO(autoScalePolicy.getId(), autoScalePolicy.getDuration(), autoScalePolicy.getQuietTime(),
                    autoScalePolicy.getAction(), conditionTOs, lbAutoScalePolicy.isRevoked()));
        }
        final LbAutoScaleVmProfile lbAutoScaleVmProfile = lbAutoScaleVmGroup.getProfile();
        final AutoScaleVmProfile autoScaleVmProfile = lbAutoScaleVmProfile.getProfile();

        final AutoScaleVmProfileTO autoScaleVmProfileTO =
                new AutoScaleVmProfileTO(lbAutoScaleVmProfile.getZoneId(), lbAutoScaleVmProfile.getDomainId(), lbAutoScaleVmProfile.getCsUrl(),
                        lbAutoScaleVmProfile.getAutoScaleUserApiKey(), lbAutoScaleVmProfile.getAutoScaleUserSecretKey(), lbAutoScaleVmProfile.getServiceOfferingId(),
                        lbAutoScaleVmProfile.getTemplateId(), lbAutoScaleVmProfile.getVmName(), lbAutoScaleVmProfile.getNetworkId(), autoScaleVmProfile.getOtherDeployParams(),
                        autoScaleVmProfile.getCounterParams(), autoScaleVmProfile.getDestroyVmGraceperiod());

        final AutoScaleVmGroup autoScaleVmGroup = lbAutoScaleVmGroup.getVmGroup();
        autoScaleVmGroupTO =
                new AutoScaleVmGroupTO(autoScaleVmGroup.getUuid(), autoScaleVmGroup.getMinMembers(), autoScaleVmGroup.getMaxMembers(), autoScaleVmGroup.getMemberPort(),
                        autoScaleVmGroup.getInterval(), autoScalePolicyTOs, autoScaleVmProfileTO, autoScaleVmGroup.getState(), lbAutoScaleVmGroup.getCurrentState());
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

    public static class CounterTO implements Serializable {
        private static final long serialVersionUID = 2L;
        private final String name;
        private final String source;
        private final String value;

        public CounterTO(final String name, final String source, final String value) {
            this.name = name;
            this.source = source;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getSource() {
            return source;
        }

        public String getValue() {
            return value;
        }
    }

    public static class ConditionTO implements Serializable {
        private static final long serialVersionUID = 2L;
        private final long threshold;
        private final String relationalOperator;
        private final CounterTO counter;

        public ConditionTO(final long threshold, final String relationalOperator, final CounterTO counter) {
            this.threshold = threshold;
            this.relationalOperator = relationalOperator;
            this.counter = counter;
        }

        public long getThreshold() {
            return threshold;
        }

        public String getRelationalOperator() {
            return relationalOperator;
        }

        public CounterTO getCounter() {
            return counter;
        }
    }

    public static class AutoScalePolicyTO implements Serializable {
        private static final long serialVersionUID = 2L;
        private final long id;
        private final int duration;
        private final int quietTime;
        private final List<ConditionTO> conditions;
        boolean revoked;
        private final String action;

        public AutoScalePolicyTO(final long id, final int duration, final int quietTime, final String action, final List<ConditionTO> conditions, final boolean revoked) {
            this.id = id;
            this.duration = duration;
            this.quietTime = quietTime;
            this.conditions = conditions;
            this.action = action;
            this.revoked = revoked;
        }

        public long getId() {
            return id;
        }

        public int getDuration() {
            return duration;
        }

        public int getQuietTime() {
            return quietTime;
        }

        public String getAction() {
            return action;
        }

        public boolean isRevoked() {
            return revoked;
        }

        public List<ConditionTO> getConditions() {
            return conditions;
        }
    }

    public static class AutoScaleVmProfileTO implements Serializable {
        private static final long serialVersionUID = 2L;
        private final String zoneId;
        private final String domainId;
        private final String serviceOfferingId;
        private final String templateId;
        private final String otherDeployParams;
        private final List<Pair<String, String>> counterParamList;
        private final Integer destroyVmGraceperiod;
        private final String cloudStackApiUrl;
        private final String autoScaleUserApiKey;
        private final String autoScaleUserSecretKey;
        private final String vmName;
        private final String networkId;

        public AutoScaleVmProfileTO(final String zoneId, final String domainId, final String cloudStackApiUrl, final String autoScaleUserApiKey, final String
                autoScaleUserSecretKey,
                                    final String serviceOfferingId, final String templateId, final String vmName, final String networkId, final String otherDeployParams, final
                                    List<Pair<String, String>>
                                            counterParamList,
                                    final Integer destroyVmGraceperiod) {
            this.zoneId = zoneId;
            this.domainId = domainId;
            this.serviceOfferingId = serviceOfferingId;
            this.templateId = templateId;
            this.otherDeployParams = otherDeployParams;
            this.counterParamList = counterParamList;
            this.destroyVmGraceperiod = destroyVmGraceperiod;
            this.cloudStackApiUrl = cloudStackApiUrl;
            this.autoScaleUserApiKey = autoScaleUserApiKey;
            this.autoScaleUserSecretKey = autoScaleUserSecretKey;
            this.vmName = vmName;
            this.networkId = networkId;
        }

        public String getZoneId() {
            return zoneId;
        }

        public String getDomainId() {
            return domainId;
        }

        public String getServiceOfferingId() {
            return serviceOfferingId;
        }

        public String getTemplateId() {
            return templateId;
        }

        public String getOtherDeployParams() {
            return otherDeployParams;
        }

        public List<Pair<String, String>> getCounterParamList() {
            return counterParamList;
        }

        public Integer getDestroyVmGraceperiod() {
            return destroyVmGraceperiod;
        }

        public String getCloudStackApiUrl() {
            return cloudStackApiUrl;
        }

        public String getAutoScaleUserApiKey() {
            return autoScaleUserApiKey;
        }

        public String getAutoScaleUserSecretKey() {
            return autoScaleUserSecretKey;
        }

        public String getVmName() {
            return vmName;
        }

        public String getNetworkId() {
            return networkId;
        }
    }

    public static class AutoScaleVmGroupTO implements Serializable {
        private static final long serialVersionUID = 2L;
        private final String uuid;
        private final int minMembers;
        private final int maxMembers;
        private final int memberPort;
        private final int interval;
        private final List<AutoScalePolicyTO> policies;
        private final AutoScaleVmProfileTO profile;
        private final String state;
        private final String currentState;

        AutoScaleVmGroupTO(final String uuid, final int minMembers, final int maxMembers, final int memberPort, final int interval, final List<AutoScalePolicyTO> policies, final
        AutoScaleVmProfileTO profile,
                           final String state, final String currentState) {
            this.uuid = uuid;
            this.minMembers = minMembers;
            this.maxMembers = maxMembers;
            this.memberPort = memberPort;
            this.interval = interval;
            this.policies = policies;
            this.profile = profile;
            this.state = state;
            this.currentState = currentState;
        }

        public String getUuid() {
            return uuid;
        }

        public int getMinMembers() {
            return minMembers;
        }

        public int getMaxMembers() {
            return maxMembers;
        }

        public int getMemberPort() {
            return memberPort;
        }

        public int getInterval() {
            return interval;
        }

        public List<AutoScalePolicyTO> getPolicies() {
            return policies;
        }

        public AutoScaleVmProfileTO getProfile() {
            return profile;
        }

        public String getState() {
            return state;
        }

        public String getCurrentState() {
            return currentState;
        }
    }
}
