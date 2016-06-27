package com.cloud.network.dao;

import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface AccountGuestVlanMapDao extends GenericDao<AccountGuestVlanMapVO, Long> {

    public List<AccountGuestVlanMapVO> listAccountGuestVlanMapsByAccount(long accountId);

    public List<AccountGuestVlanMapVO> listAccountGuestVlanMapsByVlan(long guestVlanId);

    public List<AccountGuestVlanMapVO> listAccountGuestVlanMapsByPhysicalNetwork(long physicalNetworkId);

    public int removeByAccountId(long accountId);
}
