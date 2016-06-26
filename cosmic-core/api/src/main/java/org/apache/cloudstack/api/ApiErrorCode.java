package org.apache.cloudstack.api;

/**
 * Enum class for various API error code used in CloudStack
 */
public enum ApiErrorCode {

    UNAUTHORIZED(401),
    METHOD_NOT_ALLOWED(405),
    MALFORMED_PARAMETER_ERROR(430),
    PARAM_ERROR(431),
    UNSUPPORTED_ACTION_ERROR(432),
    API_LIMIT_EXCEED(429),

    INTERNAL_ERROR(530),
    ACCOUNT_ERROR(531),
    ACCOUNT_RESOURCE_LIMIT_ERROR(532),
    INSUFFICIENT_CAPACITY_ERROR(533),
    RESOURCE_UNAVAILABLE_ERROR(534),
    RESOURCE_ALLOCATION_ERROR(535),
    RESOURCE_IN_USE_ERROR(536),
    NETWORK_RULE_CONFLICT_ERROR(537);

    private int httpCode;

    private ApiErrorCode(final int httpStatusCode) {
        httpCode = httpStatusCode;
    }

    public int getHttpCode() {
        return httpCode;
    }

    public void setHttpCode(final int httpCode) {
        this.httpCode = httpCode;
    }

    @Override
    public String toString() {
        return String.valueOf(this.httpCode);
    }

}
