package com.cloud.org;

public interface Managed {
    public enum ManagedState {
        Managed, PrepareUnmanaged, Unmanaged, PrepareUnmanagedError
    }
}
