//

//

package com.cloud.utils.backoff;

import com.cloud.utils.component.Adapter;

/**
 * BackoffAlgorithm implements multiple BackoffAlgorithm.
 */
public interface BackoffAlgorithm extends Adapter {

    /**
     */
    void waitBeforeRetry();

    /**
     * no longer need to backoff.  reset to beginning.
     */
    void reset();
}
