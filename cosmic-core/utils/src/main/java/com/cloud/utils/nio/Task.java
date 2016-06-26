//

//

package com.cloud.utils.nio;

import com.cloud.utils.exception.TaskExecutionException;

import java.util.concurrent.Callable;

/**
 * Task represents one todo item for the AgentManager or the AgentManager
 */
public abstract class Task implements Callable<Boolean> {

    Object _data;

    Type _type;
    Link _link;

    public Task(final Type type, final Link link, final byte[] data) {
        _data = data;
        _type = type;
        _link = link;
    }

    public Task(final Type type, final Link link, final Object data) {
        _data = data;
        _type = type;
        _link = link;
    }

    protected Task() {
    }

    public Type getType() {
        return _type;
    }

    public Link getLink() {
        return _link;
    }

    public byte[] getData() {
        return (byte[]) _data;
    }

    public Object get() {
        return _data;
    }

    @Override
    public String toString() {
        return _type.toString();
    }

    @Override
    public Boolean call() throws TaskExecutionException {
        doTask(this);
        return true;
    }

    abstract protected void doTask(Task task) throws TaskExecutionException;

    public enum Type {
        CONNECT,     // Process a new connection.
        DISCONNECT,  // Process an existing connection disconnecting.
        DATA,        // data incoming.
        CONNECT_FAILED, // Connection failed.
        OTHER        // Allows other tasks to be defined by the caller.
    }
}
