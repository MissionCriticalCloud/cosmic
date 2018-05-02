package com.cloud.agent.api;

import com.cloud.legacymodel.communication.answer.Answer;

public class PerformanceMonitorAnswer extends Answer {
    public PerformanceMonitorAnswer() {
    }

    public PerformanceMonitorAnswer(final PerformanceMonitorCommand cmd,
                                    final boolean result, final String details) {
        super(cmd, result, details);
    }
}
