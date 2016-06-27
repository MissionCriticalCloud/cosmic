package com.cloud.agent.api.to;

import java.util.List;

public class NicTO extends NetworkTO {
    int deviceId;
    Integer networkRateMbps;
    Integer networkRateMulticastMbps;
    boolean defaultNic;
    String nicUuid;
    List<String> nicSecIps;

    public NicTO() {
        super();
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(final int deviceId) {
        this.deviceId = deviceId;
    }

    public Integer getNetworkRateMbps() {
        return networkRateMbps;
    }

    public void setNetworkRateMbps(final Integer networkRateMbps) {
        this.networkRateMbps = networkRateMbps;
    }

    public Integer getNetworkRateMulticastMbps() {
        return networkRateMulticastMbps;
    }

    public boolean isDefaultNic() {
        return defaultNic;
    }

    public void setDefaultNic(final boolean defaultNic) {
        this.defaultNic = defaultNic;
    }

    @Override
    public String getUuid() {
        return nicUuid;
    }

    @Override
    public void setUuid(final String uuid) {
        this.nicUuid = uuid;
    }

    @Override
    public String toString() {
        return new StringBuilder("[Nic:").append(type).append("-").append(ip).append("-").append(broadcastUri).append("]").toString();
    }

    public List<String> getNicSecIps() {
        return nicSecIps;
    }

    public void setNicSecIps(final List<String> secIps) {
        this.nicSecIps = secIps;
    }

    public String getNetworkUuid() {
        return super.getUuid();
    }

    public void setNetworkUuid(final String uuid) {
        super.setUuid(uuid);
    }
}
