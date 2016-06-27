package com.cloud.network.rules;

public interface LoadBalancerContainer {

    String getName();

    String getDescription();

    String getAlgorithm();

    String getLbProtocol();

    Scheme getScheme();

    public enum Scheme {
        Public, Internal
    }
}
