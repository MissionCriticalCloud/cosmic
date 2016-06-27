package com.cloud.vm;

/**
 * ConsoleProxy is a system VM instance that is used
 * to proxy VNC traffic
 */
public interface ConsoleProxy extends SystemVm {
    public int getActiveSession();

    public byte[] getSessionDetails();
}
