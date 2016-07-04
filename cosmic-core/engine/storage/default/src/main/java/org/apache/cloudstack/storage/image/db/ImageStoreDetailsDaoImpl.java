package org.apache.cloudstack.storage.image.db;

import com.cloud.utils.crypt.DBEncryptionUtil;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.TransactionLegacy;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.storage.datastore.db.ImageStoreDetailVO;
import org.apache.cloudstack.storage.datastore.db.ImageStoreDetailsDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class ImageStoreDetailsDaoImpl extends GenericDaoBase<ImageStoreDetailVO, Long> implements ImageStoreDetailsDao {

    protected final SearchBuilder<ImageStoreDetailVO> storeSearch;

    protected ImageStoreDetailsDaoImpl() {
        super();
        storeSearch = createSearchBuilder();
        storeSearch.and("store", storeSearch.entity().getStoreId(), SearchCriteria.Op.EQ);
        storeSearch.done();
    }

    @Override
    public void update(final long storeId, final Map<String, String> details) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        final SearchCriteria<ImageStoreDetailVO> sc = storeSearch.create();
        sc.setParameters("store", storeId);

        txn.start();
        expunge(sc);
        for (final Map.Entry<String, String> entry : details.entrySet()) {
            final ImageStoreDetailVO detail = new ImageStoreDetailVO(storeId, entry.getKey(), entry.getValue());
            persist(detail);
        }
        txn.commit();
    }

    @Override
    public Map<String, String> getDetails(final long storeId) {
        final SearchCriteria<ImageStoreDetailVO> sc = storeSearch.create();
        sc.setParameters("store", storeId);

        final List<ImageStoreDetailVO> details = listBy(sc);
        final Map<String, String> detailsMap = new HashMap<>();
        for (final ImageStoreDetailVO detail : details) {
            final String name = detail.getName();
            String value = detail.getValue();
            if (name.equals(ApiConstants.KEY) || name.equals(ApiConstants.S3_SECRET_KEY)) {
                value = DBEncryptionUtil.decrypt(value);
            }
            detailsMap.put(name, value);
        }

        return detailsMap;
    }

    @Override
    public void deleteDetails(final long storeId) {
        final SearchCriteria<ImageStoreDetailVO> sc = storeSearch.create();
        sc.setParameters("store", storeId);

        final List<ImageStoreDetailVO> results = search(sc, null);
        for (final ImageStoreDetailVO result : results) {
            remove(result.getId());
        }
    }
}
