package com.cloud.vm;

import org.apache.commons.lang.builder.ToStringBuilder;

public class AfterScanAction {
    public enum Action {
        NOP, EXPAND, SHRINK
    }

    private final Action action;
    private final int value;

    private AfterScanAction(final Action action, final int value) {
        this.action = action;
        this.value = value;
    }

    public static AfterScanAction expand() {
        return new AfterScanAction(Action.EXPAND, 1);
    }

    public static AfterScanAction expand(final int ammoun) {
        return new AfterScanAction(Action.EXPAND, ammoun);
    }

    public static AfterScanAction shrink() {
        return new AfterScanAction(Action.SHRINK, 1);
    }

    public static AfterScanAction nop() {
        return new AfterScanAction(Action.NOP, 0);
    }

    public Action getAction() {
        return action;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
