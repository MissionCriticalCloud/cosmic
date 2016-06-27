//

//

package com.cloud.agent.api;

import com.cloud.utils.Ternary;

import java.util.List;

public class PrepareOCFS2NodesCommand extends Command {
    List<Ternary<Integer, String, String>> nodes;
    String clusterName;

    public PrepareOCFS2NodesCommand(final String clusterName, final List<Ternary<Integer, String, String>> nodes) {
        this.nodes = nodes;
        this.clusterName = clusterName;
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }

    public List<Ternary<Integer, String, String>> getNodes() {
        return nodes;
    }

    public String getClusterName() {
        return clusterName;
    }
}
