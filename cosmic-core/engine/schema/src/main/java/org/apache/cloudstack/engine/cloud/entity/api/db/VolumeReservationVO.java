package org.apache.cloudstack.engine.cloud.entity.api.db;

import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "volume_reservation")
public class VolumeReservationVO implements InternalIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "vm_reservation_id")
    private long vmReservationId;

    @Column(name = "vm_id")
    private long vmId;

    @Column(name = "volume_id")
    private long volumeId;

    @Column(name = "pool_id")
    private long poolId;

    /**
     * There should never be a public constructor for this class. Since it's
     * only here to define the table for the DAO class.
     */
    protected VolumeReservationVO() {
    }

    public VolumeReservationVO(final long vmId, final long volumeId, final long poolId, final long vmReservationId) {
        this.vmId = vmId;
        this.volumeId = volumeId;
        this.poolId = poolId;
        this.vmReservationId = vmReservationId;
    }

    @Override
    public long getId() {
        return id;
    }

    public long getVmId() {
        return vmId;
    }

    public long getVmReservationId() {
        return vmReservationId;
    }

    public long getVolumeId() {
        return volumeId;
    }

    public Long getPoolId() {
        return poolId;
    }
}
