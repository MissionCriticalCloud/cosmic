package com.cloud.cluster;

public class ClusterServicePdu {
    public final static int PDU_TYPE_MESSAGE = 0;
    public final static int PDU_TYPE_REQUEST = 1;
    public final static int PDU_TYPE_RESPONSE = 2;
    private static long s_nextPduSequenceId = 1;
    private long sequenceId;
    private long ackSequenceId;
    private String sourcePeer;
    private String destPeer;
    private long agentId;
    private boolean stopOnError;
    private String jsonPackage;
    private int pduType = PDU_TYPE_MESSAGE;

    public ClusterServicePdu() {
        sequenceId = getNextPduSequenceId();
        ackSequenceId = 0;
        agentId = 0;
        stopOnError = false;
    }

    public synchronized long getNextPduSequenceId() {
        return s_nextPduSequenceId++;
    }

    public long getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(final long sequenceId) {
        this.sequenceId = sequenceId;
    }

    public long getAckSequenceId() {
        return ackSequenceId;
    }

    public void setAckSequenceId(final long ackSequenceId) {
        this.ackSequenceId = ackSequenceId;
    }

    public String getSourcePeer() {
        return sourcePeer;
    }

    public void setSourcePeer(final String sourcePeer) {
        this.sourcePeer = sourcePeer;
    }

    public String getDestPeer() {
        return destPeer;
    }

    public void setDestPeer(final String destPeer) {
        this.destPeer = destPeer;
    }

    public long getAgentId() {
        return agentId;
    }

    public void setAgentId(final long agentId) {
        this.agentId = agentId;
    }

    public boolean isStopOnError() {
        return stopOnError;
    }

    public void setStopOnError(final boolean stopOnError) {
        this.stopOnError = stopOnError;
    }

    public String getJsonPackage() {
        return jsonPackage;
    }

    public void setJsonPackage(final String jsonPackage) {
        this.jsonPackage = jsonPackage;
    }

    public int getPduType() {
        return pduType;
    }

    public void setPduType(final int pduType) {
        this.pduType = pduType;
    }
}
