package org.apache.cloudstack.region.gslb;

import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface GlobalLoadBalancerLbRuleMapDao extends GenericDao<GlobalLoadBalancerLbRuleMapVO, Long> {

    List<GlobalLoadBalancerLbRuleMapVO> listByGslbRuleId(long gslbRuleId);

    GlobalLoadBalancerLbRuleMapVO findByGslbRuleIdAndLbRuleId(long gslbRuleId, long lbRuleId);
}
