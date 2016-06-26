package com.cloud.storage.dao;

import com.cloud.storage.GuestOSVO;
import com.cloud.utils.db.GenericDao;

public interface GuestOSDao extends GenericDao<GuestOSVO, Long> {

    GuestOSVO listByDisplayName(String displayName);
}
