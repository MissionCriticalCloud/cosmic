package com.cloud.dc.dao;

import com.cloud.dc.AccountVlanMapVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface AccountVlanMapDao extends GenericDao<AccountVlanMapVO, Long> {

    public List<AccountVlanMapVO> listAccountVlanMapsByAccount(long accountId);

    public List<AccountVlanMapVO> listAccountVlanMapsByVlan(long vlanDbId);

    public AccountVlanMapVO findAccountVlanMap(long accountId, long vlanDbId);
}
