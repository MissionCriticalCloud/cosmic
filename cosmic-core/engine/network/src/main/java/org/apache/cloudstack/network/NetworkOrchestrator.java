package org.apache.cloudstack.network;

public interface NetworkOrchestrator {

    /**
     * Prepares for a VM to join a network
     *
     * @param vm            vm
     * @param reservationId reservation id
     */
    void prepare(String vm, String reservationId);

    /**
     * Release all reservation
     */
    void release(String vm, String reservationId);

    /**
     * Cancel a previous reservation
     *
     * @param reservationId
     */
    void cancel(String reservationId);
}
