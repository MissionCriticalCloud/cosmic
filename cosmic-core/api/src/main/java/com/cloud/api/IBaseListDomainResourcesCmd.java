package com.cloud.api;

public interface IBaseListDomainResourcesCmd extends IBaseListCmd {
    boolean listAll();

    boolean isRecursive();

    Long getDomainId();
}
