package com.cloud.legacymodel.communication.command;

public class AggregationControlCommand extends NetworkElementCommand {
    private Action action;

    protected AggregationControlCommand() {
        super();
    }

    public AggregationControlCommand(final Action action, final String name, final String ip, final String guestIp) {
        super();
        this.action = action;
        this.setAccessDetail(NetworkElementCommand.ROUTER_NAME, name);
        this.setAccessDetail(NetworkElementCommand.ROUTER_IP, ip);
    }

    public Action getAction() {
        return action;
    }

    public enum Action {
        Start,
        Finish,
        Cleanup,
    }
}
