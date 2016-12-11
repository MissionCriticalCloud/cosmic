package com.cloud.storage.dao;

import com.cloud.framework.config.ConfigKey;
import com.cloud.framework.config.ConfigKey.Scope;
import com.cloud.framework.config.ScopedConfigStorage;
import com.cloud.resourcedetail.ResourceDetailsDaoBase;
import com.cloud.storage.datastore.db.StoragePoolDetailVO;
import com.cloud.storage.datastore.db.StoragePoolDetailsDao;

public class StoragePoolDetailsDaoImpl extends ResourceDetailsDaoBase<StoragePoolDetailVO> implements StoragePoolDetailsDao, ScopedConfigStorage {

    public StoragePoolDetailsDaoImpl() {
    }

    @Override
    public Scope getScope() {
        return ConfigKey.Scope.StoragePool;
    }

    @Override
    public String getConfigValue(final long id, final ConfigKey<?> key) {
        final StoragePoolDetailVO vo = findDetail(id, key.key());
        return vo == null ? null : vo.getValue();
    }

    @Override
    public void addDetail(final long resourceId, final String key, final String value, final boolean display) {
        super.addDetail(new StoragePoolDetailVO(resourceId, key, value, display));
    }
}
