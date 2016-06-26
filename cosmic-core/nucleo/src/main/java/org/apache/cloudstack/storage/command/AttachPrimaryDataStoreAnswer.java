//

//

package org.apache.cloudstack.storage.command;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;

public class AttachPrimaryDataStoreAnswer extends Answer {
    private String uuid;
    private long capacity;
    private long avail;

    public AttachPrimaryDataStoreAnswer(final Command cmd) {
        super(cmd);
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public long getCapacity() {
        return capacity;
    }

    public void setCapacity(final long capacity) {
        this.capacity = capacity;
    }

    public long getAvailable() {
        return avail;
    }

    public void setAvailable(final long avail) {
        this.avail = avail;
    }
}
