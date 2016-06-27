//

//

package com.cloud.agent.api;

public class SecurityGroupRuleAnswer extends Answer {
    Long logSequenceNumber = null;
    Long vmId = null;
    FailureReason reason = FailureReason.NONE;

    protected SecurityGroupRuleAnswer() {
    }

    public SecurityGroupRuleAnswer(final SecurityGroupRulesCmd cmd) {
        super(cmd);
        this.logSequenceNumber = cmd.getSeqNum();
        this.vmId = cmd.getVmId();
    }

    public SecurityGroupRuleAnswer(final SecurityGroupRulesCmd cmd, final boolean result, final String detail) {
        super(cmd, result, detail);
        this.logSequenceNumber = cmd.getSeqNum();
        this.vmId = cmd.getVmId();
        reason = FailureReason.PROGRAMMING_FAILED;
    }

    public SecurityGroupRuleAnswer(final SecurityGroupRulesCmd cmd, final boolean result, final String detail, final FailureReason r) {
        super(cmd, result, detail);
        this.logSequenceNumber = cmd.getSeqNum();
        this.vmId = cmd.getVmId();
        reason = r;
    }

    public Long getLogSequenceNumber() {
        return logSequenceNumber;
    }

    public Long getVmId() {
        return vmId;
    }

    public FailureReason getReason() {
        return reason;
    }

    public void setReason(final FailureReason reason) {
        this.reason = reason;
    }

    public static enum FailureReason {
        NONE, UNKNOWN, PROGRAMMING_FAILED, CANNOT_BRIDGE_FIREWALL
    }
}
