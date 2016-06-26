//

//

package com.cloud.agent.api;

public class PerformanceMonitorAnswer extends Answer {
    public PerformanceMonitorAnswer() {
    }

    public PerformanceMonitorAnswer(final PerformanceMonitorCommand cmd,
                                    final boolean result, final String details) {
        super(cmd, result, details);
    }
}
