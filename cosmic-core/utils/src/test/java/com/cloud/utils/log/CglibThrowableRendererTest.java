//

//

package com.cloud.utils.log;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;

public class CglibThrowableRendererTest {

    CglibThrowableRenderer cglibThrowableRenderer = new CglibThrowableRenderer();

    @Test
    public void testDoRendere() {
        final SampleClass sampleClass = (SampleClass) Enhancer.create(SampleClass.class, new MyInvocationHandler());
        try {
            sampleClass.theFirstMethodThatCapturesAnException();
        } catch (final Exception e) {
            final String[] exceptions = cglibThrowableRenderer.doRender(e);
            assertThatTheTraceListDoesNotContainsCgLibLogs(exceptions);
        }
    }

    private void assertThatTheTraceListDoesNotContainsCgLibLogs(final String[] exceptions) {
        for (final String s : exceptions) {
            Assert.assertEquals(false, isCgLibLogTrace(s));
        }
    }

    private boolean isCgLibLogTrace(final String s) {
        return StringUtils.contains(s, "net.sf.cglib.proxy");
    }

    static class SampleClass {
        public void theFirstMethodThatCapturesAnException() {
            try {
                methodThatCapturesAndThrowsException();
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }

        private void methodThatCapturesAndThrowsException() throws Exception {
            try {
                methodThatThrowsAnError();
            } catch (final Error e) {
                throw new Exception("Throws an exception", e);
            }
        }

        private void methodThatThrowsAnError() {
            throw new Error("Exception to test the CglibThrowableRenderer.");
        }
    }

    static class MyInvocationHandler implements MethodInterceptor {
        @Override
        public Object intercept(final Object obj, final Method method, final Object[] args, final MethodProxy proxy) throws Throwable {
            return proxy.invoke(new SampleClass(), args);
        }
    }
}
