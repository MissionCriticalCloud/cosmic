package org.apache.cloudstack.storage.volume.db;

import org.apache.cloudstack.resourcedetail.ResourceDetailsDaoBase;
import org.apache.cloudstack.storage.datastore.db.PrimaryDataStoreDetailVO;
import org.apache.cloudstack.storage.datastore.db.PrimaryDataStoreDetailsDao;

import org.springframework.stereotype.Component;

@Component
public class PrimaryDataStoreDetailsDaoImpl extends ResourceDetailsDaoBase<PrimaryDataStoreDetailVO> implements PrimaryDataStoreDetailsDao {

    @Override
    public void addDetail(final long resourceId, final String key, final String value, final boolean display) {
        super.addDetail(new PrimaryDataStoreDetailVO(resourceId, key, value, display));
    }
}
