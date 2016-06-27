//

//

package org.apache.cloudstack.utils.graphite;

public class GraphiteException extends RuntimeException {

    public GraphiteException(final String message) {
        super(message);
    }

    public GraphiteException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
