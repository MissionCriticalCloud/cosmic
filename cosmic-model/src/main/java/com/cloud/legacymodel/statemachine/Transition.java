package com.cloud.legacymodel.statemachine;

import java.util.List;

public class Transition<S, E> {

    private final S currentState;

    private final E event;

    private final S toState;

    private final List<Impact> impacts;

    public Transition(final S currentState, final E event, final S toState, final List<Impact> impacts) {
        this.currentState = currentState;
        this.event = event;
        this.toState = toState;
        this.impacts = impacts;
    }

    public S getCurrentState() {
        return currentState;
    }

    public E getEvent() {
        return event;
    }

    public S getToState() {
        return toState;
    }

    public boolean isImpacted(final Impact impact) {
        if (impacts == null || impacts.isEmpty()) {
            return false;
        }
        return impacts.contains(impact);
    }

    @Override
    public int hashCode() {
        int result = currentState != null ? currentState.hashCode() : 0;
        result = 31 * result + (event != null ? event.hashCode() : 0);
        result = 31 * result + (toState != null ? toState.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Transition that = (Transition) o;

        if (currentState != null ? !currentState.equals(that.currentState) : that.currentState != null) {
            return false;
        }
        if (event != null ? !event.equals(that.event) : that.event != null) {
            return false;
        }
        return toState != null ? toState.equals(that.toState) : that.toState == null;
    }

    public enum Impact {
        USAGE
    }
}
