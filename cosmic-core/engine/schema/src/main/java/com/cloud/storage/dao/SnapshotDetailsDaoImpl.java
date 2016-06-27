package com.cloud.storage.dao;

import org.apache.cloudstack.resourcedetail.ResourceDetailsDaoBase;

public class SnapshotDetailsDaoImpl extends ResourceDetailsDaoBase<SnapshotDetailsVO> implements SnapshotDetailsDao {
    @Override
    public void addDetail(final long resourceId, final String key, final String value, final boolean display) {
        super.addDetail(new SnapshotDetailsVO(resourceId, key, value, display));
    }
}
