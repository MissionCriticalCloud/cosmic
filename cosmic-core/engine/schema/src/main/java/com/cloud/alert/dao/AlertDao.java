package com.cloud.alert.dao;

import com.cloud.alert.AlertVO;
import com.cloud.utils.db.GenericDao;

import java.util.Date;
import java.util.List;

public interface AlertDao extends GenericDao<AlertVO, Long> {
    AlertVO getLastAlert(short type, long dataCenterId, Long podId, Long clusterId);

    // This is for backward compatibility
    AlertVO getLastAlert(short type, long dataCenterId, Long podId);

    public boolean deleteAlert(List<Long> ids, String type, Date startDate, Date endDate, Long zoneId);

    public boolean archiveAlert(List<Long> ids, String type, Date startDate, Date endDate, Long zoneId);

    public List<AlertVO> listOlderAlerts(Date oldTime);
}
