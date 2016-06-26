package com.cloud.network.dao;

import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface FirewallRulesCidrsDao extends GenericDao<FirewallRulesCidrsVO, Long> {

    void persist(long firewallRuleId, List<String> sourceCidrs);

    List<String> getSourceCidrs(long firewallRuleId);

    @DB
    List<FirewallRulesCidrsVO> listByFirewallRuleId(long firewallRuleId);
}
