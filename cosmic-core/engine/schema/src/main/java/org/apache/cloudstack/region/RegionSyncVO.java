package org.apache.cloudstack.region;

import com.cloud.utils.db.GenericDao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "region_sync")
public class RegionSyncVO implements RegionSync {

    @Column(name = "processed")
    boolean processed;
    @Id
    @Column(name = "id")
    private long id;
    @Column(name = "region_id")
    private int regionId;
    @Column(name = "api")
    private String api;
    @Column(name = GenericDao.CREATED_COLUMN)
    private Date createDate;

    public RegionSyncVO() {
    }

    public RegionSyncVO(final int regionId, final String api) {
        this.regionId = regionId;
        this.api = api;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(final boolean processed) {
        this.processed = processed;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public int getRegionId() {
        return regionId;
    }

    public void setRegionId(final int regionId) {
        this.regionId = regionId;
    }

    @Override
    public String getApi() {
        return api;
    }

    public void setApi(final String api) {
        this.api = api;
    }

    @Override
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(final Date createDate) {
        this.createDate = createDate;
    }
}
