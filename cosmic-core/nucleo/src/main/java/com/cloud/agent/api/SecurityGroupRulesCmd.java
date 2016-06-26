//

//

package com.cloud.agent.api;

import com.cloud.agent.api.LogLevel.Log4jLevel;
import com.cloud.utils.net.NetUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.DeflaterOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityGroupRulesCmd extends Command {
    private static final Logger s_logger = LoggerFactory.getLogger(SecurityGroupRulesCmd.class);
    String guestIp;
    String vmName;
    String guestMac;
    String signature;
    Long seqNum;
    Long vmId;
    Long msId;
    IpPortAndProto[] ingressRuleSet;
    IpPortAndProto[] egressRuleSet;
    private List<String> secIps;

    public SecurityGroupRulesCmd() {
        super();
    }

    public SecurityGroupRulesCmd(final String guestIp, final String guestMac, final String vmName, final Long vmId, final String signature, final Long seqNum, final
    IpPortAndProto[] ingressRuleSet,
                                 final IpPortAndProto[] egressRuleSet) {
        super();
        this.guestIp = guestIp;
        this.vmName = vmName;
        this.ingressRuleSet = ingressRuleSet;
        this.egressRuleSet = egressRuleSet;
        this.guestMac = guestMac;
        this.signature = signature;
        this.seqNum = seqNum;
        this.vmId = vmId;
        if (signature == null) {
            final String stringified = stringifyRules();
            this.signature = DigestUtils.md5Hex(stringified);
        }
    }

    public String stringifyRules() {
        final StringBuilder ruleBuilder = new StringBuilder();
        for (final SecurityGroupRulesCmd.IpPortAndProto ipPandP : getIngressRuleSet()) {
            ruleBuilder.append("I:").append(ipPandP.getProto()).append(":").append(ipPandP.getStartPort()).append(":").append(ipPandP.getEndPort()).append(":");
            for (final String cidr : ipPandP.getAllowedCidrs()) {
                ruleBuilder.append(cidr).append(",");
            }
            ruleBuilder.append("NEXT");
            ruleBuilder.append(" ");
        }
        for (final SecurityGroupRulesCmd.IpPortAndProto ipPandP : getEgressRuleSet()) {
            ruleBuilder.append("E:").append(ipPandP.getProto()).append(":").append(ipPandP.getStartPort()).append(":").append(ipPandP.getEndPort()).append(":");
            for (final String cidr : ipPandP.getAllowedCidrs()) {
                ruleBuilder.append(cidr).append(",");
            }
            ruleBuilder.append("NEXT");
            ruleBuilder.append(" ");
        }
        return ruleBuilder.toString();
    }

    public IpPortAndProto[] getIngressRuleSet() {
        return ingressRuleSet;
    }

    public void setIngressRuleSet(final IpPortAndProto[] ingressRuleSet) {
        this.ingressRuleSet = ingressRuleSet;
    }

    public IpPortAndProto[] getEgressRuleSet() {
        return egressRuleSet;
    }

    public void setEgressRuleSet(final IpPortAndProto[] egressRuleSet) {
        this.egressRuleSet = egressRuleSet;
    }

    public SecurityGroupRulesCmd(final String guestIp, final String guestMac, final String vmName, final Long vmId, final String signature, final Long seqNum, final
    IpPortAndProto[] ingressRuleSet,
                                 final IpPortAndProto[] egressRuleSet, final List<String> secIps) {
        super();
        this.guestIp = guestIp;
        this.vmName = vmName;
        this.ingressRuleSet = ingressRuleSet;
        this.egressRuleSet = egressRuleSet;
        this.guestMac = guestMac;
        this.signature = signature;
        this.seqNum = seqNum;
        this.vmId = vmId;
        if (signature == null) {
            final String stringified = stringifyRules();
            this.signature = DigestUtils.md5Hex(stringified);
        }
        this.secIps = secIps;
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }

    public String getGuestIp() {
        return guestIp;
    }

    public String getVmName() {
        return vmName;
    }

    public String getSecIpsString() {
        final StringBuilder sb = new StringBuilder();
        final List<String> ips = getSecIps();
        if (ips == null) {
            return "0:";
        } else {
            for (final String ip : ips) {
                sb.append(ip).append(":");
            }
        }
        return sb.toString();
    }

    public List<String> getSecIps() {
        return secIps;
    }

    public String stringifyCompressedRules() {
        final StringBuilder ruleBuilder = new StringBuilder();
        for (final SecurityGroupRulesCmd.IpPortAndProto ipPandP : getIngressRuleSet()) {
            ruleBuilder.append("I:").append(ipPandP.getProto()).append(":").append(ipPandP.getStartPort()).append(":").append(ipPandP.getEndPort()).append(":");
            for (final String cidr : ipPandP.getAllowedCidrs()) {
                //convert cidrs in the form "a.b.c.d/e" to "hexvalue of 32bit ip/e"
                ruleBuilder.append(compressCidr(cidr)).append(",");
            }
            ruleBuilder.append("NEXT");
            ruleBuilder.append(" ");
        }
        for (final SecurityGroupRulesCmd.IpPortAndProto ipPandP : getEgressRuleSet()) {
            ruleBuilder.append("E:").append(ipPandP.getProto()).append(":").append(ipPandP.getStartPort()).append(":").append(ipPandP.getEndPort()).append(":");
            for (final String cidr : ipPandP.getAllowedCidrs()) {
                //convert cidrs in the form "a.b.c.d/e" to "hexvalue of 32bit ip/e"
                ruleBuilder.append(compressCidr(cidr)).append(",");
            }
            ruleBuilder.append("NEXT");
            ruleBuilder.append(" ");
        }
        return ruleBuilder.toString();
    }

    //convert cidrs in the form "a.b.c.d/e" to "hexvalue of 32bit ip/e"
    private String compressCidr(final String cidr) {
        final String[] toks = cidr.split("/");
        final long ipnum = NetUtils.ip2Long(toks[0]);
        return Long.toHexString(ipnum) + "/" + toks[1];
    }

    /*
     * Compress the security group rules using zlib compression to allow the call to the hypervisor
     * to scale beyond 8k cidrs.
     */
    public String compressStringifiedRules() {
        final StringBuilder ruleBuilder = new StringBuilder();
        for (final SecurityGroupRulesCmd.IpPortAndProto ipPandP : getIngressRuleSet()) {
            ruleBuilder.append("I:").append(ipPandP.getProto()).append(":").append(ipPandP.getStartPort()).append(":").append(ipPandP.getEndPort()).append(":");
            for (final String cidr : ipPandP.getAllowedCidrs()) {
                ruleBuilder.append(cidr).append(",");
            }
            ruleBuilder.append("NEXT");
            ruleBuilder.append(" ");
        }
        for (final SecurityGroupRulesCmd.IpPortAndProto ipPandP : getEgressRuleSet()) {
            ruleBuilder.append("E:").append(ipPandP.getProto()).append(":").append(ipPandP.getStartPort()).append(":").append(ipPandP.getEndPort()).append(":");
            for (final String cidr : ipPandP.getAllowedCidrs()) {
                ruleBuilder.append(cidr).append(",");
            }
            ruleBuilder.append("NEXT");
            ruleBuilder.append(" ");
        }
        final String stringified = ruleBuilder.toString();
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            //Note : not using GZipOutputStream since that is for files
            //GZipOutputStream gives a different header, although the compression is the same
            final DeflaterOutputStream dzip = new DeflaterOutputStream(out);
            dzip.write(stringified.getBytes());
            dzip.close();
        } catch (final IOException e) {
            s_logger.warn("Exception while compressing security group rules");
            return null;
        }
        return Base64.encodeBase64String(out.toByteArray());
    }

    public String getSignature() {
        return signature;
    }

    public String getGuestMac() {
        return guestMac;
    }

    public Long getSeqNum() {
        return seqNum;
    }

    public Long getVmId() {
        return vmId;
    }

    public int getTotalNumCidrs() {
        //useful for logging
        int count = 0;
        for (final IpPortAndProto i : ingressRuleSet) {
            count += i.allowedCidrs.length;
        }
        for (final IpPortAndProto i : egressRuleSet) {
            count += i.allowedCidrs.length;
        }
        return count;
    }

    public Long getMsId() {
        return msId;
    }

    public void setMsId(final long msId) {
        this.msId = msId;
    }

    public static class IpPortAndProto {
        private String proto;
        private int startPort;
        private int endPort;
        @LogLevel(Log4jLevel.Trace)
        private String[] allowedCidrs;

        public IpPortAndProto() {
        }

        public IpPortAndProto(final String proto, final int startPort, final int endPort, final String[] allowedCidrs) {
            super();
            this.proto = proto;
            this.startPort = startPort;
            this.endPort = endPort;
            this.allowedCidrs = allowedCidrs;
        }

        public String[] getAllowedCidrs() {
            return allowedCidrs;
        }

        public void setAllowedCidrs(final String[] allowedCidrs) {
            this.allowedCidrs = allowedCidrs;
        }

        public String getProto() {
            return proto;
        }

        public int getStartPort() {
            return startPort;
        }

        public int getEndPort() {
            return endPort;
        }
    }
}
