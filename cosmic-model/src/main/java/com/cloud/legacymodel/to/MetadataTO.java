package com.cloud.legacymodel.to;

import java.util.List;
import java.util.Map;

public class MetadataTO {

    private String domainUuid;
    private Long vmId;
    private String cosmicDomainName;
    private String cosmicDomainPath;
    private String hostname;
    private String instanceName;
    private Map<String, String> resourceDetails;
    private Map<String, String> resourceTags;
    private List<String> vpcNameList;

    public String getDomainUuid() {
        return domainUuid;
    }

    public void setDomainUuid(final String domainUuid) {
        this.domainUuid = domainUuid;
    }

    public Long getVmId() {
        return vmId;
    }

    public void setVmId(final Long vmId) {
        this.vmId = vmId;
    }

    public String getCosmicDomainName() {
        return cosmicDomainName;
    }

    public void setCosmicDomainName(final String cosmicDomainName) {
        this.cosmicDomainName = cosmicDomainName;
    }

    public String getCosmicDomainPath() {
        return cosmicDomainPath;
    }

    public void setCosmicDomainPath(final String cosmicDomainPath) {
        this.cosmicDomainPath = cosmicDomainPath;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(final String hostname) {
        this.hostname = hostname;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(final String instanceName) {
        this.instanceName = instanceName;
    }

    public Map<String, String> getResourceDetails() {
        return resourceDetails;
    }

    public void setResourceDetails(final Map<String, String> resourceDetails) {
        this.resourceDetails = resourceDetails;
    }

    public Map<String, String> getResourceTags() {
        return resourceTags;
    }

    public void setResourceTags(final Map<String, String> resourceTags) {
        this.resourceTags = resourceTags;
    }

    public List<String> getVpcNameList() {
        return vpcNameList;
    }

    public void setVpcNameList(final List<String> vpcNameList) {
        this.vpcNameList = vpcNameList;
    }
}
