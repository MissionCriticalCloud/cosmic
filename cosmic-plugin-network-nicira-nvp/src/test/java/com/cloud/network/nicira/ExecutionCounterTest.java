//

//

package com.cloud.network.nicira;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class ExecutionCounterTest {

    @Test
    public void testIncrementCounter() throws Exception {
        final ExecutionCounter executionCounter = new ExecutionCounter(-1);

        executionCounter.incrementExecutionCounter().incrementExecutionCounter();

        assertThat(executionCounter.getValue(), equalTo(2));
    }

    @Test
    public void testHasNotYetReachedTheExecutuionLimit() throws Exception {
        final ExecutionCounter executionCounter = new ExecutionCounter(2);

        executionCounter.incrementExecutionCounter();

        assertThat(executionCounter.hasReachedExecutionLimit(), equalTo(false));
    }

    @Test
    public void testHasAlreadyReachedTheExecutuionLimit() throws Exception {
        final ExecutionCounter executionCounter = new ExecutionCounter(2);

        executionCounter.incrementExecutionCounter().incrementExecutionCounter();

        assertThat(executionCounter.hasReachedExecutionLimit(), equalTo(true));
    }

    @Test
    public void testConcurrentUpdatesToCounter() throws Exception {
        final ExecutionCounter executionCounter = new ExecutionCounter(0);
        final ExecutorService executorService = Executors.newFixedThreadPool(3);
        final AtomicInteger counterTask1 = new AtomicInteger(-1);
        final AtomicInteger counterTask2 = new AtomicInteger(-1);
        final AtomicInteger counterTask3 = new AtomicInteger(-1);

        final Runnable task1 = new Runnable() {
            @Override
            public void run() {
                executionCounter.incrementExecutionCounter().incrementExecutionCounter();
                executionCounter.incrementExecutionCounter().incrementExecutionCounter();
                counterTask1.set(executionCounter.getValue());
            }
        };
        final Runnable task2 = new Runnable() {
            @Override
            public void run() {
                executionCounter.incrementExecutionCounter().incrementExecutionCounter();
                counterTask2.set(executionCounter.getValue());
            }
        };
        final Runnable task3 = new Runnable() {
            @Override
            public void run() {
                counterTask3.set(executionCounter.getValue());
            }
        };

        executorService.execute(task1);
        executorService.execute(task2);
        executorService.execute(task3);

        executorService.shutdown();
        executorService.awaitTermination(5L, TimeUnit.SECONDS);

        assertThat(counterTask1.get(), equalTo(4));
        assertThat(counterTask2.get(), equalTo(2));
        assertThat(counterTask3.get(), equalTo(0));
    }
}
