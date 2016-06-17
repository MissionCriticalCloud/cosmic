package com.cloud.exception;

@SuppressWarnings("serial")
public class NicPreparationException extends InsufficientCapacityException {

  public NicPreparationException(String message, Throwable cause) {
    super(message, cause);
  }

  public NicPreparationException(String message) {
    super(message, null);
  }

}
