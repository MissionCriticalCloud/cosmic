//

//

package com.cloud.utils.events;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubscriptionMgr {
    protected final static Logger s_logger = LoggerFactory.getLogger(SubscriptionMgr.class);

    private static final SubscriptionMgr s_instance = new SubscriptionMgr();

    private final Map<String, List<SubscriberInfo>> registry;

    private SubscriptionMgr() {
        registry = new HashMap<>();
    }

    public static SubscriptionMgr getInstance() {
        return s_instance;
    }

    public <T> void subscribe(final String subject, final T subscriber, final String listenerMethod) throws SecurityException, NoSuchMethodException {

        synchronized (this) {
            final List<SubscriberInfo> l = getAndSetSubscriberList(subject);

            final Class<?> clazz = subscriber.getClass();
            final SubscriberInfo subscribeInfo = new SubscriberInfo(clazz, subscriber, listenerMethod);

            if (!l.contains(subscribeInfo)) {
                l.add(subscribeInfo);
            }
        }
    }

    private List<SubscriberInfo> getAndSetSubscriberList(final String subject) {
        List<SubscriberInfo> l = registry.get(subject);
        if (l == null) {
            l = new ArrayList<>();
            registry.put(subject, l);
        }

        return l;
    }

    public <T> void unsubscribe(final String subject, final T subscriber, final String listenerMethod) {
        synchronized (this) {
            final List<SubscriberInfo> l = getSubscriberList(subject);
            if (l != null) {
                for (final SubscriberInfo info : l) {
                    if (info.isMe(subscriber.getClass(), subscriber, listenerMethod)) {
                        l.remove(info);
                        return;
                    }
                }
            }
        }
    }

    private List<SubscriberInfo> getSubscriberList(final String subject) {
        return registry.get(subject);
    }

    public void notifySubscribers(final String subject, final Object sender, final EventArgs args) {

        final List<SubscriberInfo> l = getExecutableSubscriberList(subject);
        if (l != null) {
            for (final SubscriberInfo info : l) {
                try {
                    info.execute(sender, args);
                } catch (final IllegalArgumentException e) {
                    s_logger.warn("Exception on notifying event subscribers: ", e);
                } catch (final IllegalAccessException e) {
                    s_logger.warn("Exception on notifying event subscribers: ", e);
                } catch (final InvocationTargetException e) {
                    s_logger.warn("Exception on notifying event subscribers: ", e);
                }
            }
        }
    }

    private synchronized List<SubscriberInfo> getExecutableSubscriberList(final String subject) {
        final List<SubscriberInfo> l = registry.get(subject);
        if (l != null) {
            // do a shadow clone
            final ArrayList<SubscriberInfo> clonedList = new ArrayList<>(l.size());
            for (final SubscriberInfo info : l) {
                clonedList.add(info);
            }

            return clonedList;
        }
        return null;
    }

    private static class SubscriberInfo {
        private final Class<?> clazz;
        private final Object subscriber;
        private final String methodName;
        private Method method;

        public SubscriberInfo(final Class<?> clazz, final Object subscriber, final String methodName) throws SecurityException, NoSuchMethodException {

            this.clazz = clazz;
            this.subscriber = subscriber;
            this.methodName = methodName;
            for (final Method method : clazz.getMethods()) {
                if (method.getName().equals(methodName)) {
                    final Class<?>[] paramTypes = method.getParameterTypes();
                    if (paramTypes != null && paramTypes.length == 2 && paramTypes[0] == Object.class && EventArgs.class.isAssignableFrom(paramTypes[1])) {
                        this.method = method;

                        break;
                    }
                }
            }
            if (this.method == null) {
                throw new NoSuchMethodException();
            }
        }

        public void execute(final Object sender, final EventArgs args) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {

            method.invoke(subscriber, sender, args);
        }

        public boolean isMe(final Class<?> clazz, final Object subscriber, final String methodName) {
            return this.clazz == clazz && this.subscriber == subscriber && this.methodName.equals(methodName);
        }

        @Override
        public boolean equals(final Object o) {
            if (o == null) {
                return false;
            }

            if (o instanceof SubscriberInfo) {
                return this.clazz == ((SubscriberInfo) o).clazz && this.subscriber == ((SubscriberInfo) o).subscriber &&
                        this.methodName.equals(((SubscriberInfo) o).methodName);
            }
            return false;
        }
    }
}
