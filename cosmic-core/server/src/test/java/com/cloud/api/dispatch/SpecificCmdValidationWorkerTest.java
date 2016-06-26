package com.cloud.api.dispatch;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.cloud.exception.ResourceAllocationException;
import org.apache.cloudstack.api.BaseCmd;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class SpecificCmdValidationWorkerTest {

    @Test
    public void testHandle() throws ResourceAllocationException {
        // Prepare
        final BaseCmd cmd = mock(BaseCmd.class);
        final Map<String, String> params = new HashMap<>();

        // Execute
        final SpecificCmdValidationWorker worker = new SpecificCmdValidationWorker();

        worker.handle(new DispatchTask(cmd, params));

        // Assert
        verify(cmd, times(1)).validateSpecificParameters(params);
    }
}
