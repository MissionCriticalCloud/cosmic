package org.apache.cloudstack.engine.subsystem.api.hypervisor;

public interface ComputeSubsystem {

    void start(String vm, String reservationId);

    void cancel(String reservationId);

    void stop(String vm, String reservationId);

    void migrate(String vm, String reservationId);
}
