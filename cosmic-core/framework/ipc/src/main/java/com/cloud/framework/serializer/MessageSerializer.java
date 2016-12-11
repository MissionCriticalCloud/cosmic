package com.cloud.framework.serializer;

public interface MessageSerializer {
    <T> String serializeTo(Class<?> clz, T object);

    <T> T serializeFrom(String message);
}
