package org.apache.cloudstack.acl;

import com.cloud.user.Account;
import com.cloud.utils.component.Adapter;
import org.apache.cloudstack.api.ServerApiException;

/**
 * APILimitChecker checks if we should block an API request based on pre-set account based api limit.
 */
public interface APILimitChecker extends Adapter {
    // Interface for checking if the account is over its api limit
    void checkLimit(Account account) throws ServerApiException;
}
