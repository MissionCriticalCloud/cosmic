//

//

package com.cloud.agent.api;

import com.cloud.agent.api.storage.SsCommand;
import com.cloud.agent.api.to.DataStoreTO;

public class ComputeChecksumCommand extends SsCommand {
    private DataStoreTO store;
    private String templatePath;

    public ComputeChecksumCommand() {
        super();
    }

    public ComputeChecksumCommand(final DataStoreTO store, final String templatePath) {
        this.templatePath = templatePath;
        this.setStore(store);
    }

    public String getTemplatePath() {
        return templatePath;
    }

    public DataStoreTO getStore() {
        return store;
    }

    public void setStore(final DataStoreTO store) {
        this.store = store;
    }
}
