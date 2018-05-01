package com.cloud.storage.image.datastore;

import com.cloud.api.ApiConstants;
import com.cloud.legacymodel.exceptions.CloudRuntimeException;
import com.cloud.legacymodel.exceptions.InvalidParameterValueException;
import com.cloud.storage.DataStoreRole;
import com.cloud.storage.ScopeType;
import com.cloud.storage.datastore.db.ImageStoreDao;
import com.cloud.storage.datastore.db.ImageStoreDetailVO;
import com.cloud.storage.datastore.db.ImageStoreDetailsDao;
import com.cloud.storage.datastore.db.ImageStoreVO;
import com.cloud.storage.datastore.db.SnapshotDataStoreDao;
import com.cloud.utils.crypt.DBEncryptionUtil;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class ImageStoreHelper {
    @Inject
    ImageStoreDao imageStoreDao;
    @Inject
    ImageStoreDetailsDao imageStoreDetailsDao;
    @Inject
    SnapshotDataStoreDao snapshotStoreDao;

    public ImageStoreVO createImageStore(final Map<String, Object> params, final Map<String, String> details) {
        ImageStoreVO store = imageStoreDao.findByName((String) params.get("name"));
        if (store != null) {
            return store;
        }
        store = new ImageStoreVO();
        store.setProtocol((String) params.get("protocol"));
        store.setProviderName((String) params.get("providerName"));
        store.setScope((ScopeType) params.get("scope"));
        store.setDataCenterId((Long) params.get("zoneId"));
        final String uuid = (String) params.get("uuid");
        if (uuid != null) {
            store.setUuid(uuid);
        } else {
            store.setUuid(UUID.randomUUID().toString());
        }
        store.setUrl((String) params.get("url"));
        store.setName((String) params.get("name"));
        if (store.getName() == null) {
            store.setName(store.getUuid());
        }

        store.setRole((DataStoreRole) params.get("role"));

        if ("cifs".equalsIgnoreCase((String) params.get("protocol")) && details != null) {
            final String user = details.get("user");
            String password = details.get("password");
            final String domain = details.get("domain");
            String updatedPath = (String) params.get("url");

            if (user == null || password == null) {
                final String errMsg = "Missing cifs user and password details. Add them as details parameter.";
                throw new InvalidParameterValueException(errMsg);
            } else {
                try {
                    password = DBEncryptionUtil.encrypt(URLEncoder.encode(password, "UTF-8"));
                    details.put("password", password);
                    updatedPath += "?user=" + user + "&password=" + password + "&domain=" + domain;
                } catch (final UnsupportedEncodingException e) {
                    throw new CloudRuntimeException("Error while generating the cifs url. " + e.getMessage());
                }
                store.setUrl(updatedPath);
            }
        }

        store = imageStoreDao.persist(store);

        // persist details
        if (details != null) {
            final Iterator<String> keyIter = details.keySet().iterator();
            while (keyIter.hasNext()) {
                final String key = keyIter.next().toString();
                final ImageStoreDetailVO detail = new ImageStoreDetailVO();
                detail.setStoreId(store.getId());
                detail.setName(key);
                String value = details.get(key);
                // encrypt swift key or s3 secret key
                if (key.equals(ApiConstants.KEY)) {
                    value = DBEncryptionUtil.encrypt(value);
                }
                detail.setValue(value);
                imageStoreDetailsDao.persist(detail);
            }
        }
        return store;
    }
}
