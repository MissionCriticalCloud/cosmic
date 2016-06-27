package org.apache.cloudstack.api;

public interface ResponseObject {
    /**
     * Get the name of the API response
     *
     * @return the name of the API response
     */
    String getResponseName();

    /**
     * Set the name of the API response
     *
     * @param name
     */
    void setResponseName(String name);

    /**
     * Get the name of the API object
     *
     * @return the name of the API object
     */
    String getObjectName();

    /**
     * @param name
     */
    void setObjectName(String name);

    /**
     * Returns the object UUid
     */
    String getObjectId();

    /**
     * Returns the job id
     *
     * @return
     */
    String getJobId();

    /**
     * Sets the job id
     *
     * @param jobId
     */
    void setJobId(String jobId);

    /**
     * Returns the job status
     *
     * @return
     */
    Integer getJobStatus();

    /**
     * @param jobStatus
     */
    void setJobStatus(Integer jobStatus);

    public enum ResponseView {
        Full,
        Restricted
    }
}
