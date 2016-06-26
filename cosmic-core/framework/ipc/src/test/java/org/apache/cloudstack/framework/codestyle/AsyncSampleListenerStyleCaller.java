package org.apache.cloudstack.framework.codestyle;

public class AsyncSampleListenerStyleCaller {
    AsyncSampleCallee _ds;

    public void MethodThatWillCallAsyncMethod() {
        final String vol = new String();

        /*    _ds.createVolume(vol,
                new AsyncCompletionCallback<String>() {
                    @Override
                    public void complete(String resultObject) {
                        // TODO Auto-generated method stub

                    }
            });*/
    }
}
