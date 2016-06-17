package com.cloud.agent.api;

public class FindLogicalSwitchAnswer extends Answer {
  private String logicalSwitchPortUuid;

  public FindLogicalSwitchAnswer(final Command command, final boolean success, final String details, final String localSwitchPortUuid) {
      super(command, success, details);
      logicalSwitchPortUuid = localSwitchPortUuid;
  }

  public String getLogicalSwitchPortUuid() {
      return logicalSwitchPortUuid;
  }

  public FindLogicalSwitchAnswer(final Command command, final Exception e) {
      super(command, e);
  }
}
