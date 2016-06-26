package org.apache.cloudstack.storage.command;

public class CommandResult {
    private boolean success;
    private String result;

    public CommandResult() {
        this.success = true;
        this.result = "";
    }

    public boolean isSuccess() {
        return this.success;
    }

    public void setSuccess(final boolean success) {
        this.success = success;
    }

    public boolean isFailed() {
        return !this.success;
    }

    public String getResult() {
        return this.result;
    }

    public void setResult(final String result) {
        this.result = result;
        if (result != null) {
            this.success = false;
        }
    }
}
