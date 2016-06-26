package com.cloud.storage.dao;

import com.cloud.storage.VMTemplateZoneVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface VMTemplateZoneDao extends GenericDao<VMTemplateZoneVO, Long> {
    public List<VMTemplateZoneVO> listByZoneId(long id);

    public List<VMTemplateZoneVO> listByTemplateId(long templateId);

    public VMTemplateZoneVO findByZoneTemplate(long zoneId, long templateId);

    public List<VMTemplateZoneVO> listByZoneTemplate(Long zoneId, long templateId);

    public void deletePrimaryRecordsForTemplate(long templateId);
}
