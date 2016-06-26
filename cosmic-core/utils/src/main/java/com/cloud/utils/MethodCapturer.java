//

//

package com.cloud.utils;

import java.lang.reflect.Method;
import java.util.WeakHashMap;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/*
 * This helper class provides a way to retrieve Method in a strong-type way. It takes advantage of power of
 * Intelligent IDE(Eclipse) in code-editing
 *
 * DummyImpl dummy = new DummyImpl();
 * MethodCapturer<DummyImpl> capturer = MethodCapturer.capture(dummy);
 * Method method = capturer.get(capturer.instance().foo2());
 *
 */
public class MethodCapturer<T> {

    private final static int CACHE_SIZE = 1024;
    private static final WeakHashMap<Object, Object> s_cache = new WeakHashMap<>();
    private T _instance;
    private Method _method;

    private MethodCapturer() {
    }

    public static <T> MethodCapturer<T> capture(final T obj) {
        synchronized (s_cache) {
            final MethodCapturer<T> capturer = (MethodCapturer<T>) s_cache.get(obj);
            if (capturer != null) {
                return capturer;
            }

            final MethodCapturer<T> capturerNew = new MethodCapturer<>();

            final Enhancer en = new Enhancer();
            en.setSuperclass(obj.getClass());
            en.setCallbacks(new Callback[]{new MethodInterceptor() {
                @Override
                public Object intercept(final Object arg0, final Method arg1, final Object[] arg2, final MethodProxy arg3) throws Throwable {
                    capturerNew.setMethod(arg1);
                    return null;
                }
            }, new MethodInterceptor() {
                @Override
                public Object intercept(final Object arg0, final Method arg1, final Object[] arg2, final MethodProxy arg3) throws Throwable {
                    return null;
                }
            }});
            en.setCallbackFilter(new CallbackFilter() {
                @Override
                public int accept(final Method method) {
                    if (method.getParameterTypes().length == 0 && method.getName().equals("finalize")) {
                        return 1;
                    }
                    return 0;
                }
            });

            capturerNew.setInstance((T) en.create());

            // We expect MethodCapturer is only used for singleton objects here, so we only maintain a limited cache
            // here
            if (s_cache.size() < CACHE_SIZE) {
                s_cache.put(obj, capturerNew);
            }

            return capturerNew;
        }
    }

    private void setMethod(final Method method) {
        _method = method;
    }

    private void setInstance(final T instance) {
        _instance = instance;
    }

    public T instance() {
        return _instance;
    }

    public Method get(final Object... useless) {
        return _method;
    }
}
