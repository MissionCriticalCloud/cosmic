package com.cloud.api.command.admin.user;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

import com.cloud.api.response.ListResponse;
import com.cloud.api.response.UserResponse;
import com.cloud.context.CallContext;
import com.cloud.query.QueryService;
import com.cloud.utils.StringUtils;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListUsersCmdTest {
    public static final Logger s_logger = LoggerFactory.getLogger(ListUsersCmdTest.class.getName());

    @Mock
    private QueryService queryService;

    @InjectMocks
    private final ListUsersCmd listUsersCmd = new ListUsersCmd();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() throws Exception {
        CallContext.unregister();
    }

    @Test
    public void testExecuteWithDefault() {
        final String secretKey = "Very secret key";
        ListResponse<UserResponse> responseList = new ListResponse<>();
        UserResponse response = new UserResponse();

        response.setSecretKey(secretKey);
        responseList.setResponses(Arrays.asList(response));
        when(queryService.searchForUsers(listUsersCmd)).thenReturn(responseList);

        listUsersCmd.execute();
        responseList = (ListResponse)listUsersCmd.getResponseObject();
        response = responseList.getResponses().get(0);

        assertFalse("SecretKey was revealed in ResponseObject, wasn't masked", secretKey.equals(response.getSecretKey()));
    }

    @Test
    public void testExecuteWithEmptySecretKey() {
        ListResponse<UserResponse> responseList = new ListResponse<>();
        UserResponse response = new UserResponse();

        responseList.setResponses(Arrays.asList(response));
        when(queryService.searchForUsers(listUsersCmd)).thenReturn(responseList);

        listUsersCmd.execute();
        responseList = (ListResponse)listUsersCmd.getResponseObject();
        response = responseList.getResponses().get(0);

        assertFalse("Empty SecretKey should be left empty", StringUtils.isNotBlank(response.getSecretKey()));
    }
}
