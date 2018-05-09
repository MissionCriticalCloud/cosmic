package com.cloud.ratelimit;

import com.cloud.api.response.ApiLimitResponse;
import com.cloud.legacymodel.user.Account;
import com.cloud.utils.component.PluggableService;

/**
 * Provide API rate limit service
 */
public interface ApiRateLimitService extends PluggableService {

    ApiLimitResponse searchApiLimit(Account caller);

    boolean resetApiLimit(Long accountId);

    void setTimeToLive(int timeToLive);

    void setMaxAllowed(int max);

    void setEnabled(boolean enabled);
}
