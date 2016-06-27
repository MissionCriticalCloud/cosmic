package com.cloud.info;

public class RunningHostCountInfo {

    private long dcId;
    private String hostType;
    private int count;

    public long getDcId() {
        return dcId;
    }

    public void setDcId(final long dcId) {
        this.dcId = dcId;
    }

    public String getHostType() {
        return hostType;
    }

    public void setHostType(final String hostType) {
        this.hostType = hostType;
    }

    public int getCount() {
        return count;
    }

    public void setCount(final int count) {
        this.count = count;
    }
}
