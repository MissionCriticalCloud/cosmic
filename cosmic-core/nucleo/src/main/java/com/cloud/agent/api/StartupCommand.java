//

//

package com.cloud.agent.api;

import com.cloud.host.Host;

public class StartupCommand extends Command {
    Host.Type type;
    String dataCenter;
    String pod;
    String cluster;
    String guid;
    String name;
    Long id;
    String version;
    String iqn;
    String publicIpAddress;
    String publicNetmask;
    String publicMacAddress;
    String privateIpAddress;
    String privateMacAddress;
    String privateNetmask;
    String storageIpAddress;
    String storageNetmask;
    String storageMacAddress;
    String storageIpAddressDeux;
    String storageMacAddressDeux;
    String storageNetmaskDeux;
    String agentTag;
    String resourceName;
    String gatewayIpAddress;

    public StartupCommand(final Host.Type type) {
        this.type = type;
    }

    public StartupCommand(final Long id, final Host.Type type, final String name, final String dataCenter, final String pod, final String guid, final String version, final
    String gatewayIpAddress) {
        this(id, type, name, dataCenter, pod, guid, version);
        this.gatewayIpAddress = gatewayIpAddress;
    }

    public StartupCommand(final Long id, final Host.Type type, final String name, final String dataCenter, final String pod, final String guid, final String version) {
        super();
        this.id = id;
        this.dataCenter = dataCenter;
        this.pod = pod;
        this.guid = guid;
        this.name = name;
        this.version = version;
        this.type = type;
    }

    public Host.Type getHostType() {
        return type;
    }

    public void setHostType(final Host.Type type) {
        this.type = type;
    }

    public String getIqn() {
        return iqn;
    }

    public void setIqn(final String iqn) {
        this.iqn = iqn;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(final String cluster) {
        this.cluster = cluster;
    }

    public String getDataCenter() {
        return dataCenter;
    }

    public void setDataCenter(final String dataCenter) {
        this.dataCenter = dataCenter;
    }

    public String getPod() {
        return pod;
    }

    public void setPod(final String pod) {
        this.pod = pod;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getStorageIpAddressDeux() {
        return storageIpAddressDeux;
    }

    public void setStorageIpAddressDeux(final String storageIpAddressDeux) {
        this.storageIpAddressDeux = storageIpAddressDeux;
    }

    public String getStorageMacAddressDeux() {
        return storageMacAddressDeux;
    }

    public void setStorageMacAddressDeux(final String storageMacAddressDeux) {
        this.storageMacAddressDeux = storageMacAddressDeux;
    }

    public String getStorageNetmaskDeux() {
        return storageNetmaskDeux;
    }

    public void setStorageNetmaskDeux(final String storageNetmaskDeux) {
        this.storageNetmaskDeux = storageNetmaskDeux;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(final String guid) {
        this.guid = guid;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public void setGuid(final String guid, final String resourceName) {
        this.resourceName = resourceName;
        this.guid = guid + "-" + resourceName;
    }

    public String getPublicNetmask() {
        return publicNetmask;
    }

    public void setPublicNetmask(final String publicNetmask) {
        this.publicNetmask = publicNetmask;
    }

    public String getPublicMacAddress() {
        return publicMacAddress;
    }

    public void setPublicMacAddress(final String publicMacAddress) {
        this.publicMacAddress = publicMacAddress;
    }

    public String getPrivateIpAddress() {
        return privateIpAddress;
    }

    public void setPrivateIpAddress(final String privateIpAddress) {
        this.privateIpAddress = privateIpAddress;
    }

    public String getPrivateMacAddress() {
        return privateMacAddress;
    }

    public void setPrivateMacAddress(final String privateMacAddress) {
        this.privateMacAddress = privateMacAddress;
    }

    public String getPrivateNetmask() {
        return privateNetmask;
    }

    public void setPrivateNetmask(final String privateNetmask) {
        this.privateNetmask = privateNetmask;
    }

    public String getStorageIpAddress() {
        return storageIpAddress;
    }

    public void setStorageIpAddress(final String storageIpAddress) {
        this.storageIpAddress = storageIpAddress;
    }

    public String getStorageNetmask() {
        return storageNetmask;
    }

    public void setStorageNetmask(final String storageNetmask) {
        this.storageNetmask = storageNetmask;
    }

    public String getStorageMacAddress() {
        return storageMacAddress;
    }

    public void setStorageMacAddress(final String storageMacAddress) {
        this.storageMacAddress = storageMacAddress;
    }

    public String getPublicIpAddress() {
        return publicIpAddress;
    }

    public void setPublicIpAddress(final String publicIpAddress) {
        this.publicIpAddress = publicIpAddress;
    }

    public String getAgentTag() {
        return agentTag;
    }

    public void setAgentTag(final String tag) {
        agentTag = tag;
    }

    public String getGuidWithoutResource() {
        if (resourceName == null) {
            return guid;
        } else {
            final int hyph = guid.lastIndexOf('-');
            if (hyph == -1) {
                return guid;
            }
            final String tmpResource = guid.substring(hyph + 1, guid.length());
            if (resourceName.equals(tmpResource)) {
                return guid.substring(0, hyph);
            } else {
                return guid;
            }
        }
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(final String resourceName) {
        this.resourceName = resourceName;
    }

    public String getGatewayIpAddress() {
        return gatewayIpAddress;
    }

    public void setGatewayIpAddress(final String gatewayIpAddress) {
        this.gatewayIpAddress = gatewayIpAddress;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
