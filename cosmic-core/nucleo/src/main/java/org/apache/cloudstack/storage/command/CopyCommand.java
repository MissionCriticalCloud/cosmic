//

//

package org.apache.cloudstack.storage.command;

import com.cloud.agent.api.to.DataTO;

import java.util.HashMap;
import java.util.Map;

public final class CopyCommand extends StorageSubSystemCommand {
    private DataTO srcTO;
    private DataTO destTO;
    private DataTO cacheTO;
    private boolean executeInSequence = false;
    private Map<String, String> options = new HashMap<>();
    private Map<String, String> options2 = new HashMap<>();

    public CopyCommand(final DataTO srcData, final DataTO destData, final int timeout, final boolean executeInSequence) {
        super();
        srcTO = srcData;
        destTO = destData;
        setWait(timeout);
        this.executeInSequence = executeInSequence;
    }

    public DataTO getDestTO() {
        return destTO;
    }

    public void setDestTO(final DataTO destTO) {
        this.destTO = destTO;
    }

    public DataTO getSrcTO() {
        return srcTO;
    }

    public void setSrcTO(final DataTO srcTO) {
        this.srcTO = srcTO;
    }

    @Override
    public boolean executeInSequence() {
        return executeInSequence;
    }

    public DataTO getCacheTO() {
        return cacheTO;
    }

    public void setCacheTO(final DataTO cacheTO) {
        this.cacheTO = cacheTO;
    }

    public int getWaitInMillSeconds() {
        return getWait() * 1000;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public void setOptions(final Map<String, String> options) {
        this.options = options;
    }

    public Map<String, String> getOptions2() {
        return options2;
    }

    public void setOptions2(final Map<String, String> options2) {
        this.options2 = options2;
    }

    @Override
    public void setExecuteInSequence(final boolean inSeq) {
        executeInSequence = inSeq;
    }
}
