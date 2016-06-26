package com.cloud.cluster;

public class ClusterServiceRequestPdu extends ClusterServicePdu {

    private String responseResult;
    private long startTick;
    private long timeout;

    public ClusterServiceRequestPdu() {
        startTick = System.currentTimeMillis();
        timeout = -1;
        setPduType(PDU_TYPE_REQUEST);
    }

    public String getResponseResult() {
        return responseResult;
    }

    public void setResponseResult(final String responseResult) {
        this.responseResult = responseResult;
    }

    public long getStartTick() {
        return startTick;
    }

    public void setStartTick(final long startTick) {
        this.startTick = startTick;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(final long timeout) {
        this.timeout = timeout;
    }
}
