package com.cloud.api.dispatch;

import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.user.User;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.context.CallContext;

import java.util.HashMap;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ParamProcessWorkerTest {

    @Mock
    protected AccountManager accountManager;

    protected ParamProcessWorker paramProcessWorker;

    @Before
    public void setup() {
        CallContext.register(Mockito.mock(User.class), Mockito.mock(Account.class));
        paramProcessWorker = new ParamProcessWorker();
        paramProcessWorker._accountMgr = accountManager;
    }

    @After
    public void cleanup() {
        CallContext.unregister();
    }

    @Test
    public void processParameters() {
        final HashMap<String, String> params = new HashMap<>();
        params.put("strparam1", "foo");
        params.put("intparam1", "100");
        params.put("boolparam1", "true");
        params.put("doubleparam1", "11.89");
        final TestCmd cmd = new TestCmd();
        paramProcessWorker.processParameters(cmd, params);
        Assert.assertEquals("foo", cmd.strparam1);
        Assert.assertEquals(100, cmd.intparam1);
        Assert.assertTrue(Double.compare(cmd.doubleparam1, 11.89) == 0);
    }

    public static class TestCmd extends BaseCmd {

        @Parameter(name = "strparam1")
        String strparam1;

        @Parameter(name = "intparam1", type = CommandType.INTEGER)
        int intparam1;

        @Parameter(name = "boolparam1", type = CommandType.BOOLEAN)
        boolean boolparam1;

        @Parameter(name = "doubleparam1", type = CommandType.DOUBLE)
        double doubleparam1;

        @Override
        public void execute() throws ResourceUnavailableException, InsufficientCapacityException, ServerApiException, ConcurrentOperationException,
                ResourceAllocationException, NetworkRuleConflictException {
            // well documented nothing
        }

        @Override
        public String getCommandName() {
            return "test";
        }

        @Override
        public long getEntityOwnerId() {
            return 0;
        }
    }
}
