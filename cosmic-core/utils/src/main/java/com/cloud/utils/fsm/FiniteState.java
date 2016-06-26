//

//

package com.cloud.utils.fsm;

import java.util.List;
import java.util.Set;

/**
 * @param <S> State
 * @param <E> Event
 */
public interface FiniteState<S, E> {
    /**
     * @return the state machine being used.
     */
    StateMachine<S, E> getStateMachine();

    /**
     * get next state based on the event.
     *
     * @param event
     * @return next State
     */
    S getNextState(E event);

    /**
     * Get the states that could have traveled to the current state
     * via this event.
     *
     * @param event
     * @return array of states
     */
    List<S> getFromStates(E event);

    /**
     * Get the possible events that can happen from the current state.
     *
     * @return array of events.
     */
    Set<E> getPossibleEvents();

    String getDescription();
}
