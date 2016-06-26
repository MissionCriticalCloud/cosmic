package com.cloud.api.dispatch;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.cloud.exception.ResourceAllocationException;
import com.cloud.user.Account;
import com.cloud.user.AccountVO;
import com.cloud.user.User;
import com.cloud.user.UserVO;
import org.apache.cloudstack.api.BaseAsyncCreateCmd;
import org.apache.cloudstack.context.CallContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

public class CommandCreationWorkerTest {

    @Test
    public void testHandle() throws ResourceAllocationException {
        // Prepare
        final BaseAsyncCreateCmd asyncCreateCmd = mock(BaseAsyncCreateCmd.class);
        final Map<String, String> params = new HashMap<>();
        final Account account = new AccountVO("testaccount", 1L, "networkdomain", (short) 0, "uuid");
        final UserVO user = new UserVO(1, "testuser", "password", "firstname", "lastName", "email", "timezone", UUID.randomUUID().toString(), User.Source.UNKNOWN);
        CallContext.register(user, account);

        // Execute
        final CommandCreationWorker creationWorker = new CommandCreationWorker();

        creationWorker.handle(new DispatchTask(asyncCreateCmd, params));

        // Assert
        verify(asyncCreateCmd, times(1)).create();
    }
}
