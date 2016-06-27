package org.apache.cloudstack.api;

public interface IBaseListDomainResourcesCmd extends IBaseListCmd {
    boolean listAll();

    boolean isRecursive();

    Long getDomainId();
}
