package com.cloud.network.security;

/**
 * Work related to security groups for a vm
 */
public interface SecurityGroupWork {

    Long getInstanceId();

    Long getLogsequenceNumber();

    public abstract void setLogsequenceNumber(Long logsequenceNumber);

    Step getStep();

    void setStep(Step step);

    public enum Step {
        Scheduled, Processing, Done, Error
    }
}
