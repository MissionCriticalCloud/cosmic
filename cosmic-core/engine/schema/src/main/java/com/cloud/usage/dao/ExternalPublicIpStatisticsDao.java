package com.cloud.usage.dao;

import com.cloud.usage.ExternalPublicIpStatisticsVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface ExternalPublicIpStatisticsDao extends GenericDao<ExternalPublicIpStatisticsVO, Long> {

    ExternalPublicIpStatisticsVO lock(long accountId, long zoneId, String publicIpAddress);

    ExternalPublicIpStatisticsVO findBy(long accountId, long zoneId, String publicIpAddress);

    List<ExternalPublicIpStatisticsVO> listBy(long accountId, long zoneId);
}
