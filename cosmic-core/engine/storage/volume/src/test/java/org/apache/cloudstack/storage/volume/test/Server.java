package org.apache.cloudstack.storage.volume.test;

import org.apache.cloudstack.framework.async.AsyncCallbackDispatcher;

public class Server {
    Server1 svr;

    public Server() {
        svr = new Server1();
    }

    void foo() {
        // svr.foo1("foo", new
        // AsyncCallbackDispatcher(this).setOperationName("callback").setContextParam("name",
        // "foo"));
    }

    void foocallback(final AsyncCallbackDispatcher callback) {
        /*
         * System.out.println(callback.getContextParam("name")); String result =
         * callback.getResult(); System.out.println(result);
         */
    }
}
