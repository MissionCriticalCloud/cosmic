package org.apache.cloudstack.engine.datacenter.entity.api;

import com.cloud.utils.fsm.StateMachine2;
import com.cloud.utils.fsm.StateObject;
import org.apache.cloudstack.engine.entity.api.CloudStackEntity;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;

/**
 * This interface specifies the states and operations all physical
 * and virtual resources in the data center must implement.
 */
@Produces({"application/json", "application/xml"})
public interface DataCenterResourceEntity extends CloudStackEntity, StateObject<DataCenterResourceEntity.State> {

    /**
     * Prepare the resource to take new on new demands.
     */
    @POST
    boolean enable();

    /**
     * Disables the resource.  Cleanup.  Prepare for the resource to be removed.
     */
    @POST
    boolean disable();

    /**
     * Do not use the resource for new demands.
     */
    @POST
    boolean deactivate();

    /**
     * Reactivates a deactivated resource.
     */
    @POST
    boolean reactivate();

    @Override
    @GET
    State getState();

    public void persist();

    String getName();

    /**
     * This is the state machine for how CloudStack should interact with
     */
    public enum State {
        Disabled("The resource is disabled so CloudStack should not use it.  This is the initial state of all resources added to CloudStack."), Enabled(
                "The resource is now enabled for CloudStack to use."), Deactivated("The resource is deactivated so CloudStack should not use it for new resource needs.");

        protected static final StateMachine2<State, Event, DataCenterResourceEntity> s_fsm = new StateMachine2<>();

        static {
            s_fsm.addTransition(Disabled, Event.EnableRequest, Enabled);
            s_fsm.addTransition(Enabled, Event.DisableRequest, Disabled);
            s_fsm.addTransition(Enabled, Event.DeactivateRequest, Deactivated);
            s_fsm.addTransition(Deactivated, Event.ActivatedRequest, Enabled);
        }

        String _description;

        private State(final String description) {
            _description = description;
        }

        public enum Event {
            EnableRequest, DisableRequest, DeactivateRequest, ActivatedRequest
        }

    }
}
