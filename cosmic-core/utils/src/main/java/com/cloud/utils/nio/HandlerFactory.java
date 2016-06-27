//

//

package com.cloud.utils.nio;

/**
 * WorkerFactory creates and selects workers.
 */
public interface HandlerFactory {
    public Task create(Task.Type type, Link link, byte[] data);
}
