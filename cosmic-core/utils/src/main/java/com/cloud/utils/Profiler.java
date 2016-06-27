//

//

package com.cloud.utils;

public class Profiler {

    private static final long MILLIS_FACTOR = 1000l;
    private static final double EXPONENT = 2d;

    private Long startTickNanoSeconds;
    private Long stopTickNanoSeconds;

    public long start() {
        startTickNanoSeconds = System.nanoTime();
        return startTickNanoSeconds;
    }

    public long stop() {
        stopTickNanoSeconds = System.nanoTime();
        return stopTickNanoSeconds;
    }

    /**
     * 1 millisecond = 1e+6 nanoseconds
     * 1 second = 1000 milliseconds = 1e+9 nanoseconds
     *
     * @return the duration in nanoseconds.
     */
    public long getDuration() {
        if (startTickNanoSeconds != null && stopTickNanoSeconds != null) {
            return stopTickNanoSeconds - startTickNanoSeconds;
        }

        return -1;
    }

    public boolean isStarted() {
        return startTickNanoSeconds != null;
    }

    public boolean isStopped() {
        return stopTickNanoSeconds != null;
    }

    @Override
    public String toString() {
        if (startTickNanoSeconds == null) {
            return "Not Started";
        }

        if (stopTickNanoSeconds == null) {
            return "Started but not stopped";
        }

        return "Done. Duration: " + getDurationInMillis() + "ms";
    }

    /**
     * 1 millisecond = 1e+6 nanoseconds
     * 1 second = 1000 millisecond = 1e+9 nanoseconds
     *
     * @return the duration in milliseconds.
     */
    public long getDurationInMillis() {
        if (startTickNanoSeconds != null && stopTickNanoSeconds != null) {
            return (stopTickNanoSeconds - startTickNanoSeconds) / (long) Math.pow(MILLIS_FACTOR, EXPONENT);
        }

        return -1;
    }
}
