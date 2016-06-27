//

//

package com.cloud.utils.rest;

import org.apache.http.HttpStatus;

public class HttpStatusCodeHelper {

    public static boolean isSuccess(final int statusCode) {
        return statusCode >= HttpStatus.SC_OK && statusCode <= HttpStatus.SC_MULTI_STATUS;
    }

    public static boolean isUnauthorized(final int statusCode) {
        return statusCode == HttpStatus.SC_UNAUTHORIZED;
    }
}
