package org.apache.cloudstack.api.command.admin.account;

import com.cloud.user.Account;
import com.cloud.user.AccountService;
import com.cloud.user.User;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.context.CallContext;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

public class CreateAccountCmdTest {
    public static final Logger s_logger = LoggerFactory.getLogger(CreateAccountCmdTest.class.getName());

    @Mock
    private AccountService accountService;

    @InjectMocks
    private final CreateAccountCmd createAccountCmd = new CreateAccountCmd();

    private final short accountType = 1;
    private final Long domainId = 1L;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(createAccountCmd, "domainId", domainId);
        ReflectionTestUtils.setField(createAccountCmd, "accountType", accountType);
        CallContext.register(Mockito.mock(User.class), Mockito.mock(Account.class));
    }

    @After
    public void tearDown() throws Exception {
        CallContext.unregister();
    }

    @Test
    public void testExecuteWithNotBlankPassword() {
        ReflectionTestUtils.setField(createAccountCmd, "password", "Test");
        try {
            createAccountCmd.execute();
        } catch (final ServerApiException e) {
            Assert.assertTrue("Received exception as the mock accountService createUserAccount returns null user", true);
        }
        Mockito.verify(accountService, Mockito.times(1)).createUserAccount(null, "Test", null, null, null, null, null, accountType, domainId, null, null, null, null);
    }

    @Test
    public void testExecuteWithNullPassword() {
        ReflectionTestUtils.setField(createAccountCmd, "password", null);
        try {
            createAccountCmd.execute();
            Assert.fail("should throw exception for a null password");
        } catch (final ServerApiException e) {
            Assert.assertEquals(ApiErrorCode.PARAM_ERROR, e.getErrorCode());
            Assert.assertEquals("Empty passwords are not allowed", e.getMessage());
        }
        Mockito.verify(accountService, Mockito.never()).createUserAccount(null, null, null, null, null, null, null, accountType, domainId, null, null, null, null);
    }

    @Test
    public void testExecuteWithEmptyPassword() {
        ReflectionTestUtils.setField(createAccountCmd, "password", "");
        try {
            createAccountCmd.execute();
            Assert.fail("should throw exception for a empty password");
        } catch (final ServerApiException e) {
            Assert.assertEquals(ApiErrorCode.PARAM_ERROR, e.getErrorCode());
            Assert.assertEquals("Empty passwords are not allowed", e.getMessage());
        }
        Mockito.verify(accountService, Mockito.never()).createUserAccount(null, null, null, null, null, null, null, accountType, domainId, null, null, null, null);
    }
}
