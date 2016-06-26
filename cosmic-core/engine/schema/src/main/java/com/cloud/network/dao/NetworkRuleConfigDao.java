package com.cloud.network.dao;

import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface NetworkRuleConfigDao extends GenericDao<NetworkRuleConfigVO, Long> {
    List<NetworkRuleConfigVO> listBySecurityGroupId(long securityGroupId);

    void deleteBySecurityGroup(long securityGroupId);
}
