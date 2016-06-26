package org.apache.cloudstack.storage.datastore.db;

import com.cloud.utils.db.GenericDao;

import java.util.Map;

public interface ImageStoreDetailsDao extends GenericDao<ImageStoreDetailVO, Long> {

    void update(long storeId, Map<String, String> details);

    Map<String, String> getDetails(long storeId);

    void deleteDetails(long storeId);
}
