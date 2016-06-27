package com.cloud.agent.api.to;

import com.cloud.storage.Volume;

import java.util.Map;

public class DiskTO {
    public static final String CHAP_INITIATOR_USERNAME = "chapInitiatorUsername";
    public static final String CHAP_INITIATOR_SECRET = "chapInitiatorSecret";
    public static final String CHAP_TARGET_USERNAME = "chapTargetUsername";
    public static final String CHAP_TARGET_SECRET = "chapTargetSecret";
    public static final String MANAGED = "managed";
    public static final String IQN = "iqn";
    public static final String STORAGE_HOST = "storageHost";
    public static final String STORAGE_PORT = "storagePort";
    public static final String VOLUME_SIZE = "volumeSize";
    public static final String MOUNT_POINT = "mountpoint";
    public static final String PROTOCOL_TYPE = "protocoltype";
    public static final String PATH = "path";
    public static final String UUID = "uuid";

    private DataTO data;
    private Long diskSeq;
    private String path;
    private Volume.Type type;
    private Map<String, String> _details;

    public DiskTO() {

    }

    public DiskTO(final DataTO data, final Long diskSeq, final String path, final Volume.Type type) {
        this.data = data;
        this.diskSeq = diskSeq;
        this.path = path;
        this.type = type;
    }

    public DataTO getData() {
        return data;
    }

    public void setData(final DataTO data) {
        this.data = data;
    }

    public Long getDiskSeq() {
        return diskSeq;
    }

    public void setDiskSeq(final Long diskSeq) {
        this.diskSeq = diskSeq;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public Volume.Type getType() {
        return type;
    }

    public void setType(final Volume.Type type) {
        this.type = type;
    }

    public Map<String, String> getDetails() {
        return _details;
    }

    public void setDetails(final Map<String, String> details) {
        _details = details;
    }
}
