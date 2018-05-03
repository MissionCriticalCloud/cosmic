package com.cloud.utils.fsm;

public interface FiniteStateObject<S> {

    void setState(S state);
}
