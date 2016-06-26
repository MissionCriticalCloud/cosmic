//

//

package com.cloud.agent.api.storage;

import com.cloud.agent.api.to.DataStoreTO;

public class ListTemplateCommand extends StorageCommand {
    private DataStoreTO store;

    //private String secUrl;

    public ListTemplateCommand() {
    }

    public ListTemplateCommand(final DataStoreTO store) {
        this.store = store;
        //        this.secUrl = url;
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }

    public DataStoreTO getDataStore() {
        return store;
    }

    //   public String getSecUrl() {
    //       return secUrl;
    //   }
}
