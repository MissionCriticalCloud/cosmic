package com.cloud.network.dao;

import com.cloud.network.VpnUserVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface VpnUserDao extends GenericDao<VpnUserVO, Long> {
    List<VpnUserVO> listByAccount(Long accountId);

    VpnUserVO findByAccountAndUsername(Long accountId, String userName);

    long getVpnUserCount(Long accountId);
}
