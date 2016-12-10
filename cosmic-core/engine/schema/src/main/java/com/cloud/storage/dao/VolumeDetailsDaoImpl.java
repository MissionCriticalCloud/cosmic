package com.cloud.storage.dao;

import com.cloud.resourcedetail.ResourceDetailsDaoBase;
import com.cloud.storage.VolumeDetailVO;

import org.springframework.stereotype.Component;

@Component
public class VolumeDetailsDaoImpl extends ResourceDetailsDaoBase<VolumeDetailVO> implements VolumeDetailsDao {

    @Override
    public void addDetail(final long resourceId, final String key, final String value, final boolean display) {
        super.addDetail(new VolumeDetailVO(resourceId, key, value, display));
    }
}
