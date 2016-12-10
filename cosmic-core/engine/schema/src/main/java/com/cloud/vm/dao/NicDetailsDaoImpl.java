package com.cloud.vm.dao;

import com.cloud.resourcedetail.ResourceDetailsDaoBase;
import com.cloud.vm.NicDetailVO;

import org.springframework.stereotype.Component;

@Component
public class NicDetailsDaoImpl extends ResourceDetailsDaoBase<NicDetailVO> implements NicDetailsDao {

    @Override
    public void addDetail(final long resourceId, final String key, final String value, final boolean display) {
        super.addDetail(new NicDetailVO(resourceId, key, value, display));
    }
}
