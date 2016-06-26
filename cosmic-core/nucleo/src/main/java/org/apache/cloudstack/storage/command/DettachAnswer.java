//

//

package org.apache.cloudstack.storage.command;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.to.DiskTO;

public final class DettachAnswer extends Answer {
    private DiskTO disk;

    public DettachAnswer() {
        super(null);
    }

    public DettachAnswer(final DiskTO disk) {
        super(null);
        setDisk(disk);
    }

    public DettachAnswer(final String errMsg) {
        super(null, false, errMsg);
    }

    public DiskTO getDisk() {
        return disk;
    }

    public void setDisk(final DiskTO disk) {
        this.disk = disk;
    }
}
