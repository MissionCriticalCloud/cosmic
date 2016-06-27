//

//

package com.cloud.agent.api.routing;

import com.cloud.agent.api.Answer;

public class GlobalLoadBalancerConfigAnswer extends Answer {

    public GlobalLoadBalancerConfigAnswer(final boolean success, final String details) {
        this.result = success;
        this.details = details;
    }
}
