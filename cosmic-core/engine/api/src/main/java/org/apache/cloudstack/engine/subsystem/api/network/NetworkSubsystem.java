package org.apache.cloudstack.engine.subsystem.api.network;

public interface NetworkSubsystem {
    String create();

    String start(String network, String reservationId);

    void shutdown(String nework, String reservationId);

    void prepare(String vm, String network, String reservationId);

    void release(String vm, String network, String reservationId);

    void cancel(String reservationId);

    void destroy(String network, String reservationId);
}
