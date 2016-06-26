package org.apache.cloudstack.resourcedetail.dao;

import org.apache.cloudstack.resourcedetail.ResourceDetailsDaoBase;
import org.apache.cloudstack.resourcedetail.SnapshotPolicyDetailVO;

import org.springframework.stereotype.Component;

@Component
public class SnapshotPolicyDetailsDaoImpl extends ResourceDetailsDaoBase<SnapshotPolicyDetailVO> implements SnapshotPolicyDetailsDao {

    @Override
    public void addDetail(final long resourceId, final String key, final String value, final boolean display) {
        super.addDetail(new SnapshotPolicyDetailVO(resourceId, key, value));
    }
}
