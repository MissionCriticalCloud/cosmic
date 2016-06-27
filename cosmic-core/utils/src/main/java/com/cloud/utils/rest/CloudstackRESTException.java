//

//

package com.cloud.utils.rest;

public class CloudstackRESTException extends Exception {

    public CloudstackRESTException(final String message) {
        super(message);
    }

    public CloudstackRESTException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
