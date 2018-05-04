package com.cloud.legacymodel.network;

public interface LoadBalancerContainer {

    String getName();

    String getDescription();

    String getAlgorithm();

    String getLbProtocol();

    int getServerTimeout();

    int getClientTimeout();

    Scheme getScheme();

    enum Scheme {
        Public
    }
}
