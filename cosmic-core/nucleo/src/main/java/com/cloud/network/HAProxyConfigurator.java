package com.cloud.network;

import com.cloud.agent.api.routing.LoadBalancerConfigCommand;
import com.cloud.agent.api.to.LoadBalancerTO;
import com.cloud.agent.api.to.LoadBalancerTO.DestinationTO;
import com.cloud.agent.api.to.LoadBalancerTO.StickinessPolicyTO;
import com.cloud.agent.api.to.PortForwardingRuleTO;
import com.cloud.network.rules.LbStickinessMethod.StickinessMethodType;
import com.cloud.utils.Pair;
import com.cloud.utils.net.NetUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HAProxyConfigurator implements LoadBalancerConfigurator {

    private static final Logger s_logger = LoggerFactory.getLogger(HAProxyConfigurator.class);
    private static final String blankLine = "\t ";
    private static final String[] globalSection = {"global", "\tlog 127.0.0.1:3914   local0 warning", "\tmaxconn 4096", "\tmaxpipes 1024", "\tchroot /var/lib/haproxy",
            "\tuser haproxy", "\tgroup haproxy", "\tdaemon"};

    private static final String[] defaultsSection = {"defaults", "\tlog     global", "\tmode    tcp", "\toption  dontlognull", "\tretries 3", "\toption redispatch",
            "\toption forwardfor", "\toption forceclose", "\ttimeout connect    5000", "\ttimeout client     60000", "\ttimeout server     60000"};

    private static final String[] defaultListen = {"listen  vmops 0.0.0.0:9", "\toption transparent"};

    @Override
    public String[] generateConfiguration(final List<PortForwardingRuleTO> fwRules) {
        // Group the rules by publicip:publicport
        final Map<String, List<PortForwardingRuleTO>> pools = new HashMap<>();

        for (final PortForwardingRuleTO rule : fwRules) {
            final StringBuilder sb = new StringBuilder();
            final String poolName = sb.append(rule.getSrcIp().replace(".", "_")).append('-').append(rule.getSrcPortRange()[0]).toString();
            if (!rule.revoked()) {
                List<PortForwardingRuleTO> fwList = pools.get(poolName);
                if (fwList == null) {
                    fwList = new ArrayList<>();
                    pools.put(poolName, fwList);
                }
                fwList.add(rule);
            }
        }

        final List<String> result = new ArrayList<>();

        result.addAll(Arrays.asList(globalSection));
        result.add(blankLine);
        result.addAll(Arrays.asList(defaultsSection));
        result.add(blankLine);

        if (pools.isEmpty()) {
            // HAproxy cannot handle empty listen / frontend or backend, so add a dummy listener on port 9
            result.addAll(Arrays.asList(defaultListen));
        }
        result.add(blankLine);

        for (final Map.Entry<String, List<PortForwardingRuleTO>> e : pools.entrySet()) {
            final List<String> poolRules = getRulesForPool(e.getKey(), e.getValue());
            result.addAll(poolRules);
        }

        return result.toArray(new String[result.size()]);
    }

    private List<String> getRulesForPool(final String poolName, final List<PortForwardingRuleTO> fwRules) {
        final PortForwardingRuleTO firstRule = fwRules.get(0);
        final String publicIP = firstRule.getSrcIp();
        final int publicPort = firstRule.getSrcPortRange()[0];

        final List<String> result = new ArrayList<>();
        // Add line like this: "listen  65_37_141_30-80 65.37.141.30:80"
        StringBuilder sb = new StringBuilder();
        sb.append("listen ").append(poolName).append(" ").append(publicIP).append(":").append(publicPort);
        result.add(sb.toString());
        sb = new StringBuilder();
        // FIXME sb.append("\t").append("balance ").append(algorithm);
        result.add(sb.toString());
        if (publicPort == NetUtils.HTTP_PORT) {
            sb = new StringBuilder();
            sb.append("\t").append("mode http");
            result.add(sb.toString());
            sb = new StringBuilder();
            sb.append("\t").append("option httpclose");
            result.add(sb.toString());
        }
        int i = 0;
        for (final PortForwardingRuleTO rule : fwRules) {
            // Add line like this: "server  65_37_141_30-80_3 10.1.1.4:80 check"
            if (rule.revoked()) {
                continue;
            }
            sb = new StringBuilder();
            sb.append("\t")
              .append("server ")
              .append(poolName)
              .append("_")
              .append(Integer.toString(i++))
              .append(" ")
              .append(rule.getDstIp())
              .append(":")
              .append(rule.getDstPortRange()[0])
              .append(" check");
            result.add(sb.toString());
        }
        result.add(blankLine);
        return result;
    }

    @Override
    public String[] generateConfiguration(final LoadBalancerConfigCommand lbCmd) {
        final List<String> result = new ArrayList<>();
        final List<String> gSection = Arrays.asList(globalSection);
        // Note that this is overwritten on the String in the static ArrayList<String>
        gSection.set(2, "\tmaxconn " + lbCmd.maxconn);

        final String pipesLine = "\tmaxpipes " + Long.toString(Long.parseLong(lbCmd.maxconn) / 4);
        gSection.set(3, pipesLine);
        if (s_logger.isDebugEnabled()) {
            for (final String s : gSection) {
                s_logger.debug("global section: " + s);
            }
        }
        result.addAll(gSection);

        result.add(blankLine);
        final List<String> dSection = Arrays.asList(defaultsSection);
        if (lbCmd.keepAliveEnabled) {
            dSection.set(7, "\tno option forceclose");
        }

        if (s_logger.isDebugEnabled()) {
            for (final String s : dSection) {
                s_logger.debug("default section: " + s);
            }
        }
        result.addAll(dSection);
        if (!lbCmd.lbStatsVisibility.equals("disabled")) {
            // new rule : listen admin_page guestip/link-local:8081
            if (lbCmd.lbStatsVisibility.equals("global")) {
                result.add(generateStatsRule(lbCmd, "stats_on_public", lbCmd.lbStatsPublicIP));
            } else if (lbCmd.lbStatsVisibility.equals("guest-network")) {
                result.add(generateStatsRule(lbCmd, "stats_on_guest", lbCmd.lbStatsGuestIP));
            } else if (lbCmd.lbStatsVisibility.equals("link-local")) {
                result.add(generateStatsRule(lbCmd, "stats_on_private", lbCmd.lbStatsPrivateIP));
            } else if (lbCmd.lbStatsVisibility.equals("all")) {
                result.add(generateStatsRule(lbCmd, "stats_on_public", lbCmd.lbStatsPublicIP));
                result.add(generateStatsRule(lbCmd, "stats_on_guest", lbCmd.lbStatsGuestIP));
                result.add(generateStatsRule(lbCmd, "stats_on_private", lbCmd.lbStatsPrivateIP));
            } else {
                // Stats will be available on the default http serving port, no special stats port
                final StringBuilder subRule =
                        new StringBuilder("\tstats enable\n\tstats uri     ").append(lbCmd.lbStatsUri)
                                                                             .append("\n\tstats realm   Haproxy\\ Statistics\n\tstats auth    ")
                                                                             .append(lbCmd.lbStatsAuth);
                result.add(subRule.toString());
            }
        }
        result.add(blankLine);
        boolean has_listener = false;
        for (final LoadBalancerTO lbTO : lbCmd.getLoadBalancers()) {
            if (lbTO.isRevoked()) {
                continue;
            }
            final List<String> poolRules = getRulesForPool(lbTO, lbCmd.keepAliveEnabled);
            result.addAll(poolRules);
            has_listener = true;
        }
        result.add(blankLine);
        if (!has_listener) {
            // HAproxy cannot handle empty listen / frontend or backend, so add a dummy listener on port 9
            result.addAll(Arrays.asList(defaultListen));
        }
        return result.toArray(new String[result.size()]);
    }

    private String generateStatsRule(final LoadBalancerConfigCommand lbCmd, final String ruleName, final String statsIp) {
        final StringBuilder rule = new StringBuilder("\nlisten ").append(ruleName).append(" ").append(statsIp).append(":").append(lbCmd.lbStatsPort);
        if (!lbCmd.keepAliveEnabled) {
            s_logger.info("Haproxy mode http enabled");
            rule.append("\n\tmode http\n\toption httpclose");
        }
        rule.append("\n\tstats enable\n\tstats uri     ")
            .append(lbCmd.lbStatsUri)
            .append("\n\tstats realm   Haproxy\\ Statistics\n\tstats auth    ")
            .append(lbCmd.lbStatsAuth);
        rule.append("\n");
        final String result = rule.toString();
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Haproxystats rule: " + result);
        }
        return result;
    }

    private List<String> getRulesForPool(final LoadBalancerTO lbTO, final boolean keepAliveEnabled) {
        StringBuilder sb = new StringBuilder();
        final String poolName = sb.append(lbTO.getSrcIp().replace(".", "_")).append('-').append(lbTO.getSrcPort()).toString();
        final String publicIP = lbTO.getSrcIp();
        final int publicPort = lbTO.getSrcPort();
        final String algorithm = lbTO.getAlgorithm();

        final List<String> result = new ArrayList<>();
        // Add line like this: "listen  65_37_141_30-80 65.37.141.30:80"
        sb = new StringBuilder();
        sb.append("listen ").append(poolName).append(" ").append(publicIP).append(":").append(publicPort);
        result.add(sb.toString());
        sb = new StringBuilder();
        sb.append("\t").append("balance ").append(algorithm);
        sb.append("\n\t").append("timeout client ").append(lbTO.getClientTimeout());
        sb.append("\n\t").append("timeout server ").append(lbTO.getServerTimeout());
        result.add(sb.toString());

        int i = 0;
        Boolean destsAvailable = false;
        final String stickinessSubRule = getLbSubRuleForStickiness(lbTO);
        final List<String> dstSubRule = new ArrayList<>();
        final List<String> dstWithCookieSubRule = new ArrayList<>();
        for (final DestinationTO dest : lbTO.getDestinations()) {
            // Add line like this: "server  65_37_141_30-80_3 10.1.1.4:80 check"
            if (dest.isRevoked()) {
                continue;
            }
            sb = new StringBuilder();
            sb.append("\t")
              .append("server ")
              .append(poolName)
              .append("_")
              .append(Integer.toString(i++))
              .append(" ")
              .append(dest.getDestIp())
              .append(":")
              .append(dest.getDestPort())
              .append(" check");
            if (lbTO.getLbProtocol() != null && lbTO.getLbProtocol().equals("tcp-proxy")) {
                sb.append(" send-proxy");
            }
            dstSubRule.add(sb.toString());
            if (stickinessSubRule != null) {
                sb.append(" cookie ").append(dest.getDestIp().replace(".", "_")).append('-').append(dest.getDestPort()).toString();
                dstWithCookieSubRule.add(sb.toString());
            }
            destsAvailable = true;
        }

        Boolean httpbasedStickiness = false;
        // Attach stickiness sub rule only if the destinations are available
        if (stickinessSubRule != null && destsAvailable == true) {
            for (final StickinessPolicyTO stickinessPolicy : lbTO.getStickinessPolicies()) {
                if (stickinessPolicy == null) {
                    continue;
                }
                if (StickinessMethodType.LBCookieBased.getName().equalsIgnoreCase(stickinessPolicy.getMethodName()) ||
                        StickinessMethodType.AppCookieBased.getName().equalsIgnoreCase(stickinessPolicy.getMethodName())) {
                    httpbasedStickiness = true;
                }
            }
            if (httpbasedStickiness) {
                result.addAll(dstWithCookieSubRule);
            } else {
                result.addAll(dstSubRule);
            }
            result.add(stickinessSubRule);
        } else {
            result.addAll(dstSubRule);
        }
        if (stickinessSubRule != null && !destsAvailable) {
            s_logger.warn("Haproxy stickiness policy for lb rule: " + lbTO.getSrcIp() + ":" + lbTO.getSrcPort() + ": Not Applied, cause:  backends are unavailable");
        }
        if (publicPort == NetUtils.HTTP_PORT && !keepAliveEnabled || httpbasedStickiness) {
            sb = new StringBuilder();
            sb.append("\t").append("mode http");
            result.add(sb.toString());
            sb = new StringBuilder();
            sb.append("\t").append("option httpclose");
            result.add(sb.toString());
        }

        result.add(blankLine);
        return result;
    }

    private String getLbSubRuleForStickiness(final LoadBalancerTO lbTO) {
        int i = 0;

        if (lbTO.getStickinessPolicies() == null) {
            return null;
        }

        final StringBuilder sb = new StringBuilder();

        for (final StickinessPolicyTO stickinessPolicy : lbTO.getStickinessPolicies()) {
            if (stickinessPolicy == null) {
                continue;
            }
            final List<Pair<String, String>> paramsList = stickinessPolicy.getParams();
            i++;

            if (StickinessMethodType.LBCookieBased.getName().equalsIgnoreCase(stickinessPolicy.getMethodName())) {
                /* Default Values */
                String cookieName = null; // optional
                String mode = "insert "; // optional
                Boolean indirect = false; // optional
                Boolean nocache = false; // optional
                Boolean postonly = false; // optional
                StringBuilder domainSb = null; // optional

                for (final Pair<String, String> paramKV : paramsList) {
                    final String key = paramKV.first();
                    final String value = paramKV.second();
                    if ("cookie-name".equalsIgnoreCase(key)) {
                        cookieName = value;
                    }
                    if ("mode".equalsIgnoreCase(key)) {
                        mode = value;
                    }
                    if ("domain".equalsIgnoreCase(key)) {
                        if (domainSb == null) {
                            domainSb = new StringBuilder();
                        }
                        domainSb = domainSb.append("domain ");
                        domainSb.append(value).append(" ");
                    }
                    if ("indirect".equalsIgnoreCase(key)) {
                        indirect = true;
                    }
                    if ("nocache".equalsIgnoreCase(key)) {
                        nocache = true;
                    }
                    if ("postonly".equalsIgnoreCase(key)) {
                        postonly = true;
                    }
                }
                if (cookieName == null) {
                    // re-check all haproxy mandatory params
                    final StringBuilder tempSb = new StringBuilder();
                    String srcip = lbTO.getSrcIp();
                    if (srcip == null) {
                        srcip = "TESTCOOKIE";
                    }
                    tempSb.append("lbcooki_").append(srcip.hashCode()).append("_").append(lbTO.getSrcPort());
                    cookieName = tempSb.toString();
                }
                sb.append("\t").append("cookie ").append(cookieName).append(" ").append(mode).append(" ");
                if (indirect) {
                    sb.append("indirect ");
                }
                if (nocache) {
                    sb.append("nocache ");
                }
                if (postonly) {
                    sb.append("postonly ");
                }
                if (domainSb != null) {
                    sb.append(domainSb).append(" ");
                }
            } else if (StickinessMethodType.SourceBased.getName().equalsIgnoreCase(stickinessPolicy.getMethodName())) {
                // Default Values
                String tablesize = "200k"; // optional
                String expire = "30m"; // optional

                // Overwrite default values with the stick parameters
                for (final Pair<String, String> paramKV : paramsList) {
                    final String key = paramKV.first();
                    final String value = paramKV.second();
                    if ("tablesize".equalsIgnoreCase(key)) {
                        tablesize = value;
                    }
                    if ("expire".equalsIgnoreCase(key)) {
                        expire = value;
                    }
                }
                sb.append("\t").append("stick-table type ip size ").append(tablesize).append(" expire ").append(expire);
                sb.append("\n\t").append("stick on src");
            } else if (StickinessMethodType.AppCookieBased.getName().equalsIgnoreCase(stickinessPolicy.getMethodName())) {

                String cookieName = null; // optional
                String length = "52"; // optional
                String holdtime = "3h"; // optional
                String mode = null; // optional
                Boolean requestlearn = false; // optional
                Boolean prefix = false; // optional

                for (final Pair<String, String> paramKV : paramsList) {
                    final String key = paramKV.first();
                    final String value = paramKV.second();
                    if ("cookie-name".equalsIgnoreCase(key)) {
                        cookieName = value;
                    }
                    if ("length".equalsIgnoreCase(key)) {
                        length = value;
                    }
                    if ("holdtime".equalsIgnoreCase(key)) {
                        holdtime = value;
                    }
                    if ("mode".equalsIgnoreCase(key)) {
                        mode = value;
                    }
                    if ("request-learn".equalsIgnoreCase(key)) {
                        requestlearn = true;
                    }
                    if ("prefix".equalsIgnoreCase(key)) {
                        prefix = true;
                    }
                }
                if (cookieName == null) {
                    // Re-check all haproxy mandatory params
                    final StringBuilder tempSb = new StringBuilder();
                    String srcip = lbTO.getSrcIp();
                    if (srcip == null) {
                        srcip = "TESTCOOKIE";
                    }
                    tempSb.append("appcookie_").append(srcip.hashCode()).append("_").append(lbTO.getSrcPort());
                    cookieName = tempSb.toString();
                }
                sb.append("\t").append("appsession ").append(cookieName).append(" len ").append(length).append(" timeout ").append(holdtime).append(" ");
                if (prefix) {
                    sb.append("prefix ");
                }
                if (requestlearn) {
                    sb.append("request-learn").append(" ");
                }
                if (mode != null) {
                    sb.append("mode ").append(mode).append(" ");
                }
            } else {
                // Error is silently swallowed. Not supposed to reach here, validation of methods are done at the higher layer
                s_logger.warn("Haproxy stickiness policy for lb rule: " + lbTO.getSrcIp() + ":" + lbTO.getSrcPort() + ": Not Applied, cause:invalid method ");
                return null;
            }
        }
        if (i == 0) {
            return null;
        }
        return sb.toString();
    }

    @Override
    public String[][] generateFwRules(final LoadBalancerConfigCommand lbCmd) {
        final String[][] result = new String[3][];
        final Set<String> toAdd = new HashSet<>();
        final Set<String> toRemove = new HashSet<>();
        final Set<String> toStats = new HashSet<>();

        for (final LoadBalancerTO lbTO : lbCmd.getLoadBalancers()) {

            final StringBuilder sb = new StringBuilder();
            sb.append(lbTO.getSrcIp()).append(":");
            sb.append(lbTO.getSrcPort()).append(":");
            sb.append(lbTO.getProtocol()).append(":");
            for (final DestinationTO dest : lbTO.getDestinations()) {
                // Add dst entry like this: "10.1.1.4:80"
                if (dest.isRevoked()) {
                    continue;
                }
                sb.append(dest.getDestIp()).append(":");
                sb.append(dest.getDestPort()).append(":");
            }
            final String lbRuleEntry = sb.toString();
            if (!lbTO.isRevoked()) {
                toAdd.add(lbRuleEntry);
            } else {
                toRemove.add(lbRuleEntry);
            }
        }
        StringBuilder sb = new StringBuilder("");
        if (lbCmd.lbStatsVisibility.equals("guest-network")) {
            sb = new StringBuilder(lbCmd.lbStatsGuestIP).append(":").append(lbCmd.lbStatsPort).append(":").append(lbCmd.lbStatsSrcCidrs).append(":,");
        } else if (lbCmd.lbStatsVisibility.equals("link-local")) {
            sb = new StringBuilder(lbCmd.lbStatsPrivateIP).append(":").append(lbCmd.lbStatsPort).append(":").append(lbCmd.lbStatsSrcCidrs).append(":,");
        } else if (lbCmd.lbStatsVisibility.equals("global")) {
            sb = new StringBuilder(lbCmd.lbStatsPublicIP).append(":").append(lbCmd.lbStatsPort).append(":").append(lbCmd.lbStatsSrcCidrs).append(":,");
        } else if (lbCmd.lbStatsVisibility.equals("all")) {
            sb = new StringBuilder("0.0.0.0/0").append(":").append(lbCmd.lbStatsPort).append(":").append(lbCmd.lbStatsSrcCidrs).append(":,");
        }
        toStats.add(sb.toString());

        toRemove.removeAll(toAdd);
        result[ADD] = toAdd.toArray(new String[toAdd.size()]);
        result[REMOVE] = toRemove.toArray(new String[toRemove.size()]);
        result[STATS] = toStats.toArray(new String[toStats.size()]);

        return result;
    }
}
