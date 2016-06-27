package org.apache.cloudstack.resourcedetail.dao;

import org.apache.cloudstack.resourcedetail.FirewallRuleDetailVO;
import org.apache.cloudstack.resourcedetail.ResourceDetailsDaoBase;

import org.springframework.stereotype.Component;

@Component
public class FirewallRuleDetailsDaoImpl extends ResourceDetailsDaoBase<FirewallRuleDetailVO> implements FirewallRuleDetailsDao {

    @Override
    public void addDetail(final long resourceId, final String key, final String value, final boolean display) {
        super.addDetail(new FirewallRuleDetailVO(resourceId, key, value, display));
    }
}
