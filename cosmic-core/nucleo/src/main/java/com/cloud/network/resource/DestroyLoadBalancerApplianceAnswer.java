//

//

package com.cloud.network.resource;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;

public class DestroyLoadBalancerApplianceAnswer extends Answer {
    public DestroyLoadBalancerApplianceAnswer(final Command cmd, final boolean success, final String details) {
        this.result = success;
        this.details = details;
    }
}
