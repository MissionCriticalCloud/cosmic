package com.cloud.alert.dao;

import com.cloud.alert.AlertVO;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.TransactionLegacy;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class AlertDaoImpl extends GenericDaoBase<AlertVO, Long> implements AlertDao {

    protected final SearchBuilder<AlertVO> AlertSearchByIdsAndType;

    public AlertDaoImpl() {
        AlertSearchByIdsAndType = createSearchBuilder();
        AlertSearchByIdsAndType.and("id", AlertSearchByIdsAndType.entity().getId(), Op.IN);
        AlertSearchByIdsAndType.and("type", AlertSearchByIdsAndType.entity().getType(), Op.EQ);
        AlertSearchByIdsAndType.and("createdDateB", AlertSearchByIdsAndType.entity().getCreatedDate(), Op.BETWEEN);
        AlertSearchByIdsAndType.and("createdDateL", AlertSearchByIdsAndType.entity().getCreatedDate(), Op.LTEQ);
        AlertSearchByIdsAndType.and("data_center_id", AlertSearchByIdsAndType.entity().getDataCenterId(), Op.EQ);
        AlertSearchByIdsAndType.and("archived", AlertSearchByIdsAndType.entity().getArchived(), Op.EQ);
        AlertSearchByIdsAndType.done();
    }

    @Override
    public AlertVO getLastAlert(final short type, final long dataCenterId, final Long podId, final Long clusterId) {
        final Filter searchFilter = new Filter(AlertVO.class, "createdDate", Boolean.FALSE, Long.valueOf(0), Long.valueOf(1));
        final SearchCriteria<AlertVO> sc = createSearchCriteria();

        sc.addAnd("type", SearchCriteria.Op.EQ, Short.valueOf(type));
        sc.addAnd("dataCenterId", SearchCriteria.Op.EQ, Long.valueOf(dataCenterId));
        sc.addAnd("archived", SearchCriteria.Op.EQ, false);
        if (podId != null) {
            sc.addAnd("podId", SearchCriteria.Op.EQ, podId);
        }
        if (clusterId != null) {
            sc.addAnd("clusterId", SearchCriteria.Op.EQ, clusterId);
        }

        final List<AlertVO> alerts = listBy(sc, searchFilter);
        if ((alerts != null) && !alerts.isEmpty()) {
            return alerts.get(0);
        }
        return null;
    }

    @Override
    public AlertVO getLastAlert(final short type, final long dataCenterId, final Long podId) {
        final Filter searchFilter = new Filter(AlertVO.class, "createdDate", Boolean.FALSE, Long.valueOf(0), Long.valueOf(1));
        final SearchCriteria<AlertVO> sc = createSearchCriteria();

        sc.addAnd("type", SearchCriteria.Op.EQ, Short.valueOf(type));
        sc.addAnd("dataCenterId", SearchCriteria.Op.EQ, Long.valueOf(dataCenterId));
        sc.addAnd("archived", SearchCriteria.Op.EQ, false);
        if (podId != null) {
            sc.addAnd("podId", SearchCriteria.Op.EQ, podId);
        }

        final List<AlertVO> alerts = listBy(sc, searchFilter);
        if ((alerts != null) && !alerts.isEmpty()) {
            return alerts.get(0);
        }
        return null;
    }

    @Override
    public boolean deleteAlert(final List<Long> ids, final String type, final Date startDate, final Date endDate, final Long zoneId) {
        final SearchCriteria<AlertVO> sc = AlertSearchByIdsAndType.create();

        if (ids != null) {
            sc.setParameters("id", ids.toArray(new Object[ids.size()]));
        }
        if (type != null) {
            sc.setParameters("type", type);
        }
        if (zoneId != null) {
            sc.setParameters("data_center_id", zoneId);
        }
        if (startDate != null && endDate != null) {
            sc.setParameters("createdDateB", startDate, endDate);
        } else if (endDate != null) {
            sc.setParameters("createdDateL", endDate);
        }
        sc.setParameters("archived", false);

        boolean result = true;
        final List<AlertVO> alerts = listBy(sc);
        if (ids != null && alerts.size() < ids.size()) {
            result = false;
            return result;
        }
        remove(sc);
        return result;
    }

    @Override
    public boolean archiveAlert(final List<Long> ids, final String type, final Date startDate, final Date endDate, final Long zoneId) {
        final SearchCriteria<AlertVO> sc = AlertSearchByIdsAndType.create();

        if (ids != null) {
            sc.setParameters("id", ids.toArray(new Object[ids.size()]));
        }
        if (type != null) {
            sc.setParameters("type", type);
        }
        if (zoneId != null) {
            sc.setParameters("data_center_id", zoneId);
        }
        if (startDate != null && endDate != null) {
            sc.setParameters("createdDateB", startDate, endDate);
        } else if (endDate != null) {
            sc.setParameters("createdDateL", endDate);
        }
        sc.setParameters("archived", false);

        boolean result = true;
        final List<AlertVO> alerts = listBy(sc);
        if (ids != null && alerts.size() < ids.size()) {
            result = false;
            return result;
        }
        if (alerts != null && !alerts.isEmpty()) {
            final TransactionLegacy txn = TransactionLegacy.currentTxn();
            txn.start();
            for (AlertVO alert : alerts) {
                alert = lockRow(alert.getId(), true);
                alert.setArchived(true);
                update(alert.getId(), alert);
                txn.commit();
            }
            txn.close();
        }
        return result;
    }

    @Override
    public List<AlertVO> listOlderAlerts(final Date oldTime) {
        if (oldTime == null) {
            return null;
        }
        final SearchCriteria<AlertVO> sc = createSearchCriteria();
        sc.addAnd("createdDate", SearchCriteria.Op.LT, oldTime);
        sc.addAnd("archived", SearchCriteria.Op.EQ, false);
        return listIncludingRemovedBy(sc, null);
    }
}
