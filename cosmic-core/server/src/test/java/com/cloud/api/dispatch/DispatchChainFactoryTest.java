package com.cloud.api.dispatch;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DispatchChainFactoryTest {

    protected static final String STANDARD_CHAIN_ERROR = "Expecting worker of class %s at index %s of StandardChain";
    protected static final String ASYNC_CHAIN_ERROR = "Expecting worker of class %s at index %s of StandardChain";

    @Test
    public void testAllChainCreation() {
        // Prepare
        final DispatchChainFactory dispatchChainFactory = new DispatchChainFactory();
        dispatchChainFactory.paramGenericValidationWorker = new ParamGenericValidationWorker();
        dispatchChainFactory.specificCmdValidationWorker = new SpecificCmdValidationWorker();
        dispatchChainFactory.paramProcessWorker = new ParamProcessWorker();
        dispatchChainFactory.commandCreationWorker = new CommandCreationWorker();
        dispatchChainFactory.paramUnpackWorker = new ParamUnpackWorker();

        final Class<?>[] standardClasses = {ParamUnpackWorker.class, ParamProcessWorker.class,
                ParamGenericValidationWorker.class, SpecificCmdValidationWorker.class};
        final Class<?>[] asyncClasses = {ParamUnpackWorker.class, ParamProcessWorker.class,
                ParamGenericValidationWorker.class, SpecificCmdValidationWorker.class, CommandCreationWorker.class};

        // Execute
        dispatchChainFactory.setup();
        final DispatchChain standardChain = dispatchChainFactory.getStandardDispatchChain();
        final DispatchChain asyncChain = dispatchChainFactory.getAsyncCreationDispatchChain();
        for (int i = 0; i < standardClasses.length; i++) {
            assertEquals(String.format(STANDARD_CHAIN_ERROR, standardClasses[i], i),
                    standardClasses[i], standardChain.workers.get(i).getClass());
        }
        for (int i = 0; i < asyncClasses.length; i++) {
            assertEquals(String.format(ASYNC_CHAIN_ERROR, asyncClasses[i], i),
                    asyncClasses[i], asyncChain.workers.get(i).getClass());
        }
    }
}
