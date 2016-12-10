package com.cloud.api;

public interface IBaseListCmd {
    String getKeyword();

    Integer getPage();

    Integer getPageSize();

    Long getPageSizeVal();

    Long getStartIndex();

    ApiCommandJobType getInstanceType();
}
