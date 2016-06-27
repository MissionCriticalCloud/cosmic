//

//

package com.cloud.utils.time;

import com.cloud.utils.concurrency.NamedThreadFactory;
import com.cloud.utils.mgmt.JmxUtil;

import javax.management.StandardMBean;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */

public class InaccurateClock extends StandardMBean implements InaccurateClockMBean {
    static final InaccurateClock s_timer = new InaccurateClock();
    private static final Logger s_logger = LoggerFactory.getLogger(InaccurateClock.class);
    static ScheduledExecutorService s_executor = null;
    private static long time;

    public InaccurateClock() {
        super(InaccurateClockMBean.class, false);
        time = System.currentTimeMillis();
        restart();
        try {
            JmxUtil.registerMBean("InaccurateClock", "InaccurateClock", this);
        } catch (final Exception e) {
            s_logger.warn("Unable to initialize inaccurate clock", e);
        }
    }

    @Override
    public synchronized String restart() {
        turnOff();
        s_executor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("InaccurateClock"));
        s_executor.scheduleAtFixedRate(new SetTimeTask(), 0, 60, TimeUnit.SECONDS);
        return "Restarted";
    }

    @Override
    public String turnOff() {
        if (s_executor != null) {
            try {
                s_executor.shutdown();
            } catch (final Throwable th) {
                s_logger.error("Unable to shutdown the Executor", th);
                return "Unable to turn off check logs";
            }
        }
        s_executor = null;
        return "Off";
    }

    @Override
    public long[] getCurrentTimes() {
        final long[] results = new long[2];
        results[0] = time;
        results[1] = System.currentTimeMillis();

        return results;
    }

    public static long getTime() {
        return s_executor != null ? time : System.currentTimeMillis();
    }

    public static long getTimeInSeconds() {
        return time / 1000;
    }

    protected class SetTimeTask implements Runnable {
        @Override
        public void run() {
            try {
                time = System.currentTimeMillis();
            } catch (final Throwable th) {
                s_logger.error("Unable to time", th);
            }
        }
    }
}
