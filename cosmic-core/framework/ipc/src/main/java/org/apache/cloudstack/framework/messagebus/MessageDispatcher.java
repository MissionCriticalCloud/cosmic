package org.apache.cloudstack.framework.messagebus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageDispatcher implements MessageSubscriber {
    private static final Logger s_logger = LoggerFactory.getLogger(MessageDispatcher.class);

    private static final Map<Class<?>, List<Method>> s_handlerCache = new HashMap<>();

    private static final Map<Object, MessageDispatcher> s_targetMap = new HashMap<>();
    private final Object _targetObject;

    public MessageDispatcher(final Object targetObject) {
        _targetObject = targetObject;
        buildHandlerMethodCache(targetObject.getClass());
    }

    private void buildHandlerMethodCache(final Class<?> handlerClz) {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Build message handler cache for " + handlerClz.getName());
        }

        synchronized (s_handlerCache) {
            List<Method> handlerList = s_handlerCache.get(handlerClz);
            if (handlerList == null) {
                handlerList = new ArrayList<>();
                s_handlerCache.put(handlerClz, handlerList);

                Class<?> clz = handlerClz;
                while (clz != null && clz != Object.class) {
                    for (final Method method : clz.getDeclaredMethods()) {
                        final MessageHandler annotation = method.getAnnotation(MessageHandler.class);
                        if (annotation != null) {
                            // allow private member access via reflection
                            method.setAccessible(true);
                            handlerList.add(method);

                            if (s_logger.isInfoEnabled()) {
                                s_logger.info("Add message handler " + handlerClz.getName() + "." + method.getName() + " to cache");
                            }
                        }
                    }

                    clz = clz.getSuperclass();
                }
            } else {
                if (s_logger.isInfoEnabled()) {
                    s_logger.info("Message handler for class " + handlerClz.getName() + " is already in cache");
                }
            }
        }

        if (s_logger.isInfoEnabled()) {
            s_logger.info("Done building message handler cache for " + handlerClz.getName());
        }
    }

    public static MessageDispatcher getDispatcher(final Object targetObject) {
        MessageDispatcher dispatcher;
        synchronized (s_targetMap) {
            dispatcher = s_targetMap.get(targetObject);
            if (dispatcher == null) {
                dispatcher = new MessageDispatcher(targetObject);
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

    @Override
    public void onPublishMessage(final String senderAddress, final String subject, final Object args) {
        dispatch(_targetObject, subject, senderAddress, args);
    }

    public static boolean dispatch(final Object target, final String subject, final String senderAddress, final Object args) {
        assert (subject != null);
        assert (target != null);

        final Method handler = resolveHandler(target.getClass(), subject);
        if (handler == null) {
            return false;
        }

        try {
            handler.invoke(target, subject, senderAddress, args);
        } catch (final IllegalArgumentException e) {
            s_logger.error("Unexpected exception when calling " + target.getClass().getName() + "." + handler.getName(), e);
            throw new RuntimeException("IllegalArgumentException when invoking event handler for subject: " + subject);
        } catch (final IllegalAccessException e) {
            s_logger.error("Unexpected exception when calling " + target.getClass().getName() + "." + handler.getName(), e);
            throw new RuntimeException("IllegalAccessException when invoking event handler for subject: " + subject);
        } catch (final InvocationTargetException e) {
            s_logger.error("Unexpected exception when calling " + target.getClass().getName() + "." + handler.getName(), e);
            throw new RuntimeException("InvocationTargetException when invoking event handler for subject: " + subject);
        }

        return true;
    }

    public static Method resolveHandler(final Class<?> handlerClz, final String subject) {
        synchronized (s_handlerCache) {
            final List<Method> handlerList = s_handlerCache.get(handlerClz);
            if (handlerList != null) {
                for (final Method method : handlerList) {
                    final MessageHandler annotation = method.getAnnotation(MessageHandler.class);
                    assert (annotation != null);

                    if (match(annotation.topic(), subject)) {
                        return method;
                    }
                }
            } else {
                s_logger.error("Handler class " + handlerClz.getName() + " is not registered");
            }
        }

        return null;
    }

    private static boolean match(final String expression, final String param) {
        return param.matches(expression);
    }
}
