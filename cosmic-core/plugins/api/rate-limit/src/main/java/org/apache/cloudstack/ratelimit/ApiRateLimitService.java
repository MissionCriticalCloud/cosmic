package org.apache.cloudstack.ratelimit;

import com.cloud.user.Account;
import com.cloud.utils.component.PluggableService;
import org.apache.cloudstack.api.response.ApiLimitResponse;

/**
 * Provide API rate limit service
 */
public interface ApiRateLimitService extends PluggableService {

    public ApiLimitResponse searchApiLimit(Account caller);

    public boolean resetApiLimit(Long accountId);

    public void setTimeToLive(int timeToLive);

    public void setMaxAllowed(int max);

    public void setEnabled(boolean enabled);
}
