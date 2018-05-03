package com.cloud.utils.fsm;

import com.cloud.legacymodel.exceptions.NoTransitionException;
import com.cloud.legacymodel.statemachine.StateObject;

public class StateMachine2Transitions<S, E, V extends StateObject<S>> {
    StateMachine2<S, E, V> stateMachine2;

    public StateMachine2Transitions(StateMachine2<S, E, V> stateMachine2) {
        this.stateMachine2 = stateMachine2;
    }

    public boolean transitTo(final V vo, final E e, final Object opaque, final StateDao<S, E, V> dao) throws NoTransitionException {
        final S currentState = vo.getState();
        final S nextState = stateMachine2.getNextState(currentState, e);
        final Transition<S, E> transition = stateMachine2.getTransition(currentState, e);

        boolean transitionStatus = true;
        if (nextState == null) {
            transitionStatus = false;
        }

        for (final StateListener<S, E, V> listener : stateMachine2.getListeners()) {
            listener.preStateTransitionEvent(currentState, e, nextState, vo, transitionStatus, opaque);
        }

        transitionStatus = dao.updateState(currentState, e, nextState, vo, opaque);
        if (!transitionStatus) {
            return false;
        }

        for (final StateListener<S, E, V> listener : stateMachine2.getListeners()) {
            listener.postStateTransitionEvent(transition, vo, transitionStatus, opaque);
        }

        return true;
    }
}
