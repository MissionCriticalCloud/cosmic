package com.cloud.user.dao;

import com.cloud.user.UserStatisticsVO;
import com.cloud.utils.db.GenericDao;

import java.util.Date;
import java.util.List;

public interface UserStatisticsDao extends GenericDao<UserStatisticsVO, Long> {
    UserStatisticsVO findBy(long accountId, long dcId, long networkId, String publicIp, Long deviceId, String deviceType);

    UserStatisticsVO lock(long accountId, long dcId, long networkId, String publicIp, Long hostId, String deviceType);

    List<UserStatisticsVO> listBy(long accountId);

    List<UserStatisticsVO> listActiveAndRecentlyDeleted(Date minRemovedDate, int startIndex, int limit);

    List<UserStatisticsVO> listUpdatedStats();
}
