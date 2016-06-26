//

//

package com.cloud.network.nicira;

public class ExecutionCounter {

    private final int executionLimit;
    private final ThreadLocal<Integer> executionCount;

    public ExecutionCounter(final int executionLimit) {
        this.executionLimit = executionLimit;
        executionCount = new ThreadLocal<Integer>() {
            @Override
            protected Integer initialValue() {
                return new Integer(0);
            }
        };
    }

    public ExecutionCounter resetExecutionCounter() {
        executionCount.set(0);
        return this;
    }

    public boolean hasReachedExecutionLimit() {
        return executionCount.get() >= executionLimit;
    }

    public ExecutionCounter incrementExecutionCounter() {
        executionCount.set(executionCount.get() + 1);
        return this;
    }

    public int getValue() {
        return executionCount.get();
    }
}
