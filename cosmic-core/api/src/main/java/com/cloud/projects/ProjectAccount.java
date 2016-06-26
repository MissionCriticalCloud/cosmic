package com.cloud.projects;

public interface ProjectAccount {
    long getAccountId();

    long getProjectId();

    Role getAccountRole();

    long getProjectAccountId();

    public enum Role {
        Admin, Regular
    }
}
