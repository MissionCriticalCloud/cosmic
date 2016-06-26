package org.apache.cloudstack.framework.serializer;

public interface MessageSerializer {
    <T> String serializeTo(Class<?> clz, T object);

    <T> T serializeFrom(String message);
}
