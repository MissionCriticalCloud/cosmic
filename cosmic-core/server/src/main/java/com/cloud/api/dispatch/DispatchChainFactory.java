package com.cloud.api.dispatch;

import com.cloud.user.AccountManager;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

public class DispatchChainFactory {

    @Inject
    protected AccountManager _accountMgr;

    @Inject
    protected ParamGenericValidationWorker paramGenericValidationWorker;

    @Inject
    protected ParamUnpackWorker paramUnpackWorker;

    @Inject
    protected ParamProcessWorker paramProcessWorker;

    @Inject
    protected SpecificCmdValidationWorker specificCmdValidationWorker;

    @Inject
    protected CommandCreationWorker commandCreationWorker;

    protected DispatchChain standardDispatchChain;

    protected DispatchChain asyncCreationDispatchChain;

    @PostConstruct
    public void setup() {
        standardDispatchChain = new DispatchChain().
                                                           add(paramUnpackWorker).
                                                           add(paramProcessWorker).
                                                           add(paramGenericValidationWorker).
                                                           add(specificCmdValidationWorker);

        asyncCreationDispatchChain = new DispatchChain().
                                                                add(paramUnpackWorker).
                                                                add(paramProcessWorker).
                                                                add(paramGenericValidationWorker).
                                                                add(specificCmdValidationWorker).
                                                                add(commandCreationWorker);
    }

    public DispatchChain getStandardDispatchChain() {
        return standardDispatchChain;
    }

    public DispatchChain getAsyncCreationDispatchChain() {
        return asyncCreationDispatchChain;
    }
}
