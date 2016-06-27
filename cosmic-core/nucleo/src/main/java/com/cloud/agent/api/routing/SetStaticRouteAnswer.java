//

//

package com.cloud.agent.api.routing;

import com.cloud.agent.api.Answer;

import java.util.Arrays;

public class SetStaticRouteAnswer extends Answer {
    String[] results;

    protected SetStaticRouteAnswer() {
    }

    public SetStaticRouteAnswer(final SetStaticRouteCommand cmd, final boolean success, final String[] results) {
        super(cmd, success, null);
        if (results != null) {
            assert (cmd.getStaticRoutes().length == results.length) : "Static routes and their results should be the same length";
            this.results = Arrays.copyOf(results, results.length);
        }
    }

    public String[] getResults() {
        if (results != null) {
            return Arrays.copyOf(results, results.length);
        }
        return null;
    }
}
