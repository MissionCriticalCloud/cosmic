package com.cloud.api.query.vo;

import com.cloud.network.security.SecurityGroup;
import com.cloud.network.security.SecurityRule.SecurityRuleType;
import com.cloud.server.ResourceTag.ResourceObjectType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "security_group_view")
public class SecurityGroupJoinVO extends BaseViewVO implements ControlledViewEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "uuid")
    private String uuid;

    @Column(name = "account_id")
    private long accountId;

    @Column(name = "account_uuid")
    private String accountUuid;

    @Column(name = "account_name")
    private String accountName = null;

    @Column(name = "account_type")
    private short accountType;

    @Column(name = "domain_id")
    private long domainId;

    @Column(name = "domain_uuid")
    private String domainUuid;

    @Column(name = "domain_name")
    private String domainName = null;

    @Column(name = "domain_path")
    private String domainPath = null;

    @Column(name = "project_id")
    private long projectId;

    @Column(name = "project_uuid")
    private String projectUuid;

    @Column(name = "project_name")
    private String projectName;

    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "job_uuid")
    private String jobUuid;

    @Column(name = "job_status")
    private int jobStatus;

    @Column(name = "rule_id")
    private Long ruleId;

    @Column(name = "rule_uuid")
    private String ruleUuid;

    @Column(name = "rule_start_port")
    private int ruleStartPort;

    @Column(name = "rule_end_port")
    private int ruleEndPort;

    @Column(name = "rule_protocol")
    private String ruleProtocol;

    @Column(name = "rule_type")
    private String ruleType;

    @Column(name = "rule_allowed_network_id")
    private Long ruleAllowedNetworkId = null;

    @Column(name = "rule_allowed_ip_cidr")
    private String ruleAllowedSourceIpCidr = null;

    @Column(name = "tag_id")
    private long tagId;

    @Column(name = "tag_uuid")
    private String tagUuid;

    @Column(name = "tag_key")
    private String tagKey;

    @Column(name = "tag_value")
    private String tagValue;

    @Column(name = "tag_domain_id")
    private long tagDomainId;

    @Column(name = "tag_account_id")
    private long tagAccountId;

    @Column(name = "tag_resource_id")
    private long tagResourceId;

    @Column(name = "tag_resource_uuid")
    private String tagResourceUuid;

    @Column(name = "tag_resource_type")
    @Enumerated(value = EnumType.STRING)
    private ResourceObjectType tagResourceType;

    @Column(name = "tag_customer")
    private String tagCustomer;

    public SecurityGroupJoinVO() {
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public void setAccountId(final long accountId) {
        this.accountId = accountId;
    }

    public void setAccountUuid(final String accountUuid) {
        this.accountUuid = accountUuid;
    }

    public void setAccountName(final String accountName) {
        this.accountName = accountName;
    }

    public void setAccountType(final short accountType) {
        this.accountType = accountType;
    }

    public void setDomainId(final long domainId) {
        this.domainId = domainId;
    }

    public void setDomainUuid(final String domainUuid) {
        this.domainUuid = domainUuid;
    }

    public void setDomainName(final String domainName) {
        this.domainName = domainName;
    }

    public void setDomainPath(final String domainPath) {
        this.domainPath = domainPath;
    }

    public void setProjectId(final long projectId) {
        this.projectId = projectId;
    }

    public void setProjectUuid(final String projectUuid) {
        this.projectUuid = projectUuid;
    }

    public void setProjectName(final String projectName) {
        this.projectName = projectName;
    }

    public void setJobId(final Long jobId) {
        this.jobId = jobId;
    }

    public void setJobUuid(final String jobUuid) {
        this.jobUuid = jobUuid;
    }

    public void setJobStatus(final int jobStatus) {
        this.jobStatus = jobStatus;
    }

    public void setRuleId(final Long ruleId) {
        this.ruleId = ruleId;
    }

    public void setRuleUuid(final String ruleUuid) {
        this.ruleUuid = ruleUuid;
    }

    public void setRuleStartPort(final int ruleStartPort) {
        this.ruleStartPort = ruleStartPort;
    }

    public void setRuleEndPort(final int ruleEndPort) {
        this.ruleEndPort = ruleEndPort;
    }

    public void setRuleProtocol(final String ruleProtocol) {
        this.ruleProtocol = ruleProtocol;
    }

    public void setRuleType(final String ruleType) {
        this.ruleType = ruleType;
    }

    public void setRuleAllowedNetworkId(final Long ruleAllowedNetworkId) {
        this.ruleAllowedNetworkId = ruleAllowedNetworkId;
    }

    public void setRuleAllowedSourceIpCidr(final String ruleAllowedSourceIpCidr) {
        this.ruleAllowedSourceIpCidr = ruleAllowedSourceIpCidr;
    }

    public void setTagId(final long tagId) {
        this.tagId = tagId;
    }

    public void setTagUuid(final String tagUuid) {
        this.tagUuid = tagUuid;
    }

    public void setTagKey(final String tagKey) {
        this.tagKey = tagKey;
    }

    public void setTagValue(final String tagValue) {
        this.tagValue = tagValue;
    }

    public void setTagDomainId(final long tagDomainId) {
        this.tagDomainId = tagDomainId;
    }

    public void setTagAccountId(final long tagAccountId) {
        this.tagAccountId = tagAccountId;
    }

    public void setTagResourceId(final long tagResourceId) {
        this.tagResourceId = tagResourceId;
    }

    public void setTagResourceUuid(final String tagResourceUuid) {
        this.tagResourceUuid = tagResourceUuid;
    }

    public void setTagResourceType(final ResourceObjectType tagResourceType) {
        this.tagResourceType = tagResourceType;
    }

    public void setTagCustomer(final String tagCustomer) {
        this.tagCustomer = tagCustomer;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    @Override
    public long getDomainId() {
        return domainId;
    }

    @Override
    public String getDomainPath() {
        return domainPath;
    }

    @Override
    public short getAccountType() {
        return accountType;
    }

    @Override
    public String getAccountUuid() {
        return accountUuid;
    }

    @Override
    public String getAccountName() {
        return accountName;
    }

    @Override
    public String getDomainUuid() {
        return domainUuid;
    }

    @Override
    public String getDomainName() {
        return domainName;
    }

    @Override
    public String getProjectUuid() {
        return projectUuid;
    }

    @Override
    public String getProjectName() {
        return projectName;
    }

    public long getProjectId() {
        return projectId;
    }

    public Long getJobId() {
        return jobId;
    }

    public String getJobUuid() {
        return jobUuid;
    }

    public int getJobStatus() {
        return jobStatus;
    }

    public String getDescription() {
        return description;
    }

    public Long getRuleId() {
        return ruleId;
    }

    public String getRuleUuid() {
        return ruleUuid;
    }

    public int getRuleStartPort() {
        return ruleStartPort;
    }

    public int getRuleEndPort() {
        return ruleEndPort;
    }

    public String getRuleProtocol() {
        return ruleProtocol;
    }

    public SecurityRuleType getRuleType() {
        if ("ingress".equalsIgnoreCase(ruleType)) {
            return SecurityRuleType.IngressRule;
        } else {
            return SecurityRuleType.EgressRule;
        }
    }

    public Long getRuleAllowedNetworkId() {
        return ruleAllowedNetworkId;
    }

    public String getRuleAllowedSourceIpCidr() {
        return ruleAllowedSourceIpCidr;
    }

    public long getTagId() {
        return tagId;
    }

    public String getTagUuid() {
        return tagUuid;
    }

    public String getTagKey() {
        return tagKey;
    }

    public String getTagValue() {
        return tagValue;
    }

    public long getTagDomainId() {
        return tagDomainId;
    }

    public long getTagAccountId() {
        return tagAccountId;
    }

    public long getTagResourceId() {
        return tagResourceId;
    }

    public String getTagResourceUuid() {
        return tagResourceUuid;
    }

    public ResourceObjectType getTagResourceType() {
        return tagResourceType;
    }

    public String getTagCustomer() {
        return tagCustomer;
    }

    @Override
    public Class<?> getEntityType() {
        return SecurityGroup.class;
    }
}
