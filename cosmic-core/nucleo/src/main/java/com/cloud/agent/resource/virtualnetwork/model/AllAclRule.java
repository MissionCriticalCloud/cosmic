//

//

package com.cloud.agent.resource.virtualnetwork.model;

public class AllAclRule extends AclRule {
    private final String type = "all";

    public AllAclRule() {
        // Empty constructor for (de)serialization
    }

    public AllAclRule(final String cidr, final boolean allowed) {
        super(cidr, allowed);
    }
}
