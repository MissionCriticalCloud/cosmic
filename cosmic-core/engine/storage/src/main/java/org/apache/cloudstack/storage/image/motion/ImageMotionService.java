package org.apache.cloudstack.storage.image.motion;

import org.apache.cloudstack.engine.subsystem.api.storage.TemplateInfo;
import org.apache.cloudstack.framework.async.AsyncCompletionCallback;
import org.apache.cloudstack.storage.command.CommandResult;

public interface ImageMotionService {
    void copyTemplateAsync(TemplateInfo destTemplate, TemplateInfo srcTemplate, AsyncCompletionCallback<CommandResult> callback);

    boolean copyIso(String isoUri, String destIsoUri);
}
