package org.apache.cloudstack.api;

import com.cloud.exception.InvalidParameterValueException;
import com.cloud.utils.exception.CSExceptionErrorCode;

import java.util.Map;

public abstract class BaseListCmd extends BaseCmd implements IBaseListCmd {

    public static final Long s_pageSizeUnlimited = -1L;
    private static Long s_maxPageSize = null;

    // ///////////////////////////////////////////////////
    // ///////// BaseList API parameters /////////////////
    // ///////////////////////////////////////////////////
    @Parameter(name = ApiConstants.KEYWORD, type = CommandType.STRING, description = "List by keyword")
    private String keyword;

    // FIXME: Need to be able to specify next/prev/first/last, so Integer might not be right
    @Parameter(name = ApiConstants.PAGE, type = CommandType.INTEGER)
    private Integer page;

    @Parameter(name = ApiConstants.PAGE_SIZE, type = CommandType.INTEGER)
    private Integer pageSize;

    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////

    public BaseListCmd() {
    }

    @Override
    public String getKeyword() {
        return keyword;
    }

    @Override
    public Integer getPage() {
        return page;
    }

    @Override
    public Integer getPageSize() {
        if (pageSize != null && s_maxPageSize.longValue() != s_pageSizeUnlimited && pageSize.longValue() > s_maxPageSize.longValue()) {
            throw new InvalidParameterValueException("Page size can't exceed max allowed page size value: " + s_maxPageSize.longValue());
        }

        if (pageSize != null && pageSize.longValue() == s_pageSizeUnlimited && page != null) {
            throw new InvalidParameterValueException("Can't specify page parameter when pagesize is -1 (Unlimited)");
        }

        return pageSize;
    }

    @Override
    public Long getPageSizeVal() {
        Long defaultPageSize = s_maxPageSize;
        final Integer pageSizeInt = getPageSize();
        if (pageSizeInt != null) {
            defaultPageSize = pageSizeInt.longValue();
        }
        if (defaultPageSize.longValue() == s_pageSizeUnlimited) {
            defaultPageSize = null;
        }

        return defaultPageSize;
    }

    @Override
    public Long getStartIndex() {
        Long startIndex = Long.valueOf(0);
        final Long pageSizeVal = getPageSizeVal();

        if (pageSizeVal == null) {
            startIndex = null;
        } else if (page != null) {
            final int pageNum = page.intValue();
            if (pageNum > 0) {
                startIndex = Long.valueOf(pageSizeVal * (pageNum - 1));
            }
        }
        return startIndex;
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.None;
    }

    @Override
    public void configure() {
        if (s_maxPageSize == null) {
            if (_configService.getDefaultPageSize().longValue() != s_pageSizeUnlimited) {
                s_maxPageSize = _configService.getDefaultPageSize();
            } else {
                s_maxPageSize = s_pageSizeUnlimited;
            }
        }
    }

    @Override
    public long getEntityOwnerId() {
        // no owner is needed for list command
        return 0;
    }

    @Override
    public void validateSpecificParameters(final Map<String, String> params) {
        super.validateSpecificParameters(params);

        final Object pageSizeObj = params.get(ApiConstants.PAGE_SIZE);
        Long pageSize = null;
        if (pageSizeObj != null) {
            pageSize = Long.valueOf((String) pageSizeObj);
        }

        if (params.get(ApiConstants.PAGE) == null &&
                pageSize != null &&
                !pageSize.equals(BaseListCmd.s_pageSizeUnlimited)) {
            final ServerApiException ex = new ServerApiException(ApiErrorCode.PARAM_ERROR, "\"page\" parameter is required when \"pagesize\" is specified");
            ex.setCSErrorCode(CSExceptionErrorCode.getCSErrCode(ex.getClass().getName()));
            throw ex;
        } else if (pageSize == null && (params.get(ApiConstants.PAGE) != null)) {
            throw new ServerApiException(ApiErrorCode.PARAM_ERROR, "\"pagesize\" parameter is required when \"page\" is specified");
        }
    }
}
