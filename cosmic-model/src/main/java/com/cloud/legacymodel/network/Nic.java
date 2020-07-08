package com.cloud.legacymodel.network;

import com.cloud.legacymodel.Identity;
import com.cloud.legacymodel.InternalIdentity;
import com.cloud.legacymodel.statemachine.FiniteState;
import com.cloud.legacymodel.statemachine.StateMachine;
import com.cloud.model.enumeration.DHCPMode;
import com.cloud.model.enumeration.IpAddressFormat;
import com.cloud.model.enumeration.VirtualMachineType;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.sun.org.apache.xerces.internal.xs.StringList;

/**
 * Nic represents one nic on the VM.
 */
public interface Nic extends Identity, InternalIdentity {
    /**
     * @return reservation id returned by the allocation source. This can be the String version of the database id if
     * the
     * allocation source does not need it's own implementation of the reservation id. This is passed back to the
     * allocation source to release the resource.
     */
    String getReservationId();

    /**
     * @return unique name for the allocation source.
     */
    String getReserver();

    /**
     * @return the time a reservation request was made to the allocation source.
     */
    Date getUpdateTime();

    /**
     * @return the reservation state of the resource.
     */
    State getState();

    ReservationStrategy getReservationStrategy();

    boolean isDefaultNic();

    String getMacAddress();

    /**
     * @return network profile id that this
     */
    long getNetworkId();

    /**
     * @return the vm instance id that this nic belongs to.
     */
    long getInstanceId();

    DHCPMode getMode();

    URI getIsolationUri();

    URI getBroadcastUri();

    List<String> getMirrorIpAddressList();

    VirtualMachineType getVmType();

    IpAddressFormat getAddressFormat();

    boolean getSecondaryIp();

    String getIPv4Address();

    String getIPv4Netmask();

    String getIPv4Gateway();

    String getIPv6Gateway();

    String getIPv6Cidr();

    String getIPv6Address();

    enum Event {
        ReservationRequested, ReleaseRequested, CancelRequested, OperationCompleted, OperationFailed,
    }

    enum State implements FiniteState<State, Event> {
        Allocated("Resource is allocated but not reserved"), Reserving("Resource is being reserved right now"), Reserved("Resource has been reserved."), Releasing(
                "Resource is being released"), Deallocating("Resource is being deallocated");

        final static private StateMachine<State, Event> s_fsm = new StateMachine<>();

        static {
            s_fsm.addTransition(State.Allocated, Event.ReservationRequested, State.Reserving);
            s_fsm.addTransition(State.Reserving, Event.CancelRequested, State.Allocated);
            s_fsm.addTransition(State.Reserving, Event.OperationCompleted, State.Reserved);
            s_fsm.addTransition(State.Reserving, Event.OperationFailed, State.Allocated);
            s_fsm.addTransition(State.Reserved, Event.ReleaseRequested, State.Releasing);
            s_fsm.addTransition(State.Releasing, Event.OperationCompleted, State.Allocated);
            s_fsm.addTransition(State.Releasing, Event.OperationFailed, State.Reserved);
        }

        String _description;

        State(final String description) {
            _description = description;
        }

        @Override
        public StateMachine<State, Event> getStateMachine() {
            return s_fsm;
        }

        @Override
        public State getNextState(final Event event) {
            return s_fsm.getNextState(this, event);
        }

        @Override
        public List<State> getFromStates(final Event event) {
            return s_fsm.getFromStates(this, event);
        }

        @Override
        public Set<Event> getPossibleEvents() {
            return s_fsm.getPossibleEvents(this);
        }

        @Override
        public String getDescription() {
            return _description;
        }
    }

    enum ReservationStrategy {
        PlaceHolder, Create, Start, Managed
    }
}
