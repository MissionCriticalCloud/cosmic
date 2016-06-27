//

//

package com.cloud.agent.api;

import java.util.Date;
import java.util.List;

public class DirectNetworkUsageCommand extends Command {

    private List<String> publicIps;
    private Date start;
    private Date end;
    private String includeZones;
    private String excludeZones;

    public DirectNetworkUsageCommand(final List<String> publicIps, final Date start, final Date end, final String includeZones, final String excludeZones) {
        this.setPublicIps(publicIps);
        this.setStart(start);
        this.setEnd(end);
        this.setIncludeZones(includeZones);
        this.setExcludeZones(excludeZones);
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }

    public List<String> getPublicIps() {
        return publicIps;
    }

    public void setPublicIps(final List<String> publicIps) {
        this.publicIps = publicIps;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(final Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(final Date end) {
        this.end = end;
    }

    public String getIncludeZones() {
        return includeZones;
    }

    public void setIncludeZones(final String includeZones) {
        this.includeZones = includeZones;
    }

    public String getExcludeZones() {
        return excludeZones;
    }

    public void setExcludeZones(final String excludeZones) {
        this.excludeZones = excludeZones;
    }
}
