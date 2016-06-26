//

//

package com.cloud.agent.api;

import java.util.HashMap;

public class ClusterVMMetaDataSyncAnswer extends Answer {
    private final long _clusterId;
    private final HashMap<String, String> _vmMetaDatum;
    private boolean _isExecuted = false;

    public ClusterVMMetaDataSyncAnswer(final long clusterId, final HashMap<String, String> vmMetaDatum) {
        _clusterId = clusterId;
        _vmMetaDatum = vmMetaDatum;
        result = true;
    }

    // this is here because a cron command answer is being sent twice
    //  AgentAttache.processAnswers
    //  AgentManagerImpl.notifyAnswersToMonitors
    public boolean isExecuted() {
        return _isExecuted;
    }

    public void setExecuted() {
        _isExecuted = true;
    }

    public long getClusterId() {
        return _clusterId;
    }

    public HashMap<String, String> getVMMetaDatum() {
        return _vmMetaDatum;
    }
}
