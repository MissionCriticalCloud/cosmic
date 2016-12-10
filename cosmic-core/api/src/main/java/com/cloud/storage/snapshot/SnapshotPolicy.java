package com.cloud.storage.snapshot;

import com.cloud.api.Displayable;
import com.cloud.api.Identity;
import com.cloud.api.InternalIdentity;

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
