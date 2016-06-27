package org.apache.cloudstack.storage.resource;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;

public interface SecondaryStorageResourceHandler {

    Answer executeRequest(Command cmd);
}
