package org.apache.cloudstack.region;

import java.util.Date;

/**
 *
 */
public interface RegionSync {

    public long getId();

    public int getRegionId();

    public String getApi();

    Date getCreateDate();
}
