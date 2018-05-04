package com.cloud.api.command.admin.user;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

import com.cloud.api.ResponseGenerator;
import com.cloud.api.response.UserResponse;
import com.cloud.context.CallContext;
import com.cloud.legacymodel.user.UserAccount;
import com.cloud.user.AccountService;
import com.cloud.legacymodel.exceptions.InvalidParameterValueException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetUserCmdTest {
    public static final Logger s_logger = LoggerFactory.getLogger(GetUserCmdTest.class.getName());

    @Mock
    private AccountService accountService;

    @Mock
    private ResponseGenerator responseGenerator;

    @Mock
    private UserAccount userAccount;

    @InjectMocks
    private final GetUserCmd getUserCmd = new GetUserCmd();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() throws Exception {
        CallContext.unregister();
    }

    @Test(expected = InvalidParameterValueException.class)
    public void testExecuteWithUnknownApikey() {
        userAccount = null;

        when(accountService.getUserByApiKey(null)).thenReturn(userAccount);

        getUserCmd.execute();
    }

    @Test
    public void testExecuteWithDefault() {
        final String secretKey = "Very secret key";
        UserResponse response = new UserResponse();
        response.setSecretKey(secretKey);

        when(accountService.getUserByApiKey(null)).thenReturn(userAccount);
        when(responseGenerator.createUserResponse(userAccount)).thenReturn(response);

        getUserCmd.execute();

        response = (UserResponse) getUserCmd.getResponseObject();
        assertFalse("SecretKey was revealed in ResponseObject, wasn't masked", secretKey.equals(response.getSecretKey()));
    }
}
