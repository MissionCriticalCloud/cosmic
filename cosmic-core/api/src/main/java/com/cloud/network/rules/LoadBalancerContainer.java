package com.cloud.network.rules;

public interface LoadBalancerContainer {

    String getName();

    String getDescription();

    String getAlgorithm();

    String getLbProtocol();

    int getServerTimeout();

    int getClientTimeout();

    Scheme getScheme();

    public enum Scheme {
        Public
    }
}
