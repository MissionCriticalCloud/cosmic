package com.cloud.storage.snapshot;

import org.apache.cloudstack.api.Displayable;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

public interface SnapshotPolicy extends Identity, InternalIdentity, Displayable {

    long getVolumeId();

    String getSchedule();

    void setSchedule(String schedule);

    String getTimezone();

    void setTimezone(String timezone);

    short getInterval();

    void setInterval(short interval);

    int getMaxSnaps();

    void setMaxSnaps(int maxSnaps);

    boolean isActive();

    void setActive(boolean active);
}
