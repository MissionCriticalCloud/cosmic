package com.cloud.server;

import org.apache.cloudstack.acl.ControlledEntity;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

public interface ResourceTag extends ControlledEntity, Identity, InternalIdentity {

    /**
     * @return
     */
    String getKey();

    /**
     * @return
     */
    String getValue();

    /**
     * @return
     */
    long getResourceId();

    void setResourceId(long resourceId);

    /**
     * @return
     */
    ResourceObjectType getResourceType();

    /**
     * @return
     */
    String getCustomer();

    /**
     * @return
     */
    String getResourceUuid();

    // FIXME - extract enum to another interface as its used both by resourceTags and resourceMetaData code
    public enum ResourceObjectType {
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
        SecurityGroup(true, false),
        SecurityGroupRule(true, false),
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
        AutoScaleVmProfile(false, true),
        AutoScaleVmGroup(false, true),
        LBStickinessPolicy(false, true),
        LBHealthCheckPolicy(false, true),
        SnapshotPolicy(false, true);

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
