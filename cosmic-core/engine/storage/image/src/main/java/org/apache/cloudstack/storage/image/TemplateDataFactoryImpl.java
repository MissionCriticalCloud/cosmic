package org.apache.cloudstack.storage.image;

import com.cloud.storage.DataStoreRole;
import com.cloud.storage.VMTemplateStoragePoolVO;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.storage.dao.VMTemplatePoolDao;
import org.apache.cloudstack.engine.subsystem.api.storage.DataObject;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreManager;
import org.apache.cloudstack.engine.subsystem.api.storage.TemplateDataFactory;
import org.apache.cloudstack.engine.subsystem.api.storage.TemplateInfo;
import org.apache.cloudstack.storage.datastore.db.TemplateDataStoreDao;
import org.apache.cloudstack.storage.datastore.db.TemplateDataStoreVO;
import org.apache.cloudstack.storage.image.store.TemplateObject;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TemplateDataFactoryImpl implements TemplateDataFactory {
    private static final Logger s_logger = LoggerFactory.getLogger(TemplateDataFactoryImpl.class);
    @Inject
    VMTemplateDao imageDataDao;
    @Inject
    DataStoreManager storeMgr;
    @Inject
    VMTemplatePoolDao templatePoolDao;
    @Inject
    TemplateDataStoreDao templateStoreDao;

    @Override
    public TemplateInfo getTemplate(final long templateId, final DataStore store) {
        final VMTemplateVO templ = imageDataDao.findById(templateId);
        if (store == null) {
            final TemplateObject tmpl = TemplateObject.getTemplate(templ, null);
            return tmpl;
        }
        // verify if the given input parameters are consistent with our db data.
        boolean found = false;
        if (store.getRole() == DataStoreRole.Primary) {
            final VMTemplateStoragePoolVO templatePoolVO = templatePoolDao.findByPoolTemplate(store.getId(), templateId);
            if (templatePoolVO != null) {
                found = true;
            }
        } else {
            final TemplateDataStoreVO templateStoreVO = templateStoreDao.findByStoreTemplate(store.getId(), templateId);
            if (templateStoreVO != null) {
                found = true;
            }
        }

        if (s_logger.isDebugEnabled()) {
            if (!found) {
                s_logger.debug("template " + templateId + " is not in store:" + store.getId() + ", type:" + store.getRole());
            } else {
                s_logger.debug("template " + templateId + " is already in store:" + store.getId() + ", type:" + store.getRole());
            }
        }

        final TemplateObject tmpl = TemplateObject.getTemplate(templ, store);
        return tmpl;
    }

    @Override
    public TemplateInfo getTemplate(final long templateId, final DataStoreRole storeRole) {
        final TemplateDataStoreVO tmplStore = templateStoreDao.findByTemplate(templateId, storeRole);
        DataStore store = null;
        if (tmplStore != null) {
            store = storeMgr.getDataStore(tmplStore.getDataStoreId(), storeRole);
        }
        return this.getTemplate(templateId, store);
    }

    @Override
    public TemplateInfo getTemplate(final long templateId, final DataStoreRole storeRole, final Long zoneId) {
        final TemplateDataStoreVO tmplStore = templateStoreDao.findByTemplateZone(templateId, zoneId, storeRole);
        DataStore store = null;
        if (tmplStore != null) {
            store = storeMgr.getDataStore(tmplStore.getDataStoreId(), storeRole);
        }
        return this.getTemplate(templateId, store);
    }

    @Override
    public TemplateInfo getReadyTemplateOnImageStore(final long templateId, final Long zoneId) {
        final TemplateDataStoreVO tmplStore = templateStoreDao.findByTemplateZoneReady(templateId, zoneId);
        if (tmplStore != null) {
            final DataStore store = storeMgr.getDataStore(tmplStore.getDataStoreId(), DataStoreRole.Image);
            return this.getTemplate(templateId, store);
        } else {
            return null;
        }
    }

    @Override
    public TemplateInfo getTemplate(final DataObject obj, final DataStore store) {
        final TemplateObject tmpObj = (TemplateObject) this.getTemplate(obj.getId(), store);
        // carry over url set in passed in data object, for copyTemplate case
        // where url is generated on demand and not persisted in DB.
        // need to think of a more generic way to pass these runtime information
        // carried through DataObject post 4.2
        final TemplateObject origTmpl = (TemplateObject) obj;
        tmpObj.setUrl(origTmpl.getUrl());
        return tmpObj;
    }

    @Override
    public TemplateInfo getReadyTemplateOnCache(final long templateId) {
        final TemplateDataStoreVO tmplStore = templateStoreDao.findReadyOnCache(templateId);
        if (tmplStore != null) {
            final DataStore store = storeMgr.getDataStore(tmplStore.getDataStoreId(), DataStoreRole.ImageCache);
            return getTemplate(templateId, store);
        } else {
            return null;
        }
    }

    @Override
    public List<TemplateInfo> listTemplateOnCache(final long templateId) {
        final List<TemplateDataStoreVO> cacheTmpls = templateStoreDao.listOnCache(templateId);
        final List<TemplateInfo> tmplObjs = new ArrayList<>();
        for (final TemplateDataStoreVO cacheTmpl : cacheTmpls) {
            final long storeId = cacheTmpl.getDataStoreId();
            final DataStore store = storeMgr.getDataStore(storeId, DataStoreRole.ImageCache);
            final TemplateInfo tmplObj = getTemplate(templateId, store);
            tmplObjs.add(tmplObj);
        }
        return tmplObjs;
    }
}
