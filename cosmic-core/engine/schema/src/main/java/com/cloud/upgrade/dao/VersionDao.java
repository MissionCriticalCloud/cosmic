package com.cloud.upgrade.dao;

import com.cloud.upgrade.dao.VersionVO.Step;
import com.cloud.utils.db.GenericDao;

public interface VersionDao extends GenericDao<VersionVO, Long> {
    VersionVO findByVersion(String version, Step step);

    String getCurrentVersion();
}
