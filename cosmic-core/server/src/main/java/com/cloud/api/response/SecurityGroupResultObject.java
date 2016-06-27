package com.cloud.api.response;

import com.cloud.api.ApiDBUtils;
import com.cloud.network.security.SecurityGroup;
import com.cloud.network.security.SecurityGroupRules;
import com.cloud.serializer.Param;
import com.cloud.user.Account;
import org.apache.cloudstack.acl.ControlledEntity;
import org.apache.cloudstack.api.InternalIdentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecurityGroupResultObject implements ControlledEntity, InternalIdentity {
    @Param(name = "id")
    private Long id;

    @Param(name = "name")
    private String name;

    @Param(name = "description")
    private String description;

    @Param(name = "domainid")
    private long domainId;

    @Param(name = "accountid")
    private long accountId;

    @Param(name = "accountname")
    private String accountName = null;

    @Param(name = "securitygrouprules")
    private List<SecurityGroupRuleResultObject> securityGroupRules = null;

    public SecurityGroupResultObject() {
    }

    public SecurityGroupResultObject(final Long id, final String name, final String description, final long domainId, final long accountId, final String accountName,
                                     final List<SecurityGroupRuleResultObject> ingressRules) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.domainId = domainId;
        this.accountId = accountId;
        this.accountName = accountName;
        securityGroupRules = ingressRules;
    }

    public static List<SecurityGroupResultObject> transposeNetworkGroups(final List<? extends SecurityGroupRules> groups) {
        final List<SecurityGroupResultObject> resultObjects = new ArrayList<>();
        final Map<Long, SecurityGroup> allowedSecurityGroups = new HashMap<>();
        final Map<Long, Account> accounts = new HashMap<>();

        if ((groups != null) && !groups.isEmpty()) {
            List<SecurityGroupRuleResultObject> securityGroupRuleDataList = new ArrayList<>();
            SecurityGroupResultObject currentGroup = null;

            final List<Long> processedGroups = new ArrayList<>();
            for (final SecurityGroupRules netGroupRule : groups) {
                final Long groupId = netGroupRule.getId();
                if (!processedGroups.contains(groupId)) {
                    processedGroups.add(groupId);

                    if (currentGroup != null) {
                        if (!securityGroupRuleDataList.isEmpty()) {
                            currentGroup.setSecurityGroupRules(securityGroupRuleDataList);
                            securityGroupRuleDataList = new ArrayList<>();
                        }
                        resultObjects.add(currentGroup);
                    }

                    // start a new group
                    final SecurityGroupResultObject groupResult = new SecurityGroupResultObject();
                    groupResult.setId(netGroupRule.getId());
                    groupResult.setName(netGroupRule.getName());
                    groupResult.setDescription(netGroupRule.getDescription());
                    groupResult.setDomainId(netGroupRule.getDomainId());

                    Account account = accounts.get(netGroupRule.getAccountId());
                    if (account == null) {
                        account = ApiDBUtils.findAccountById(netGroupRule.getAccountId());
                        accounts.put(account.getId(), account);
                    }

                    groupResult.setAccountId(account.getId());
                    groupResult.setAccountName(account.getAccountName());

                    currentGroup = groupResult;
                }

                if (netGroupRule.getRuleId() != null) {
                    // there's at least one securitygroup rule for this network group, add the securitygroup rule data
                    final SecurityGroupRuleResultObject securityGroupRuleData = new SecurityGroupRuleResultObject();
                    securityGroupRuleData.setEndPort(netGroupRule.getEndPort());
                    securityGroupRuleData.setStartPort(netGroupRule.getStartPort());
                    securityGroupRuleData.setId(netGroupRule.getRuleId());
                    securityGroupRuleData.setProtocol(netGroupRule.getProtocol());
                    securityGroupRuleData.setRuleType(netGroupRule.getRuleType());
                    final Long allowedSecurityGroupId = netGroupRule.getAllowedNetworkId();
                    if (allowedSecurityGroupId != null) {
                        SecurityGroup allowedSecurityGroup = allowedSecurityGroups.get(allowedSecurityGroupId);
                        if (allowedSecurityGroup == null) {
                            allowedSecurityGroup = ApiDBUtils.findSecurityGroupById(allowedSecurityGroupId);
                            allowedSecurityGroups.put(allowedSecurityGroupId, allowedSecurityGroup);
                        }

                        securityGroupRuleData.setAllowedSecurityGroup(allowedSecurityGroup.getName());

                        Account allowedAccount = accounts.get(allowedSecurityGroup.getAccountId());
                        if (allowedAccount == null) {
                            allowedAccount = ApiDBUtils.findAccountById(allowedSecurityGroup.getAccountId());
                            accounts.put(allowedAccount.getId(), allowedAccount);
                        }

                        securityGroupRuleData.setAllowedSecGroupAcct(allowedAccount.getAccountName());
                    } else if (netGroupRule.getAllowedSourceIpCidr() != null) {
                        securityGroupRuleData.setAllowedSourceIpCidr(netGroupRule.getAllowedSourceIpCidr());
                    }
                    securityGroupRuleDataList.add(securityGroupRuleData);
                }
            }

            // all rules have been processed, add the final data into the list
            if (currentGroup != null) {
                if (!securityGroupRuleDataList.isEmpty()) {
                    currentGroup.setSecurityGroupRules(securityGroupRuleDataList);
                }
                resultObjects.add(currentGroup);
            }
        }
        return resultObjects;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public long getDomainId() {
        return domainId;
    }

    public void setDomainId(final Long domainId) {
        this.domainId = domainId;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(final Long accountId) {
        this.accountId = accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(final String accountName) {
        this.accountName = accountName;
    }

    public List<SecurityGroupRuleResultObject> getSecurityGroupRules() {
        return securityGroupRules;
    }

    public void setSecurityGroupRules(final List<SecurityGroupRuleResultObject> securityGroupRules) {
        this.securityGroupRules = securityGroupRules;
    }

    @Override
    public Class<?> getEntityType() {
        return SecurityGroup.class;
    }
}
