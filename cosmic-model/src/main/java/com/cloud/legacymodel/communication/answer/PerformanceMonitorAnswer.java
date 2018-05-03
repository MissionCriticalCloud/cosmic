package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.communication.command.PerformanceMonitorCommand;

public class PerformanceMonitorAnswer extends Answer {
    public PerformanceMonitorAnswer() {
    }

    public PerformanceMonitorAnswer(final PerformanceMonitorCommand cmd,
                                    final boolean result, final String details) {
        super(cmd, result, details);
    }
}
