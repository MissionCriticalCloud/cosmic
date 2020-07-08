package com.cloud.network.nicira;

import java.util.ArrayList;
import java.util.List;

public class LogicalSwitchPort extends BaseNiciraNamedEntity {
    private final String type = "LogicalSwitchPortConfig";
    private Integer portno;
    private boolean adminStatusEnabled;
    private String queueUuid;
    private List<String> securityProfiles;
    private List<NiciraNvpCollectorConfig> mirrorTargets = new ArrayList<>();

    private Boolean macLearning;

    public LogicalSwitchPort() {
        super();
    }

    public LogicalSwitchPort(final String displayName, final List<NiciraNvpTag> tags, final boolean adminStatusEnabled, final Boolean macLearning, final List<String> mirrorIpAddress, final List<String> mirrorKeyList) {
        super();
        this.displayName = displayName;
        this.tags = tags;
        this.adminStatusEnabled = adminStatusEnabled;
        this.macLearning = macLearning;

        int counter = 0;
        long mirror_key;
        for (final String mirror_ip_address : mirrorIpAddress) {
            try {
                mirror_key = Long.parseLong(mirrorKeyList.get(counter));
            } catch (final IndexOutOfBoundsException | NumberFormatException ex) {
                mirror_key = counter;
            }
            if (mirror_ip_address != null && !mirror_ip_address.equals("")) {
                final NiciraNvpCollectorConfig mirrorTarget = new NiciraNvpCollectorConfig();
                mirrorTarget.setIpAddress(mirror_ip_address);
                mirrorTarget.setMirrorKey(mirror_key);

                this.mirrorTargets.add(mirrorTarget);
                counter++;
            }
        }
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

    public List<NiciraNvpCollectorConfig> getMirrorTargets() {
        return mirrorTargets;
    }

    public void setMirrorTargets(final List<NiciraNvpCollectorConfig> mirrorTargets) {
        this.mirrorTargets = mirrorTargets;
    }

    public Boolean getMacLearning() {
        return macLearning;
    }

    public void setMacLearning(final Boolean macLearning) {
        this.macLearning = macLearning;
    }
}
