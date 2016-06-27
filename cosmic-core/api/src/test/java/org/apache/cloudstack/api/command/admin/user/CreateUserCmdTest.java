package org.apache.cloudstack.api.command.admin.user;

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

public class CreateUserCmdTest {
    public static final Logger s_logger = LoggerFactory.getLogger(CreateUserCmdTest.class.getName());

    @Mock
    private AccountService accountService;

    @InjectMocks
    private final CreateUserCmd createUserCmd = new CreateUserCmd();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        CallContext.register(Mockito.mock(User.class), Mockito.mock(Account.class));
    }

    @After
    public void tearDown() throws Exception {
        CallContext.unregister();
    }

    @Test
    public void testExecuteWithNotBlankPassword() {
        ReflectionTestUtils.setField(createUserCmd, "password", "Test");
        try {
            createUserCmd.execute();
        } catch (final ServerApiException e) {
            Assert.assertTrue("Received exception as the mock accountService createUser returns null user", true);
        }
        Mockito.verify(accountService, Mockito.times(1)).createUser(null, "Test", null, null, null, null, null, null, null);
    }

    @Test
    public void testExecuteWithNullPassword() {
        ReflectionTestUtils.setField(createUserCmd, "password", null);
        try {
            createUserCmd.execute();
            Assert.fail("should throw exception for a null password");
        } catch (final ServerApiException e) {
            Assert.assertEquals(ApiErrorCode.PARAM_ERROR, e.getErrorCode());
            Assert.assertEquals("Empty passwords are not allowed", e.getMessage());
        }
        Mockito.verify(accountService, Mockito.never()).createUser(null, null, null, null, null, null, null, null, null);
    }

    @Test
    public void testExecuteWithEmptyPassword() {
        ReflectionTestUtils.setField(createUserCmd, "password", "");
        try {
            createUserCmd.execute();
            Assert.fail("should throw exception for a empty password");
        } catch (final ServerApiException e) {
            Assert.assertEquals(ApiErrorCode.PARAM_ERROR, e.getErrorCode());
            Assert.assertEquals("Empty passwords are not allowed", e.getMessage());
        }
        Mockito.verify(accountService, Mockito.never()).createUser(null, null, null, null, null, null, null, null, null);
    }
}
