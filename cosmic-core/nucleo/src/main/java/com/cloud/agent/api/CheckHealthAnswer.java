//

//

package com.cloud.agent.api;

public class CheckHealthAnswer extends Answer {

    public CheckHealthAnswer() {
    }

    public CheckHealthAnswer(final CheckHealthCommand cmd, final boolean alive) {
        super(cmd, alive, "resource is " + (alive ? "alive" : "not alive"));
    }
}
