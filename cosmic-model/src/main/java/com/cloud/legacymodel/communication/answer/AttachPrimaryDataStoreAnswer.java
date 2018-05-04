package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.communication.command.Command;

public class AttachPrimaryDataStoreAnswer extends Answer {
    private String uuid;
    private long capacity;
    private long avail;

    public AttachPrimaryDataStoreAnswer(final Command cmd) {
        super(cmd);
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public long getCapacity() {
        return capacity;
    }

    public void setCapacity(final long capacity) {
        this.capacity = capacity;
    }

    public long getAvailable() {
        return avail;
    }

    public void setAvailable(final long avail) {
        this.avail = avail;
    }
}
