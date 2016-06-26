package com.cloud.network.security.dao;

import com.cloud.network.security.SecurityGroupRulesVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface SecurityGroupRulesDao extends GenericDao<SecurityGroupRulesVO, Long> {
    /**
     * List a security group and associated ingress rules
     *
     * @return the list of ingress rules associated with the security group (and security group info)
     */
    List<SecurityGroupRulesVO> listSecurityGroupRules(long accountId, String groupName);

    /**
     * List security groups and associated ingress rules
     *
     * @return the list of security groups with associated ingress rules
     */
    List<SecurityGroupRulesVO> listSecurityGroupRules(long accountId);

    /**
     * List all security groups and associated ingress rules
     *
     * @return the list of security groups with associated ingress rules
     */
    List<SecurityGroupRulesVO> listSecurityGroupRules();

    /**
     * List all security rules belonging to the specific group
     *
     * @return the security group with associated ingress rules
     */
    List<SecurityGroupRulesVO> listSecurityRulesByGroupId(long groupId);
}
