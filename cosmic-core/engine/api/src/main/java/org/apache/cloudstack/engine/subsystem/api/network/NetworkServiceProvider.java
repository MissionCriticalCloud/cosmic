package org.apache.cloudstack.engine.subsystem.api.network;

public interface NetworkServiceProvider {
    /**
     * Plug your network elements into this network
     *
     * @param network
     * @param reservationId
     */
    void plugInto(String network, String reservationId);

    /**
     * Unplug your network elements from this network
     *
     * @param network
     * @param reservationId
     */
    void unplugFrom(String network, String reservationId);

    /**
     * Cancel a previous work
     *
     * @param reservationId
     */
    void cancel(String reservationId);

    void provideServiceTo(String vm, String network, String reservationId);

    void removeServiceFrom(String vm, String network, String reservationId);

    void cleanUp(String network, String reservationId);
}
