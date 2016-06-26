//

//

package com.cloud.utils.fsm;

import java.util.Set;

public interface FiniteState2<T, E> {

    StateMachine2<T, ? extends ChangeEvent, ? extends StateObject<?>> getStateMachine();

    T getNextState(ChangeEvent e) throws NoTransitionException;

    T getFromStates(ChangeEvent e);

    Set<ChangeEvent> getPossibleEvents();
}
