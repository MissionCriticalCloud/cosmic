package com.cloud.api.dispatch;

import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCreateCmd;
import com.cloud.api.BaseCmd;
import com.cloud.api.ServerApiException;
import com.cloud.context.CallContext;
import com.cloud.exception.ResourceAllocationException;

/**
 * This worker invokes create on the {@link BaseCmd} itself
 *
 * @author afornie
 */
public class CommandCreationWorker implements DispatchWorker {

    private static final String ATTEMP_TO_CREATE_NON_CREATION_CMD =
            "Trying to invoke creation on a Command that is not " +
                    BaseAsyncCreateCmd.class.getName();

    @Override
    public void handle(final DispatchTask task) {
        final BaseCmd cmd = task.getCmd();

        if (cmd instanceof BaseAsyncCreateCmd) {
            try {
                CallContext.current().setEventDisplayEnabled(cmd.isDisplay());
                ((BaseAsyncCreateCmd) cmd).create();
            } catch (final ResourceAllocationException e) {
                throw new ServerApiException(ApiErrorCode.RESOURCE_ALLOCATION_ERROR,
                        e.getMessage(), e);
            }
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR,
                    ATTEMP_TO_CREATE_NON_CREATION_CMD);
        }
    }
}
