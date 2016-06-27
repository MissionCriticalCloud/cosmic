//

//

package com.cloud.utils.fsm;

import com.cloud.utils.SerialVersionUID;

/**
 * Thrown by the state machine when there is no transition from one state
 * to another.
 */
public class NoTransitionException extends Exception {

    private static final long serialVersionUID = SerialVersionUID.NoTransitionException;

    public NoTransitionException(final String msg) {
        super(msg);
    }
}
