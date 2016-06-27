package com.cloud.user.dao;

import com.cloud.user.UserStatsLogVO;
import com.cloud.utils.db.GenericDaoBase;

import org.springframework.stereotype.Component;

@Component
public class UserStatsLogDaoImpl extends GenericDaoBase<UserStatsLogVO, Long> implements UserStatsLogDao {
    public UserStatsLogDaoImpl() {
    }
}
