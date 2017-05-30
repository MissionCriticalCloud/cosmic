package com.cloud.api;

import java.io.Serializable;

public interface ResponseObject extends Serializable {
    /**
     * Gets the name of the API response
     *
     * @return The name of the API response
     */
    String getResponseName();

    /**
     * Sets the name of the API response
     *
     * @param name The name of the API response
     */
    void setResponseName(String name);

    /**
     * Gets the name of the API object
     *
     * @return The name of the API object
     */
    String getObjectName();

    /**
     * Sets the name of the object
     *
     * @param name The name of the object
     */
    void setObjectName(String name);

    /**
     * Returns the object UUID
     */
    String getObjectId();

    /**
     * Gets the ID of the job
     *
     * @return The ID of the job
     */
    String getJobId();

    /**
     * Sets the ID of the job
     *
     * @param jobId The ID of the job
     */
    void setJobId(String jobId);

    /**
     * Gets the job status
     *
     * @return The job status
     */
    Integer getJobStatus();

    /**
     * Sets the job status
     *
     * @param jobStatus The job status
     */
    void setJobStatus(Integer jobStatus);

    enum ResponseView {
        Full,
        Restricted
    }
}
