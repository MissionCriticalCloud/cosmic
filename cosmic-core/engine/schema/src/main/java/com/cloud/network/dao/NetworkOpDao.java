package com.cloud.network.dao;

import com.cloud.utils.db.GenericDao;

public interface NetworkOpDao extends GenericDao<NetworkOpVO, Long> {
    public int getActiveNics(long networkId);

    public void changeActiveNicsBy(long networkId, int count);

    public void setCheckForGc(long networkId);

    public void clearCheckForGc(long networkId);
}
