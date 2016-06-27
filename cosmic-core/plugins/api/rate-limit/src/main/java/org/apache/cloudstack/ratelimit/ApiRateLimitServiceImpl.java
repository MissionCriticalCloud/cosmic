package org.apache.cloudstack.ratelimit;

import com.cloud.configuration.Config;
import com.cloud.exception.PermissionDeniedException;
import com.cloud.exception.RequestLimitException;
import com.cloud.user.Account;
import com.cloud.user.AccountService;
import com.cloud.user.User;
import com.cloud.utils.component.AdapterBase;
import org.apache.cloudstack.acl.APIChecker;
import org.apache.cloudstack.api.command.admin.ratelimit.ResetApiLimitCmd;
import org.apache.cloudstack.api.command.user.ratelimit.GetApiLimitCmd;
import org.apache.cloudstack.api.response.ApiLimitResponse;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ApiRateLimitServiceImpl extends AdapterBase implements APIChecker, ApiRateLimitService {
    private static final Logger s_logger = LoggerFactory.getLogger(ApiRateLimitServiceImpl.class);
    @Inject
    AccountService _accountService;
    @Inject
    ConfigurationDao _configDao;
    /**
     * True if api rate limiting is enabled
     */
    private boolean enabled = false;
    /**
     * Fixed time duration where api rate limit is set, in seconds
     */
    private int timeToLive = 1;
    /**
     * Max number of api requests during timeToLive duration.
     */
    private int maxAllowed = 30;
    private LimitStore _store = null;

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        super.configure(name, params);

        if (_store == null) {
            // get global configured duration and max values
            final String isEnabled = _configDao.getValue(Config.ApiLimitEnabled.key());
            if (isEnabled != null) {
                enabled = Boolean.parseBoolean(isEnabled);
            }
            final String duration = _configDao.getValue(Config.ApiLimitInterval.key());
            if (duration != null) {
                timeToLive = Integer.parseInt(duration);
            }
            final String maxReqs = _configDao.getValue(Config.ApiLimitMax.key());
            if (maxReqs != null) {
                maxAllowed = Integer.parseInt(maxReqs);
            }
            // create limit store
            final EhcacheLimitStore cacheStore = new EhcacheLimitStore();
            int maxElements = 10000;
            final String cachesize = _configDao.getValue(Config.ApiLimitCacheSize.key());
            if (cachesize != null) {
                maxElements = Integer.parseInt(cachesize);
            }
            final CacheManager cm = CacheManager.create();
            final Cache cache = new Cache("api-limit-cache", maxElements, false, false, timeToLive, timeToLive);
            cm.addCache(cache);
            s_logger.info("Limit Cache created with timeToLive=" + timeToLive + ", maxAllowed=" + maxAllowed + ", maxElements=" + maxElements);
            cacheStore.setCache(cache);
            _store = cacheStore;
        }

        return true;
    }

    @Override
    public ApiLimitResponse searchApiLimit(final Account caller) {
        final ApiLimitResponse response = new ApiLimitResponse();
        response.setAccountId(caller.getUuid());
        response.setAccountName(caller.getAccountName());
        StoreEntry entry = _store.get(caller.getId());
        if (entry == null) {

            /* Populate the entry, thus unlocking any underlying mutex */
            entry = _store.create(caller.getId(), timeToLive);
            response.setApiIssued(0);
            response.setApiAllowed(maxAllowed);
            response.setExpireAfter(timeToLive);
        } else {
            response.setApiIssued(entry.getCounter());
            response.setApiAllowed(maxAllowed - entry.getCounter());
            response.setExpireAfter(entry.getExpireDuration());
        }

        return response;
    }

    @Override
    public boolean resetApiLimit(final Long accountId) {
        if (accountId != null) {
            _store.create(accountId, timeToLive);
        } else {
            _store.resetCounters();
        }
        return true;
    }

    @Override
    public void setTimeToLive(final int timeToLive) {
        this.timeToLive = timeToLive;
    }

    @Override
    public void setMaxAllowed(final int max) {
        maxAllowed = max;
    }

    @Override
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean checkAccess(final User user, final String apiCommandName) throws PermissionDeniedException {
        // check if api rate limiting is enabled or not
        if (!enabled) {
            return true;
        }
        final Long accountId = user.getAccountId();
        final Account account = _accountService.getAccount(accountId);
        if (_accountService.isRootAdmin(account.getId())) {
            // no API throttling on root admin
            return true;
        }
        StoreEntry entry = _store.get(accountId);

        if (entry == null) {

            /* Populate the entry, thus unlocking any underlying mutex */
            entry = _store.create(accountId, timeToLive);
        }

        /* Increment the client count and see whether we have hit the maximum allowed clients yet. */
        final int current = entry.incrementAndGet();

        if (current <= maxAllowed) {
            s_logger.trace("account (" + account.getAccountId() + "," + account.getAccountName() + ") has current count = " + current);
            return true;
        } else {
            final long expireAfter = entry.getExpireDuration();
            // for this exception, we can just show the same message to user and admin users.
            final String msg = "The given user has reached his/her account api limit, please retry after " + expireAfter + " ms.";
            s_logger.warn(msg);
            throw new RequestLimitException(msg);
        }
    }

    @Override
    public List<Class<?>> getCommands() {
        final List<Class<?>> cmdList = new ArrayList<>();
        cmdList.add(ResetApiLimitCmd.class);
        cmdList.add(GetApiLimitCmd.class);
        return cmdList;
    }
}
