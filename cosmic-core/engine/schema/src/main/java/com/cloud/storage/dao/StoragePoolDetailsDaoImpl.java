package com.cloud.storage.dao;

import org.apache.cloudstack.framework.config.ConfigKey;
import org.apache.cloudstack.framework.config.ConfigKey.Scope;
import org.apache.cloudstack.framework.config.ScopedConfigStorage;
import org.apache.cloudstack.resourcedetail.ResourceDetailsDaoBase;
import org.apache.cloudstack.storage.datastore.db.StoragePoolDetailVO;
import org.apache.cloudstack.storage.datastore.db.StoragePoolDetailsDao;

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
