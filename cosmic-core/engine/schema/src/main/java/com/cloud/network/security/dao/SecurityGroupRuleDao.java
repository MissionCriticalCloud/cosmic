package com.cloud.network.security.dao;

import com.cloud.network.security.SecurityGroupRuleVO;
import com.cloud.network.security.SecurityRule.SecurityRuleType;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface SecurityGroupRuleDao extends GenericDao<SecurityGroupRuleVO, Long> {
    List<SecurityGroupRuleVO> listBySecurityGroupId(long securityGroupId, SecurityRuleType type);

    List<SecurityGroupRuleVO> listByAllowedSecurityGroupId(long networkGroupId);

    SecurityGroupRuleVO findByProtoPortsAndCidr(long networkGroupId, String proto, int startPort, int endPort, String cidr);

    SecurityGroupRuleVO findByProtoPortsAndGroup(String proto, int startPort, int endPort, String networkGroup);

    SecurityGroupRuleVO findByProtoPortsAndAllowedGroupId(long networkGroupId, String proto, int startPort, int endPort, Long allowedGroupId);

    int deleteBySecurityGroup(long securityGroupId);

    int deleteByPortProtoAndGroup(long securityGroupId, String protocol, int startPort, int endPort, Long id);

    int deleteByPortProtoAndCidr(long securityGroupId, String protocol, int startPort, int endPort, String cidr);
}
