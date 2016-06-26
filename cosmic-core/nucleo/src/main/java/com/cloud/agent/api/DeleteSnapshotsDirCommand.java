//

//

package com.cloud.agent.api;

import com.cloud.agent.api.to.DataStoreTO;

/**
 * This command encapsulates a primitive operation which enables coalescing the backed up VHD snapshots on the secondary server
 * This currently assumes that the secondary storage are mounted on the XenServer.
 */
public class DeleteSnapshotsDirCommand extends Command {
    DataStoreTO store;
    String directory;

    protected DeleteSnapshotsDirCommand() {

    }

    public DeleteSnapshotsDirCommand(final DataStoreTO store, final String dir) {
        this.store = store;
        this.directory = dir;
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }

    public DataStoreTO getDataStore() {
        return store;
    }

    public String getDirectory() {
        return directory;
    }
}
