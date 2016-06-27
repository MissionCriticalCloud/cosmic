package com.cloud.vm.dao;

import org.apache.cloudstack.api.response.SecurityGroupRuleResponse;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserVmData {
    private final Set<SecurityGroupData> securityGroupList;
    private final Set<NicData> nics;
    private Long id;
    private String name;
    private String uuid;
    private String displayName;
    private String ipAddress;
    private String accountName;
    private Long domainId;
    private String domainName;
    private Date created;
    private String state;
    private Boolean haEnable;
    private Long groupId;
    private String group;
    private Long zoneId;
    private String zoneName;
    private Long hostId;
    private String hostName;
    private Long templateId;
    private String templateName;
    private String templateDisplayText;
    private Boolean passwordEnabled;
    private Long isoId;
    private String isoName;
    private String isoDisplayText;
    private Long serviceOfferingId;
    private String serviceOfferingName;
    private Boolean forVirtualNetwork;
    private Integer cpuNumber;
    private Integer cpuSpeed;
    private Integer memory;
    private String cpuUsed;
    private Long networkKbsRead;
    private Long networkKbsWrite;
    private Long diskKbsRead;
    private Long diskKbsWrite;
    private Long diskIORead;
    private Long diskIOWrite;
    private Long guestOsId;
    private Long rootDeviceId;
    private String rootDeviceType;
    private String password;
    private Long jobId;
    private Integer jobStatus;
    private String hypervisor;
    private long accountId;
    private Long publicIpId;
    private String publicIp;
    private String instanceName;
    private String sshPublicKey;

    private boolean initialized;

    public UserVmData() {
        securityGroupList = new HashSet<>();
        nics = new HashSet<>();
        initialized = false;
    }

    public void setInitialized() {
        initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public NicData newNicData() {
        return new NicData();
    }

    public SecurityGroupData newSecurityGroupData() {
        return new SecurityGroupData();
    }

    public String getHypervisor() {
        return hypervisor;
    }

    public void setHypervisor(final String hypervisor) {
        this.hypervisor = hypervisor;
    }

    public Long getObjectId() {
        return getId();
    }

    public Long getId() {
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

    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(final String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(final String accountName) {
        this.accountName = accountName;
    }

    public Long getDomainId() {
        return domainId;
    }

    public void setDomainId(final Long domainId) {
        this.domainId = domainId;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(final String domainName) {
        this.domainName = domainName;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public Boolean getHaEnable() {
        return haEnable;
    }

    public void setHaEnable(final Boolean haEnable) {
        this.haEnable = haEnable;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(final Long groupId) {
        this.groupId = groupId;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(final String group) {
        this.group = group;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public void setZoneId(final Long zoneId) {
        this.zoneId = zoneId;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(final String zoneName) {
        this.zoneName = zoneName;
    }

    public Long getHostId() {
        return hostId;
    }

    public void setHostId(final Long hostId) {
        this.hostId = hostId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(final String hostName) {
        this.hostName = hostName;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(final Long templateId) {
        this.templateId = templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(final String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateDisplayText() {
        return templateDisplayText;
    }

    public void setTemplateDisplayText(final String templateDisplayText) {
        this.templateDisplayText = templateDisplayText;
    }

    public Boolean getPasswordEnabled() {
        return passwordEnabled;
    }

    public void setPasswordEnabled(final Boolean passwordEnabled) {
        this.passwordEnabled = passwordEnabled;
    }

    public Long getIsoId() {
        return isoId;
    }

    public void setIsoId(final Long isoId) {
        this.isoId = isoId;
    }

    public String getIsoName() {
        return isoName;
    }

    public void setIsoName(final String isoName) {
        this.isoName = isoName;
    }

    public String getIsoDisplayText() {
        return isoDisplayText;
    }

    public void setIsoDisplayText(final String isoDisplayText) {
        this.isoDisplayText = isoDisplayText;
    }

    public Long getServiceOfferingId() {
        return serviceOfferingId;
    }

    public void setServiceOfferingId(final Long serviceOfferingId) {
        this.serviceOfferingId = serviceOfferingId;
    }

    public String getServiceOfferingName() {
        return serviceOfferingName;
    }

    public void setServiceOfferingName(final String serviceOfferingName) {
        this.serviceOfferingName = serviceOfferingName;
    }

    public Integer getCpuNumber() {
        return cpuNumber;
    }

    public void setCpuNumber(final Integer cpuNumber) {
        this.cpuNumber = cpuNumber;
    }

    public Integer getCpuSpeed() {
        return cpuSpeed;
    }

    public void setCpuSpeed(final Integer cpuSpeed) {
        this.cpuSpeed = cpuSpeed;
    }

    public Integer getMemory() {
        return memory;
    }

    public void setMemory(final Integer memory) {
        this.memory = memory;
    }

    public String getCpuUsed() {
        return cpuUsed;
    }

    public void setCpuUsed(final String cpuUsed) {
        this.cpuUsed = cpuUsed;
    }

    public Long getNetworkKbsRead() {
        return networkKbsRead;
    }

    public void setNetworkKbsRead(final Long networkKbsRead) {
        this.networkKbsRead = networkKbsRead;
    }

    public Long getNetworkKbsWrite() {
        return networkKbsWrite;
    }

    public void setNetworkKbsWrite(final Long networkKbsWrite) {
        this.networkKbsWrite = networkKbsWrite;
    }

    public Long getDiskKbsRead() {
        return diskKbsRead;
    }

    public void setDiskKbsRead(final Long diskKbsRead) {
        this.diskKbsRead = diskKbsRead;
    }

    public Long getDiskKbsWrite() {
        return diskKbsWrite;
    }

    public void setDiskKbsWrite(final Long diskKbsWrite) {
        this.diskKbsWrite = diskKbsWrite;
    }

    public Long getDiskIORead() {
        return diskIORead;
    }

    public void setDiskIORead(final Long diskIORead) {
        this.diskIORead = diskIORead;
    }

    public Long getDiskIOWrite() {
        return diskIOWrite;
    }

    public void setDiskIOWrite(final Long diskIOWrite) {
        this.diskIOWrite = diskIOWrite;
    }

    public Long getGuestOsId() {
        return guestOsId;
    }

    public void setGuestOsId(final Long guestOsId) {
        this.guestOsId = guestOsId;
    }

    public Long getRootDeviceId() {
        return rootDeviceId;
    }

    public void setRootDeviceId(final Long rootDeviceId) {
        this.rootDeviceId = rootDeviceId;
    }

    public String getRootDeviceType() {
        return rootDeviceType;
    }

    public void setRootDeviceType(final String rootDeviceType) {
        this.rootDeviceType = rootDeviceType;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(final Long jobId) {
        this.jobId = jobId;
    }

    public Integer getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(final Integer jobStatus) {
        this.jobStatus = jobStatus;
    }

    public Boolean getForVirtualNetwork() {
        return forVirtualNetwork;
    }

    public void setForVirtualNetwork(final Boolean forVirtualNetwork) {
        this.forVirtualNetwork = forVirtualNetwork;
    }

    public Set<NicData> getNics() {
        return nics;
    }

    public void addNic(final NicData nics) {
        this.nics.add(nics);
    }

    public Set<SecurityGroupData> getSecurityGroupList() {
        return securityGroupList;
    }

    public void addSecurityGroup(final SecurityGroupData securityGroups) {
        this.securityGroupList.add(securityGroups);
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(final long accountId) {
        this.accountId = accountId;
    }

    public Long getPublicIpId() {
        return publicIpId;
    }

    public void setPublicIpId(final Long publicIpId) {
        this.publicIpId = publicIpId;
    }

    @Override
    public String toString() {
        return "id=" + id + ", name=" + name;
    }

    public String getPublicIp() {
        return publicIp;
    }

    public void setPublicIp(final String publicIp) {
        this.publicIp = publicIp;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(final String instanceName) {
        this.instanceName = instanceName;
    }

    public String getSshPublicKey() {
        return sshPublicKey;
    }

    public void setSshPublicKey(final String sshPublicKey) {
        this.sshPublicKey = sshPublicKey;
    }

    public class NicData {
        private String objectName;
        private Long id;
        private Long networkid;
        private String netmask;
        private String gateway;
        private String ipaddress;
        private String isolationUri;
        private String broadcastUri;
        private String trafficType;
        private String type;
        private Boolean isDefault;
        private String macAddress;

        public String getObjectName() {
            return objectName;
        }

        public void setObjectName(final String objectName) {
            this.objectName = objectName;
        }

        public Long getId() {
            return id;
        }

        public void setId(final Long id) {
            this.id = id;
        }

        public Long getNetworkid() {
            return networkid;
        }

        public void setNetworkid(final Long networkid) {
            this.networkid = networkid;
        }

        public String getNetmask() {
            return netmask;
        }

        public void setNetmask(final String netmask) {
            this.netmask = netmask;
        }

        public String getGateway() {
            return gateway;
        }

        public void setGateway(final String gateway) {
            this.gateway = gateway;
        }

        public String getIpaddress() {
            return ipaddress;
        }

        public void setIpaddress(final String ipaddress) {
            this.ipaddress = ipaddress;
        }

        public String getIsolationUri() {
            return isolationUri;
        }

        public void setIsolationUri(final String isolationUri) {
            this.isolationUri = isolationUri;
        }

        public String getBroadcastUri() {
            return broadcastUri;
        }

        public void setBroadcastUri(final String broadcastUri) {
            this.broadcastUri = broadcastUri;
        }

        public String getTrafficType() {
            return trafficType;
        }

        public void setTrafficType(final String trafficType) {
            this.trafficType = trafficType;
        }

        public String getType() {
            return type;
        }

        public void setType(final String type) {
            this.type = type;
        }

        public Boolean getIsDefault() {
            return isDefault;
        }

        public void setIsDefault(final Boolean isDefault) {
            this.isDefault = isDefault;
        }

        public String getMacAddress() {
            return macAddress;
        }

        public void setMacAddress(final String macAddress) {
            this.macAddress = macAddress;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final NicData other = (NicData) obj;
            if (id == null) {
                if (other.id != null) {
                    return false;
                }
            } else if (!id.equals(other.id)) {
                return false;
            }
            return true;
        }
    }

    public class SecurityGroupData {
        private String objectName;
        private Long id;
        private String name;
        private String description;
        private String accountName;
        private Long domainId;
        private String domainName;
        private Long jobId;
        private Integer jobStatus;
        private List<SecurityGroupRuleResponse> securityGroupRules;

        public String getObjectName() {
            return objectName;
        }

        public void setObjectName(final String objectName) {
            this.objectName = objectName;
        }

        public Long getId() {
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

        public String getAccountName() {
            return accountName;
        }

        public void setAccountName(final String accountName) {
            this.accountName = accountName;
        }

        public Long getDomainId() {
            return domainId;
        }

        public void setDomainId(final Long domainId) {
            this.domainId = domainId;
        }

        public String getDomainName() {
            return domainName;
        }

        public void setDomainName(final String domainName) {
            this.domainName = domainName;
        }

        /* FIXME : the below functions are not used, so commenting out later need to include egress list
                public List<SecurityGroupRuleResponse> getIngressRules() {
                    return securityGroupRules;
                }

                public void setIngressRules(List<SecurityGroupRuleResponse> securityGroupRules) {
                    this.securityGroupRules = securityGroupRules;
                } */

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final SecurityGroupData other = (SecurityGroupData) obj;
            if (id == null) {
                if (other.id != null) {
                    return false;
                }
            } else if (!id.equals(other.id)) {
                return false;
            }
            return true;
        }
    }
}
