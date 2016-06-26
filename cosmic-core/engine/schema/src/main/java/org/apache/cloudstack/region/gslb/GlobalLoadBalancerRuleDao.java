package org.apache.cloudstack.region.gslb;

import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface GlobalLoadBalancerRuleDao extends GenericDao<GlobalLoadBalancerRuleVO, Long> {

    List<GlobalLoadBalancerRuleVO> listByRegionId(int regionId);

    List<GlobalLoadBalancerRuleVO> listByAccount(long accountId);

    GlobalLoadBalancerRuleVO findByDomainName(String domainName);
}
