//

//

package com.cloud.utils.concurrency;

import java.util.concurrent.ThreadFactory;

public class NamedThreadFactory implements ThreadFactory {
    private final String _name;
    private int _number;

    public NamedThreadFactory(final String name) {
        _name = name;
        _number = 1;
    }

    @Override
    public synchronized Thread newThread(final Runnable r) {
        return new Thread(r, _name + "-" + _number++);
    }
}
