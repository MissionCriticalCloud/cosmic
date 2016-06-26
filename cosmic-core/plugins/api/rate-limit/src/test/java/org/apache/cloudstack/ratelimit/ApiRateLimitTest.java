package org.apache.cloudstack.ratelimit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cloud.configuration.Config;
import com.cloud.exception.RequestLimitException;
import com.cloud.user.Account;
import com.cloud.user.AccountService;
import com.cloud.user.AccountVO;
import com.cloud.user.User;
import com.cloud.user.UserVO;
import org.apache.cloudstack.api.response.ApiLimitResponse;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;

import javax.naming.ConfigurationException;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ApiRateLimitTest {

    private static final long s_acctIdSeq = 5L;
    static ApiRateLimitServiceImpl s_limitService = new ApiRateLimitServiceImpl();
    static AccountService s_accountService = mock(AccountService.class);
    static ConfigurationDao s_configDao = mock(ConfigurationDao.class);
    private static Account s_testAccount;

    @BeforeClass
    public static void setUp() throws ConfigurationException {
        when(s_configDao.getValue(Config.ApiLimitInterval.key())).thenReturn(null);
        when(s_configDao.getValue(Config.ApiLimitMax.key())).thenReturn(null);
        when(s_configDao.getValue(Config.ApiLimitCacheSize.key())).thenReturn(null);
        when(s_configDao.getValue(Config.ApiLimitEnabled.key())).thenReturn("true"); // enable api rate limiting
        s_limitService._configDao = s_configDao;

        s_limitService.configure("ApiRateLimitTest", Collections.<String, Object>emptyMap());

        s_limitService._accountService = s_accountService;

        // Standard responses
        final AccountVO acct = new AccountVO(s_acctIdSeq);
        acct.setType(Account.ACCOUNT_TYPE_NORMAL);
        acct.setAccountName("demo");
        s_testAccount = acct;

        when(s_accountService.getAccount(5L)).thenReturn(s_testAccount);
        when(s_accountService.isRootAdmin(5L)).thenReturn(false);
    }

    @Before
    public void testSetUp() {
        // reset counter for each test
        s_limitService.resetApiLimit(null);
    }

    @Test
    public void sequentialApiAccess() {
        final int allowedRequests = 1;
        s_limitService.setMaxAllowed(allowedRequests);
        s_limitService.setTimeToLive(1);

        final User key = createFakeUser();
        assertTrue("Allow for the first request", isUnderLimit(key));

        assertFalse("Second request should be blocked, since we assume that the two api " + " accesses take less than a second to perform", isUnderLimit(key));
    }

    private User createFakeUser() {
        final UserVO user = new UserVO();
        user.setAccountId(s_acctIdSeq);
        return user;
    }

    private boolean isUnderLimit(final User key) {
        try {
            s_limitService.checkAccess(key, null);
            return true;
        } catch (final RequestLimitException ex) {
            return false;
        }
    }

    @Test
    public void canDoReasonableNumberOfApiAccessPerSecond() throws Exception {
        final int allowedRequests = 200;
        s_limitService.setMaxAllowed(allowedRequests);
        s_limitService.setTimeToLive(1);

        final User key = createFakeUser();

        for (int i = 0; i < allowedRequests; i++) {
            assertTrue("We should allow " + allowedRequests + " requests per second, but failed at request " + i, isUnderLimit(key));
        }

        assertFalse("We should block >" + allowedRequests + " requests per second", isUnderLimit(key));
    }

    @Test
    public void multipleClientsCanAccessWithoutBlocking() throws Exception {
        final int allowedRequests = 200;
        s_limitService.setMaxAllowed(allowedRequests);
        s_limitService.setTimeToLive(1);

        final User key = createFakeUser();

        final int clientCount = allowedRequests;
        final Runnable[] clients = new Runnable[clientCount];
        final boolean[] isUsable = new boolean[clientCount];

        final CountDownLatch startGate = new CountDownLatch(1);

        final CountDownLatch endGate = new CountDownLatch(clientCount);

        for (int i = 0; i < isUsable.length; ++i) {
            final int j = i;
            clients[j] = new Runnable() {

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void run() {
                    try {
                        startGate.await();

                        isUsable[j] = isUnderLimit(key);
                    } catch (final InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        endGate.countDown();
                    }
                }
            };
        }

        final ExecutorService executor = Executors.newFixedThreadPool(clientCount);

        for (final Runnable runnable : clients) {
            executor.execute(runnable);
        }

        startGate.countDown();

        endGate.await();

        for (final boolean b : isUsable) {
            assertTrue("Concurrent client request should be allowed within limit", b);
        }
    }

    @Test
    public void expiryOfCounterIsSupported() throws Exception {
        final int allowedRequests = 1;
        s_limitService.setMaxAllowed(allowedRequests);
        s_limitService.setTimeToLive(1);

        final User key = createFakeUser();

        assertTrue("The first request should be allowed", isUnderLimit(key));

        // Allow the token to expire
        Thread.sleep(1020);

        assertTrue("Another request after interval should be allowed as well", isUnderLimit(key));
    }

    @Test
    public void verifyResetCounters() throws Exception {
        final int allowedRequests = 1;
        s_limitService.setMaxAllowed(allowedRequests);
        s_limitService.setTimeToLive(1);

        final User key = createFakeUser();

        assertTrue("The first request should be allowed", isUnderLimit(key));

        assertFalse("Another request should be blocked", isUnderLimit(key));

        s_limitService.resetApiLimit(key.getAccountId());

        assertTrue("Another request should be allowed after reset counter", isUnderLimit(key));
    }

    @Test
    public void verifySearchCounter() throws Exception {
        final int allowedRequests = 10;
        s_limitService.setMaxAllowed(allowedRequests);
        s_limitService.setTimeToLive(1);

        final User key = createFakeUser();

        for (int i = 0; i < 5; i++) {
            assertTrue("Issued 5 requests", isUnderLimit(key));
        }

        final ApiLimitResponse response = s_limitService.searchApiLimit(s_testAccount);
        assertEquals("apiIssued is incorrect", 5, response.getApiIssued());
        assertEquals("apiAllowed is incorrect", 5, response.getApiAllowed());
        // using <= to account for inaccurate System.currentTimeMillis() clock in Windows environment
        assertTrue("expiredAfter is incorrect", response.getExpireAfter() <= 1000);
    }

    @Test
    public void disableApiLimit() throws Exception {
        try {
            final int allowedRequests = 200;
            s_limitService.setMaxAllowed(allowedRequests);
            s_limitService.setTimeToLive(1);
            s_limitService.setEnabled(false);

            final User key = createFakeUser();

            for (int i = 0; i < allowedRequests + 1; i++) {
                assertTrue("We should allow more than " + allowedRequests + " requests per second when api throttling is disabled.", isUnderLimit(key));
            }
        } finally {
            s_limitService.setEnabled(true); // enable api throttling to avoid
            // impacting other testcases
        }
    }
}
