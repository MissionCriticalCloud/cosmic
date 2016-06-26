package org.apache.cloudstack.api;

import static org.junit.Assert.assertEquals;

import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;

import org.junit.Test;

public class BaseCmdTest {

    protected static final String CMD1_NAME = "Cmd1Name";
    protected static final String CMD2_NAME = "Cmd2Name";
    protected static final String CMD1_RESPONSE = "cmd1response";
    protected static final String CMD2_RESPONSE = "cmd2response";
    private static final String NON_EXPECTED_COMMAND_NAME = "Non expected command name";

    @Test
    public void testGetActualCommandName() {
        final BaseCmd cmd1 = new Cmd1();
        final BaseCmd cmd2 = new Cmd2();

        assertEquals(NON_EXPECTED_COMMAND_NAME, CMD1_NAME, cmd1.getActualCommandName());
        assertEquals(NON_EXPECTED_COMMAND_NAME, CMD2_NAME, cmd2.getActualCommandName());
    }
}

@APICommand(name = BaseCmdTest.CMD1_NAME, responseObject = BaseResponse.class)
class Cmd1 extends BaseCmd {
    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException, ServerApiException, ConcurrentOperationException, ResourceAllocationException,
            NetworkRuleConflictException {
    }

    @Override
    public String getCommandName() {
        return BaseCmdTest.CMD1_RESPONSE;
    }

    @Override
    public long getEntityOwnerId() {
        return 0;
    }
}

@APICommand(name = BaseCmdTest.CMD2_NAME, responseObject = BaseResponse.class)
class Cmd2 extends Cmd1 {
    @Override
    public String getCommandName() {
        return BaseCmdTest.CMD2_RESPONSE;
    }
}
