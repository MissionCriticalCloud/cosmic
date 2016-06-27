package org.apache.cloudstack.api.command.user.loadbalancer;

import com.cloud.event.EventTypes;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.network.rules.FirewallRule;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseAsyncCustomIdCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.ApplicationLoadBalancerResponse;
import org.apache.cloudstack.api.response.FirewallRuleResponse;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.network.lb.ApplicationLoadBalancerRule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateLoadBalancer", description = "Updates a load balancer", responseObject = ApplicationLoadBalancerResponse.class, since = "4.4.0",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class UpdateApplicationLoadBalancerCmd extends BaseAsyncCustomIdCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpdateApplicationLoadBalancerCmd.class.getName());

    private static final String s_name = "updateloadbalancerresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = FirewallRuleResponse.class, required = true, description = "the ID of the load balancer")
    private Long id;

    @Parameter(name = ApiConstants.FOR_DISPLAY, type = CommandType.BOOLEAN, description = "an optional field, whether to the display the rule to the end user or not", since = "4" +
            ".4", authorized = {RoleType.Admin})
    private Boolean display;

    @Override
    public String getEventType() {
        return EventTypes.EVENT_LOAD_BALANCER_UPDATE;
    }

    @Override
    public String getEventDescription() {
        return "updating load balancer: " + getId();
    }

    public Long getId() {
        return id;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////
    @Override
    public void execute() {
        CallContext.current().setEventDetails("Load balancer ID: " + getId());
        final ApplicationLoadBalancerRule rule = _appLbService.updateApplicationLoadBalancer(getId(), this.getCustomId(), getDisplay());
        final ApplicationLoadBalancerResponse lbResponse = _responseGenerator.createLoadBalancerContainerReponse(rule, _lbService.getLbInstances(getId()));
        setResponseObject(lbResponse);
        lbResponse.setResponseName(getCommandName());
    }

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////
    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final ApplicationLoadBalancerRule lb = _entityMgr.findById(ApplicationLoadBalancerRule.class, getId());
        if (lb != null) {
            return lb.getAccountId();
        } else {
            throw new InvalidParameterValueException("Can't find load balancer by ID specified");
        }
    }

    public Boolean getDisplay() {
        return display;
    }

    @Override
    public void checkUuid() {
        if (this.getCustomId() != null) {
            _uuidMgr.checkUuid(this.getCustomId(), FirewallRule.class);
        }
    }
}
