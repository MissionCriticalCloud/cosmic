package com.cloud.ratelimit;

import com.cloud.api.response.ApiLimitResponse;
import com.cloud.legacymodel.user.Account;
import com.cloud.utils.component.PluggableService;

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
