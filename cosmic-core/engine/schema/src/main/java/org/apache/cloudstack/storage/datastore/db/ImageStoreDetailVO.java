package org.apache.cloudstack.storage.datastore.db;

import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "image_store_details")
public class ImageStoreDetailVO implements InternalIdentity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    long id;

    @Column(name = "store_id")
    long storeId;

    @Column(name = "name")
    String name;

    @Column(name = "value")
    String value;

    public ImageStoreDetailVO() {
    }

    public ImageStoreDetailVO(final long storeId, final String name, final String value) {
        this.storeId = storeId;
        this.name = name;
        this.value = value;
    }

    @Override
    public long getId() {
        return id;
    }

    public long getStoreId() {
        return storeId;
    }

    public void setStoreId(final long storeId) {
        this.storeId = storeId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }
}
