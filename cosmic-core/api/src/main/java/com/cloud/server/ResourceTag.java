package com.cloud.server;

import com.cloud.legacymodel.Identity;
import com.cloud.legacymodel.InternalIdentity;
import com.cloud.legacymodel.acl.ControlledEntity;

public interface ResourceTag extends ControlledEntity, Identity, InternalIdentity {

    String getKey();

    String getValue();

    long getResourceId();

    void setResourceId(long resourceId);

    ResourceObjectType getResourceType();

    String getCustomer();

    String getResourceUuid();

    // FIXME - extract enum to another interface as its used both by resourceTags and resourceMetaData code
    enum ResourceObjectType {
        UserVm(true, true),
        Template(true, true),
        ISO(true, false),
        Volume(true, true),
        Snapshot(true, false),
        Network(true, true),
        Nic(false, true),
        LoadBalancer(true, true),
        PortForwardingRule(true, true),
        FirewallRule(true, true),
        PublicIpAddress(true, true),
        Project(true, false),
        Vpc(true, true),
        NetworkACL(true, true),
        StaticRoute(true, false),
        VMSnapshot(true, false),
        RemoteAccessVpn(true, true),
        Zone(false, true),
        ServiceOffering(false, true),
        Storage(false, true),
        PrivateGateway(false, true),
        NetworkACLList(false, true),
        VpnGateway(false, true),
        CustomerGateway(false, true),
        VpnConnection(false, true),
        User(true, true),
        DiskOffering(false, true),
        LBStickinessPolicy(false, true),
        LBHealthCheckPolicy(false, true);

        private final boolean resourceTagsSupport;
        private final boolean metadataSupport;

        ResourceObjectType(final boolean resourceTagsSupport, final boolean resourceMetadataSupport) {
            this.resourceTagsSupport = resourceTagsSupport;
            metadataSupport = resourceMetadataSupport;
        }

        public boolean resourceTagsSupport() {
            return resourceTagsSupport;
        }

        public boolean resourceMetadataSupport() {
            return metadataSupport;
        }
    }
}
