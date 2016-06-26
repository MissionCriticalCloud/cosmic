//

//

package org.apache.cloudstack.storage.command;

import java.util.Map;

public final class SnapshotAndCopyCommand extends StorageSubSystemCommand {
    private final String _uuidOfSourceVdi;
    private final Map<String, String> _sourceDetails;
    private final Map<String, String> _destDetails;

    private boolean _executeInSequence = true;

    public SnapshotAndCopyCommand(final String uuidOfSourceVdi, final Map<String, String> sourceDetails, final Map<String, String> destDetails) {
        _uuidOfSourceVdi = uuidOfSourceVdi;
        _sourceDetails = sourceDetails;
        _destDetails = destDetails;
    }

    public String getUuidOfSourceVdi() {
        return _uuidOfSourceVdi;
    }

    public Map<String, String> getSourceDetails() {
        return _sourceDetails;
    }

    public Map<String, String> getDestDetails() {
        return _destDetails;
    }

    @Override
    public void setExecuteInSequence(final boolean executeInSequence) {
        _executeInSequence = executeInSequence;
    }

    @Override
    public boolean executeInSequence() {
        return _executeInSequence;
    }
}
