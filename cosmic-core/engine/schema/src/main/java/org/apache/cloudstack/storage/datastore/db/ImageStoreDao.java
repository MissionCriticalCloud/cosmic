package org.apache.cloudstack.storage.datastore.db;

import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.engine.subsystem.api.storage.ZoneScope;

import java.util.List;

public interface ImageStoreDao extends GenericDao<ImageStoreVO, Long> {
    ImageStoreVO findByName(String name);

    List<ImageStoreVO> findByProvider(String provider);

    List<ImageStoreVO> findByScope(ZoneScope scope);

    List<ImageStoreVO> findRegionImageStores();

    List<ImageStoreVO> findImageCacheByScope(ZoneScope scope);

    List<ImageStoreVO> listImageStores();

    List<ImageStoreVO> listImageCacheStores();
}
