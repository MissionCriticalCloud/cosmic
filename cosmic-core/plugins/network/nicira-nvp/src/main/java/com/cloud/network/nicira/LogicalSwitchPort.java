//

//

package com.cloud.network.nicira;

import java.util.List;

public class LogicalSwitchPort extends BaseNiciraNamedEntity {
    private final String type = "LogicalSwitchPortConfig";
    private Integer portno;
    private boolean adminStatusEnabled;
    private String queueUuid;
    private List<String> securityProfiles;
    private List<String> mirrorTargets;

    public LogicalSwitchPort() {
        super();
    }

    public LogicalSwitchPort(final String displayName, final List<NiciraNvpTag> tags, final boolean adminStatusEnabled) {
        super();
        this.displayName = displayName;
        this.tags = tags;
        this.adminStatusEnabled = adminStatusEnabled;
    }

    public Integer getPortno() {
        return portno;
    }

    public void setPortno(final Integer portno) {
        this.portno = portno;
    }

    public boolean isAdminStatusEnabled() {
        return adminStatusEnabled;
    }

    public void setAdminStatusEnabled(final boolean adminStatusEnabled) {
        this.adminStatusEnabled = adminStatusEnabled;
    }

    public String getQueueUuid() {
        return queueUuid;
    }

    public void setQueueUuid(final String queueUuid) {
        this.queueUuid = queueUuid;
    }

    public List<String> getSecurityProfiles() {
        return securityProfiles;
    }

    public void setSecurityProfiles(final List<String> securityProfiles) {
        this.securityProfiles = securityProfiles;
    }

    public List<String> getMirrorTargets() {
        return mirrorTargets;
    }

    public void setMirrorTargets(final List<String> mirrorTargets) {
        this.mirrorTargets = mirrorTargets;
    }
}
