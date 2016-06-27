package com.cloud.storage;

import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.utils.fsm.StateObject;
import org.apache.cloudstack.acl.ControlledEntity;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

import java.util.Date;

public interface Snapshot extends ControlledEntity, Identity, InternalIdentity, StateObject<Snapshot.State> {
    public static final long MANUAL_POLICY_ID = 0L;

    @Override
    long getAccountId();

    long getVolumeId();

    String getName();

    Date getCreated();

    Type getRecurringType();

    @Override
    State getState();

    HypervisorType getHypervisorType();

    boolean isRecursive();

    short getsnapshotType();

    public enum Type {
        MANUAL, RECURRING, TEMPLATE, HOURLY, DAILY, WEEKLY, MONTHLY;
        private int max = 8;

        public int getMax() {
            return max;
        }

        public void setMax(final int max) {
            this.max = max;
        }

        public boolean equals(final String snapshotType) {
            return this.toString().equalsIgnoreCase(snapshotType);
        }

        @Override
        public String toString() {
            return this.name();
        }
    }

    public enum State {
        Allocated, Creating, CreatedOnPrimary, BackingUp, BackedUp, Copying, Destroying, Destroyed,
        //it's a state, user can't see the snapshot from ui, while the snapshot may still exist on the storage
        Error;

        public boolean equals(final String status) {
            return this.toString().equalsIgnoreCase(status);
        }

        @Override
        public String toString() {
            return this.name();
        }

    }

    enum Event {
        CreateRequested, OperationNotPerformed, BackupToSecondary, BackedupToSecondary, DestroyRequested, CopyingRequested, OperationSucceeded, OperationFailed
    }
}
