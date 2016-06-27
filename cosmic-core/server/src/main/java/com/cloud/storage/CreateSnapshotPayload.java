package com.cloud.storage;

import com.cloud.user.Account;

public class CreateSnapshotPayload {
    private Long snapshotPolicyId;
    private Long snapshotId;
    private Account account;
    private boolean quiescevm;

    public Long getSnapshotPolicyId() {
        return snapshotPolicyId;
    }

    public void setSnapshotPolicyId(final Long snapshotPolicyId) {
        this.snapshotPolicyId = snapshotPolicyId;
    }

    public Long getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(final Long snapshotId) {
        this.snapshotId = snapshotId;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(final Account account) {
        this.account = account;
    }

    public boolean getQuiescevm() {
        return this.quiescevm;
    }

    public void setQuiescevm(final boolean quiescevm) {
        this.quiescevm = quiescevm;
    }
}
