//

//

package com.cloud.utils;

import com.cloud.utils.testcase.Log4jEnabledTestCase;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Profiler.class})
public class TestProfiler extends Log4jEnabledTestCase {

    private static final long SLEEP_TIME_NANO = 1000000000L;
    private static Profiler pf;

    @Before
    public void setUp() {
        pf = new Profiler();
        PowerMockito.mockStatic(System.class);
        PowerMockito.when(System.nanoTime()).thenReturn(0L, SLEEP_TIME_NANO);
    }

    @Test
    public void testProfilerInMillis() {
        //Given
        final long sleepTimeMillis = SLEEP_TIME_NANO / 1000000L;

        //When
        pf.start();
        pf.stop();

        //Then
        Assert.assertTrue(pf.getDurationInMillis() == sleepTimeMillis);
    }

    @Test
    public void testProfilerInNano() {
        //Given
        final long sleepTimeNano = SLEEP_TIME_NANO;

        //When
        pf.start();
        pf.stop();

        //Then
        Assert.assertTrue(pf.getDuration() == sleepTimeNano);
    }

    @Test
    public void testProfilerNoStart() {
        //Given
        final long expectedAnswer = -1;

        //When
        pf.stop();

        //Then
        Assert.assertTrue(pf.getDurationInMillis() == expectedAnswer);
        Assert.assertFalse(pf.isStarted());
    }

    @Test
    public void testProfilerNoStop() {
        //Given
        final long expectedAnswer = -1;

        //When
        pf.start();

        //Then
        Assert.assertTrue(pf.getDurationInMillis() == expectedAnswer);
        Assert.assertFalse(pf.isStopped());
    }
}
