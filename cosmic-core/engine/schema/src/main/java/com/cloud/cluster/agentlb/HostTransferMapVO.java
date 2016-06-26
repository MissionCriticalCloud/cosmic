package com.cloud.cluster.agentlb;

import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "op_host_transfer")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class HostTransferMapVO implements InternalIdentity {

    @Id
    @Column(name = "id")
    private long id;
    @Column(name = "initial_mgmt_server_id")
    private long initialOwner;
    @Column(name = "future_mgmt_server_id")
    private long futureOwner;
    @Column(name = "state")
    private HostTransferState state;
    @Column(name = GenericDao.CREATED_COLUMN)
    private Date created;

    public HostTransferMapVO(final long hostId, final long initialOwner, final long futureOwner) {
        this.id = hostId;
        this.initialOwner = initialOwner;
        this.futureOwner = futureOwner;
        this.state = HostTransferState.TransferRequested;
    }

    protected HostTransferMapVO() {
    }

    public long getInitialOwner() {
        return initialOwner;
    }

    public void setInitialOwner(final long initialOwner) {
        this.initialOwner = initialOwner;
    }

    public long getFutureOwner() {
        return futureOwner;
    }

    public void setFutureOwner(final long futureOwner) {
        this.futureOwner = futureOwner;
    }

    public HostTransferState getState() {
        return state;
    }

    public void setState(final HostTransferState state) {
        this.state = state;
    }

    @Override
    public long getId() {
        return id;
    }

    public Date getCreated() {
        return created;
    }

    public enum HostTransferState {
        TransferRequested, TransferStarted
    }
}
