package com.cloud.api.dispatch;

/**
 * This worker validates parameters in a semantic way, that is of
 * course specific for each {@link BaseCmd}, so actually it delegates
 * the validation on the {@link BaseCmd} itself
 */
public class SpecificCmdValidationWorker implements DispatchWorker {

    @Override
    public void handle(final DispatchTask task) {
        task.getCmd().validateSpecificParameters(task.getParams());
    }
}
