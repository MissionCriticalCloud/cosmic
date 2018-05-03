package com.cloud.storage.template;

import com.cloud.agent.api.storage.UploadAnswer;
import com.cloud.agent.api.storage.UploadCommand;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.CreateEntityDownloadURLAnswer;
import com.cloud.legacymodel.communication.command.CreateEntityDownloadURLCommand;
import com.cloud.legacymodel.communication.command.DeleteEntityDownloadURLCommand;
import com.cloud.legacymodel.storage.Upload.Status;
import com.cloud.model.enumeration.ImageFormat;
import com.cloud.storage.resource.SecondaryStorageResource;
import com.cloud.utils.component.Manager;

public interface UploadManager extends Manager {

    /**
     * @param jobId job Id
     * @return status of the upload job
     */
    public TemplateUploader.Status getUploadStatus(String jobId);

    /**
     * @param jobId job Id
     * @return status of the upload job
     */
    public Status getUploadStatus2(String jobId);

    /**
     * Get the upload percent of a upload job
     *
     * @param jobId job Id
     * @return
     */
    public int getUploadPct(String jobId);

    /**
     * Get the upload error if any
     *
     * @param jobId job Id
     * @return
     */
    public String getUploadError(String jobId);

    /**
     * Get the local path for the upload
     * @param jobId job Id
     * @return public String getUploadLocalPath(String jobId);
     */

    /**
     * Handle upload commands from the management server
     *
     * @param cmd cmd from server
     * @return answer representing status of upload.
     */
    public UploadAnswer handleUploadCommand(SecondaryStorageResource resource, UploadCommand cmd);

    public String getPublicTemplateRepo();

    String uploadPublicTemplate(long id, String url, String name, ImageFormat format, Long accountId, String descr, String cksum, String installPathPrefix, String user,
                                String password, long maxTemplateSizeInBytes);

    CreateEntityDownloadURLAnswer handleCreateEntityURLCommand(CreateEntityDownloadURLCommand cmd);

    Answer handleDeleteEntityDownloadURLCommand(DeleteEntityDownloadURLCommand cmd);
}
