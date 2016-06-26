//

//

package org.apache.cloudstack.storage.command;

import com.cloud.agent.api.Answer;

public class SnapshotAndCopyAnswer extends Answer {
    private String _path;

    public SnapshotAndCopyAnswer() {
    }

    public SnapshotAndCopyAnswer(final String errMsg) {
        super(null, false, errMsg);
    }

    public String getPath() {
        return _path;
    }

    public void setPath(final String path) {
        _path = path;
    }
}
