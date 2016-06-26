package org.apache.cloudstack.resourcedetail;

import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.TransactionLegacy;
import org.apache.cloudstack.api.ResourceDetail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ResourceDetailsDaoBase<R extends ResourceDetail> extends GenericDaoBase<R, Long> {
    private final SearchBuilder<R> AllFieldsSearch;

    public ResourceDetailsDaoBase() {
        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("resourceId", AllFieldsSearch.entity().getResourceId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("name", AllFieldsSearch.entity().getName(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("value", AllFieldsSearch.entity().getValue(), SearchCriteria.Op.EQ);
        // FIXME SnapshotDetailsVO doesn't have a display field
        if (_allAttributes.containsKey("display")) {
            AllFieldsSearch.and("display", AllFieldsSearch.entity().isDisplay(), SearchCriteria.Op.EQ);
        }
        AllFieldsSearch.done();
    }

    public List<R> findDetails(final String name, final String value, final Boolean display) {
        final SearchCriteria<R> sc = AllFieldsSearch.create();

        if (display != null) {
            sc.setParameters("display", display);
        }

        if (name != null) {
            sc.setParameters("name", name);
        }

        if (value != null) {
            sc.setParameters("value", value);
        }

        final List<R> results = search(sc, null);
        return results;
    }

    public Map<String, String> listDetailsKeyPairs(final long resourceId) {
        final SearchCriteria<R> sc = AllFieldsSearch.create();
        sc.setParameters("resourceId", resourceId);

        final List<R> results = search(sc, null);
        final Map<String, String> details = new HashMap<>(results.size());
        for (final R result : results) {
            details.put(result.getName(), result.getValue());
        }
        return details;
    }

    public List<R> listDetails(final long resourceId) {
        final SearchCriteria<R> sc = AllFieldsSearch.create();
        sc.setParameters("resourceId", resourceId);

        final List<R> results = search(sc, null);
        return results;
    }

    public void removeDetails(final long resourceId) {
        final SearchCriteria<R> sc = AllFieldsSearch.create();
        sc.setParameters("resourceId", resourceId);
        remove(sc);
    }

    public void removeDetail(final long resourceId, final String key) {
        if (key != null) {
            final SearchCriteria<R> sc = AllFieldsSearch.create();
            sc.setParameters("resourceId", resourceId);
            sc.setParameters("name", key);
            remove(sc);
        }
    }

    public void saveDetails(final List<R> details) {
        if (details.isEmpty()) {
            return;
        }
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        final SearchCriteria<R> sc = AllFieldsSearch.create();
        sc.setParameters("resourceId", details.get(0).getResourceId());
        expunge(sc);

        for (final R detail : details) {
            persist(detail);
        }

        txn.commit();
    }

    protected void addDetail(final R detail) {
        if (detail == null) {
            return;
        }
        final R existingDetail = findDetail(detail.getResourceId(), detail.getName());
        if (existingDetail != null) {
            remove(existingDetail.getId());
        }
        persist(detail);
    }

    public R findDetail(final long resourceId, final String name) {
        final SearchCriteria<R> sc = AllFieldsSearch.create();
        sc.setParameters("resourceId", resourceId);
        sc.setParameters("name", name);

        return findOneBy(sc);
    }

    public Map<String, String> listDetailsKeyPairs(final long resourceId, final boolean forDisplay) {
        final SearchCriteria<R> sc = AllFieldsSearch.create();
        sc.setParameters("resourceId", resourceId);
        sc.setParameters("display", forDisplay);

        final List<R> results = search(sc, null);
        final Map<String, String> details = new HashMap<>(results.size());
        for (final R result : results) {
            details.put(result.getName(), result.getValue());
        }
        return details;
    }

    public List<R> listDetails(final long resourceId, final boolean forDisplay) {
        final SearchCriteria<R> sc = AllFieldsSearch.create();
        sc.setParameters("resourceId", resourceId);
        sc.setParameters("display", forDisplay);

        final List<R> results = search(sc, null);
        return results;
    }
}
