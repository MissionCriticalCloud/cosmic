package org.apache.cloudstack.storage.snapshot;

import com.cloud.storage.Snapshot;
import com.cloud.storage.Snapshot.Event;
import com.cloud.storage.Snapshot.State;
import com.cloud.storage.SnapshotVO;
import com.cloud.storage.dao.SnapshotDao;
import com.cloud.storage.listener.SnapshotStateListener;
import com.cloud.utils.fsm.NoTransitionException;
import com.cloud.utils.fsm.StateMachine2;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

@Component
public class SnapshotStateMachineManagerImpl implements SnapshotStateMachineManager {
    @Inject
    protected SnapshotDao snapshotDao;
    private final StateMachine2<State, Event, SnapshotVO> stateMachine = new StateMachine2<>();

    public SnapshotStateMachineManagerImpl() {
        stateMachine.addTransition(Snapshot.State.Allocated, Event.CreateRequested, Snapshot.State.Creating);
        stateMachine.addTransition(Snapshot.State.Creating, Event.OperationSucceeded, Snapshot.State.CreatedOnPrimary);
        stateMachine.addTransition(Snapshot.State.Creating, Event.OperationNotPerformed, Snapshot.State.BackedUp);
        stateMachine.addTransition(Snapshot.State.Creating, Event.OperationFailed, Snapshot.State.Error);
        stateMachine.addTransition(Snapshot.State.CreatedOnPrimary, Event.BackupToSecondary, Snapshot.State.BackingUp);
        stateMachine.addTransition(Snapshot.State.CreatedOnPrimary, Event.OperationNotPerformed, Snapshot.State.BackedUp);
        stateMachine.addTransition(Snapshot.State.BackingUp, Event.OperationSucceeded, Snapshot.State.BackedUp);
        stateMachine.addTransition(Snapshot.State.BackingUp, Event.OperationFailed, Snapshot.State.Error);
        stateMachine.addTransition(Snapshot.State.BackedUp, Event.DestroyRequested, Snapshot.State.Destroying);
        stateMachine.addTransition(Snapshot.State.BackedUp, Event.CopyingRequested, Snapshot.State.Copying);
        stateMachine.addTransition(Snapshot.State.Copying, Event.OperationSucceeded, Snapshot.State.BackedUp);
        stateMachine.addTransition(Snapshot.State.Copying, Event.OperationFailed, Snapshot.State.BackedUp);
        stateMachine.addTransition(Snapshot.State.Destroying, Event.OperationSucceeded, Snapshot.State.Destroyed);
        stateMachine.addTransition(Snapshot.State.Destroying, Event.OperationFailed, State.BackedUp);

        stateMachine.registerListener(new SnapshotStateListener());
    }

    @Override
    public void processEvent(final SnapshotVO snapshot, final Event event) throws NoTransitionException {
        stateMachine.transitTo(snapshot, event, null, snapshotDao);
    }
}
