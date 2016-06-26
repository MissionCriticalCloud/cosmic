package org.apache.cloudstack.engine.subsystem.api.storage;

import com.cloud.utils.fsm.StateObject;

public interface ObjectInDataStoreStateMachine extends StateObject<ObjectInDataStoreStateMachine.State> {
    enum State {
        Allocated("The initial state"),
        Creating2("This is only used with createOnlyRequested event"),
        Creating("The object is being creating on data store"),
        Created("The object is created"),
        Ready("Template downloading is accomplished"),
        Copying("The object is being coping"),
        Migrating("The object is being migrated"),
        Destroying("Template is destroying"),
        Destroyed("Template is destroyed"),
        Failed("Failed to download template");
        String _description;

        private State(final String description) {
            _description = description;
        }

        public String getDescription() {
            return _description;
        }
    }

    enum Event {
        CreateRequested,
        CreateOnlyRequested,
        DestroyRequested,
        OperationSuccessed,
        OperationFailed,
        CopyingRequested,
        MigrationRequested,
        MigrationCopyRequested,
        MigrationCopySucceeded,
        MigrationCopyFailed,
        ResizeRequested,
        ExpungeRequested
    }
}
