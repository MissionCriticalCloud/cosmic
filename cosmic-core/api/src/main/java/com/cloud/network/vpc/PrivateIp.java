package com.cloud.network.vpc;

public interface PrivateIp {

    /**
     * @return
     */
    String getIpAddress();

    /**
     * @return
     */
    String getBroadcastUri();

    /**
     * @return
     */
    String getGateway();

    /**
     * @return
     */
    String getNetmask();

    /**
     * @return
     */
    String getMacAddress();

    long getNetworkId();

    boolean getSourceNat();
}
