package com.cloud.storage.download;

import com.cloud.agent.api.storage.DownloadAnswer;
import com.cloud.utils.component.Manager;
import org.apache.cloudstack.engine.subsystem.api.storage.DataObject;
import org.apache.cloudstack.framework.async.AsyncCompletionCallback;

/**
 * Monitor download progress of all templates across all servers
 */
public interface DownloadMonitor extends Manager {

    public void downloadTemplateToStorage(DataObject template, AsyncCompletionCallback<DownloadAnswer> callback);

    public void downloadVolumeToStorage(DataObject volume, AsyncCompletionCallback<DownloadAnswer> callback);
}
