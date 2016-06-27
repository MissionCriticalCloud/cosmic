//

//

package com.cloud.utils;

public class ExecutionResult {
    private Boolean success;
    private String details;

    public ExecutionResult(final Boolean success, final String details) {
        this.success = success;
        this.details = details;
    }

    public Boolean isSuccess() {
        return success;
    }

    public void setSuccess(final Boolean success) {
        this.success = success;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(final String details) {
        this.details = details;
    }
}
