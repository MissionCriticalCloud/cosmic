//

//

package com.cloud.network.nicira;

public class NiciraNvpApiException extends Exception {

    public NiciraNvpApiException() {
    }

    public NiciraNvpApiException(final String message) {
        super(message);
    }

    public NiciraNvpApiException(final Throwable cause) {
        super(cause);
    }

    public NiciraNvpApiException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
