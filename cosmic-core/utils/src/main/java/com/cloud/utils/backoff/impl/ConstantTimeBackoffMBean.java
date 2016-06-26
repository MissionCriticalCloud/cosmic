//

//

package com.cloud.utils.backoff.impl;

import com.cloud.utils.mgmt.ManagementBean;

import java.util.Collection;

public interface ConstantTimeBackoffMBean extends ManagementBean {
    public long getTimeToWait();

    public void setTimeToWait(long seconds);

    Collection<String> getWaiters();

    boolean wakeup(String threadName);
}
