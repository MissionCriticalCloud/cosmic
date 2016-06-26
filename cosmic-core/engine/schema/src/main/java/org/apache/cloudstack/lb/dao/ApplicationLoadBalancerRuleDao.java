package org.apache.cloudstack.lb.dao;

import com.cloud.network.rules.LoadBalancerContainer.Scheme;
import com.cloud.utils.db.GenericDao;
import com.cloud.utils.net.Ip;
import org.apache.cloudstack.lb.ApplicationLoadBalancerRuleVO;

import java.util.List;

public interface ApplicationLoadBalancerRuleDao extends GenericDao<ApplicationLoadBalancerRuleVO, Long> {
    List<ApplicationLoadBalancerRuleVO> listBySrcIpSrcNtwkId(Ip sourceIp, long sourceNetworkId);

    List<String> listLbIpsBySourceIpNetworkId(long sourceIpNetworkId);

    long countBySourceIp(Ip sourceIp, long sourceIpNetworkId);

    List<ApplicationLoadBalancerRuleVO> listBySourceIpAndNotRevoked(Ip sourceIp, long sourceNetworkId);

    List<String> listLbIpsBySourceIpNetworkIdAndScheme(long sourceIpNetworkId, Scheme scheme);

    long countBySourceIpAndNotRevoked(Ip sourceIp, long sourceIpNetworkId);

    long countActiveBySourceIp(Ip sourceIp, long sourceIpNetworkId);
}
