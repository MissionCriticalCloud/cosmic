package com.cloud.utils.db;

import com.cloud.utils.Profiler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/testContext.xml")
public class GlobalLockTest {
    public static final Logger s_logger = LoggerFactory.getLogger(GlobalLockTest.class);
    private final static GlobalLock WorkLock = GlobalLock.getInternLock("SecurityGroupWork");

    @Test
    public void testTimeout() {
        final Thread[] pool = new Thread[50];
        for (int i = 0; i < pool.length; i++) {
            pool[i] = new Thread(new Worker(i, 5, 3));
        }
        for (int i = 0; i < pool.length; i++) {
            pool[i].start();
        }
        for (int i = 0; i < pool.length; i++) {
            try {
                pool[i].join();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static class Worker implements Runnable {
        int id = 0;
        int timeoutSeconds = 10;
        int jobDuration = 2;

        public Worker(final int id, final int timeout, final int duration) {
            this.id = id;
            timeoutSeconds = timeout;
            jobDuration = duration;
        }

        @Override
        public void run() {
            boolean locked = false;
            try {
                final Profiler p = new Profiler();
                p.start();
                locked = WorkLock.lock(timeoutSeconds);
                p.stop();
                System.out.println("Thread " + id + " waited " + p.getDurationInMillis() + " ms, locked=" + locked);
                if (locked) {
                    Thread.sleep(jobDuration * 1000);
                }
            } catch (final InterruptedException e) {
                s_logger.debug("[ignored] interupted while testing global lock.");
            } finally {
                if (locked) {
                    final boolean unlocked = WorkLock.unlock();
                    System.out.println("Thread " + id + "  unlocked=" + unlocked);
                }
            }
        }
    }
}
