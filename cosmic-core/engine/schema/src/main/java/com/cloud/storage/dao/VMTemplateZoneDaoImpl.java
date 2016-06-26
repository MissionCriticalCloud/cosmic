package com.cloud.storage.dao;

import com.cloud.storage.VMTemplateZoneVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.TransactionLegacy;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VMTemplateZoneDaoImpl extends GenericDaoBase<VMTemplateZoneVO, Long> implements VMTemplateZoneDao {
    public static final Logger s_logger = LoggerFactory.getLogger(VMTemplateZoneDaoImpl.class.getName());

    protected final SearchBuilder<VMTemplateZoneVO> ZoneSearch;
    protected final SearchBuilder<VMTemplateZoneVO> TemplateSearch;
    protected final SearchBuilder<VMTemplateZoneVO> ZoneTemplateSearch;

    public VMTemplateZoneDaoImpl() {
        ZoneSearch = createSearchBuilder();
        ZoneSearch.and("zone_id", ZoneSearch.entity().getZoneId(), SearchCriteria.Op.EQ);
        ZoneSearch.done();

        TemplateSearch = createSearchBuilder();
        TemplateSearch.and("template_id", TemplateSearch.entity().getTemplateId(), SearchCriteria.Op.EQ);
        TemplateSearch.done();

        ZoneTemplateSearch = createSearchBuilder();
        ZoneTemplateSearch.and("zone_id", ZoneTemplateSearch.entity().getZoneId(), SearchCriteria.Op.EQ);
        ZoneTemplateSearch.and("template_id", ZoneTemplateSearch.entity().getTemplateId(), SearchCriteria.Op.EQ);
        ZoneTemplateSearch.done();
    }

    @Override
    public List<VMTemplateZoneVO> listByZoneId(final long id) {
        final SearchCriteria<VMTemplateZoneVO> sc = ZoneSearch.create();
        sc.setParameters("zone_id", id);
        return listIncludingRemovedBy(sc);
    }

    @Override
    public List<VMTemplateZoneVO> listByTemplateId(final long templateId) {
        final SearchCriteria<VMTemplateZoneVO> sc = TemplateSearch.create();
        sc.setParameters("template_id", templateId);
        return listIncludingRemovedBy(sc);
    }

    @Override
    public VMTemplateZoneVO findByZoneTemplate(final long zoneId, final long templateId) {
        final SearchCriteria<VMTemplateZoneVO> sc = ZoneTemplateSearch.create();
        sc.setParameters("zone_id", zoneId);
        sc.setParameters("template_id", templateId);
        return findOneIncludingRemovedBy(sc);
    }

    @Override
    public List<VMTemplateZoneVO> listByZoneTemplate(final Long zoneId, final long templateId) {
        final SearchCriteria<VMTemplateZoneVO> sc = ZoneTemplateSearch.create();
        if (zoneId != null) {
            sc.setParameters("zone_id", zoneId);
        }
        sc.setParameters("template_id", templateId);
        return listBy(sc);
    }

    @Override
    public void deletePrimaryRecordsForTemplate(final long templateId) {
        final SearchCriteria<VMTemplateZoneVO> sc = TemplateSearch.create();
        sc.setParameters("template_id", templateId);
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        remove(sc);
        txn.commit();
    }
}
