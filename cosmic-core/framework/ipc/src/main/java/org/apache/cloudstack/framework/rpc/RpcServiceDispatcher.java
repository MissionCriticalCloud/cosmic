package org.apache.cloudstack.framework.rpc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class RpcServiceDispatcher implements RpcServiceEndpoint {

    private static final Map<Class<?>, Map<String, Method>> s_handlerCache = new HashMap<>();

    private static final Map<Object, RpcServiceDispatcher> s_targetMap = new HashMap<>();
    private final Object _targetObject;

    public RpcServiceDispatcher(final Object targetObject) {
        _targetObject = targetObject;
    }

    public static RpcServiceDispatcher getDispatcher(final Object targetObject) {
        RpcServiceDispatcher dispatcher;
        synchronized (s_targetMap) {
            dispatcher = s_targetMap.get(targetObject);
            if (dispatcher == null) {
                dispatcher = new RpcServiceDispatcher(targetObject);
                s_targetMap.put(targetObject, dispatcher);
            }
        }
        return dispatcher;
    }

    public static void removeDispatcher(final Object targetObject) {
        synchronized (s_targetMap) {
            s_targetMap.remove(targetObject);
        }
    }

    public static Method resolveHandler(final Class<?> handlerClz, final String command) {
        synchronized (s_handlerCache) {
            final Map<String, Method> handlerMap = getAndSetHandlerMap(handlerClz);

            final Method handler = handlerMap.get(command);
            if (handler != null) {
                return handler;
            }

            for (final Method method : handlerClz.getDeclaredMethods()) {
                final RpcServiceHandler annotation = method.getAnnotation(RpcServiceHandler.class);
                if (annotation != null) {
                    if (annotation.command().equals(command)) {
                        method.setAccessible(true);
                        handlerMap.put(command, method);
                        return method;
                    }
                }
            }
        }

        return null;
    }

    private static Map<String, Method> getAndSetHandlerMap(final Class<?> handlerClz) {
        Map<String, Method> handlerMap;
        synchronized (s_handlerCache) {
            handlerMap = s_handlerCache.get(handlerClz);

            if (handlerMap == null) {
                handlerMap = new HashMap<>();
                s_handlerCache.put(handlerClz, handlerMap);
            }
        }

        return handlerMap;
    }

    @Override
    public boolean onCallReceive(final RpcServerCall call) {
        return dispatch(_targetObject, call);
    }

    public static boolean dispatch(final Object target, final RpcServerCall serviceCall) {
        assert (serviceCall != null);
        assert (target != null);

        final Method handler = resolveHandler(target.getClass(), serviceCall.getCommand());
        if (handler == null) {
            return false;
        }

        try {
            handler.invoke(target, serviceCall);
        } catch (final IllegalArgumentException e) {
            throw new RpcException("IllegalArgumentException when invoking RPC service command: " + serviceCall.getCommand());
        } catch (final IllegalAccessException e) {
            throw new RpcException("IllegalAccessException when invoking RPC service command: " + serviceCall.getCommand());
        } catch (final InvocationTargetException e) {
            throw new RpcException("InvocationTargetException when invoking RPC service command: " + serviceCall.getCommand());
        }

        return true;
    }
}
