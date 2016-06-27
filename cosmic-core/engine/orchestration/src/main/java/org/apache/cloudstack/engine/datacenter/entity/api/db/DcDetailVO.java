package org.apache.cloudstack.engine.datacenter.entity.api.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "data_center_details")
public class DcDetailVO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "dc_id")
    private long dcId;

    @Column(name = "name")
    private String name;

    @Column(name = "value")
    private String value;

    protected DcDetailVO() {
    }

    public DcDetailVO(final long dcId, final String name, final String value) {
        this.dcId = dcId;
        this.name = name;
        this.value = value;
    }

    public long getDcId() {
        return dcId;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public long getId() {
        return id;
    }
}
