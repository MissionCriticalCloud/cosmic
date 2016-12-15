package com.cloud.network.security;

import com.cloud.api.command.user.securitygroup.AuthorizeSecurityGroupEgressCmd;
import com.cloud.api.command.user.securitygroup.AuthorizeSecurityGroupIngressCmd;
import com.cloud.api.command.user.securitygroup.CreateSecurityGroupCmd;
import com.cloud.api.command.user.securitygroup.DeleteSecurityGroupCmd;
import com.cloud.api.command.user.securitygroup.RevokeSecurityGroupEgressCmd;
import com.cloud.api.command.user.securitygroup.RevokeSecurityGroupIngressCmd;
import com.cloud.exception.PermissionDeniedException;
import com.cloud.exception.ResourceInUseException;
import com.cloud.utils.exception.InvalidParameterValueException;

import java.util.List;

public interface SecurityGroupService {
    /**
     * Create a network group with the given name and description
     *
     * @param command the command specifying the name and description
     * @return the created security group if successful, null otherwise
     */
    public SecurityGroup createSecurityGroup(CreateSecurityGroupCmd command) throws PermissionDeniedException, InvalidParameterValueException;

    boolean revokeSecurityGroupIngress(RevokeSecurityGroupIngressCmd cmd);

    boolean revokeSecurityGroupEgress(RevokeSecurityGroupEgressCmd cmd);

    boolean deleteSecurityGroup(DeleteSecurityGroupCmd cmd) throws ResourceInUseException;

    public List<? extends SecurityRule> authorizeSecurityGroupIngress(AuthorizeSecurityGroupIngressCmd cmd);

    public List<? extends SecurityRule> authorizeSecurityGroupEgress(AuthorizeSecurityGroupEgressCmd cmd);

    public boolean securityGroupRulesForVmSecIp(long nicId, String secondaryIp, boolean ruleAction);
}
