package com.cloud.resourcedetail.dao;

import com.cloud.resourcedetail.FirewallRuleDetailVO;
import com.cloud.resourcedetail.ResourceDetailsDaoBase;

import org.springframework.stereotype.Component;

@Component
public class FirewallRuleDetailsDaoImpl extends ResourceDetailsDaoBase<FirewallRuleDetailVO> implements FirewallRuleDetailsDao {

    @Override
    public void addDetail(final long resourceId, final String key, final String value, final boolean display) {
        super.addDetail(new FirewallRuleDetailVO(resourceId, key, value, display));
    }
}
