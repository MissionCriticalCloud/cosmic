package com.cloud.network.dao;

import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "op_networks")
public class NetworkOpVO implements InternalIdentity {

    @Id
    @Column(name = "id")
    long id;

    @Column(name = "nics_count")
    int activeNicsCount;

    @Column(name = "gc")
    boolean garbageCollected;

    @Column(name = "check_for_gc")
    boolean checkForGc;

    protected NetworkOpVO() {
    }

    public NetworkOpVO(final long id, final boolean gc) {
        this.id = id;
        this.garbageCollected = gc;
        this.checkForGc = gc;
        this.activeNicsCount = 0;
    }

    @Override
    public long getId() {
        return id;
    }

    public long getActiveNicsCount() {
        return activeNicsCount;
    }

    public void setActiveNicsCount(final int number) {
        activeNicsCount += number;
    }

    public boolean isGarbageCollected() {
        return garbageCollected;
    }

    public boolean isCheckForGc() {
        return checkForGc;
    }

    public void setCheckForGc(final boolean check) {
        checkForGc = check;
    }
}
