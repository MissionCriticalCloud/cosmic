package com.cloud.dc.dao;

import com.cloud.api.ResourceDetail;
import com.cloud.dc.DataCenterDetailVO;
import com.cloud.framework.config.ConfigKey;
import com.cloud.framework.config.ConfigKey.Scope;
import com.cloud.framework.config.ScopedConfigStorage;
import com.cloud.resourcedetail.ResourceDetailsDaoBase;

public class DataCenterDetailsDaoImpl extends ResourceDetailsDaoBase<DataCenterDetailVO> implements DataCenterDetailsDao, ScopedConfigStorage {

    @Override
    public Scope getScope() {
        return ConfigKey.Scope.Zone;
    }

    @Override
    public String getConfigValue(final long id, final ConfigKey<?> key) {
        final ResourceDetail vo = findDetail(id, key.key());
        return vo == null ? null : vo.getValue();
    }

    @Override
    public void addDetail(final long resourceId, final String key, final String value, final boolean display) {
        super.addDetail(new DataCenterDetailVO(resourceId, key, value, display));
    }
}
