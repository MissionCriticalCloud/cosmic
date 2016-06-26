package com.cloud.api;

import com.cloud.utils.encoding.URLEncoder;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncodedStringTypeAdapter implements JsonSerializer<String> {
    public static final Logger s_logger = LoggerFactory.getLogger(EncodedStringTypeAdapter.class.getName());

    @Override
    public JsonElement serialize(final String src, final Type typeOfResponseObj, final JsonSerializationContext ctx) {
        return new JsonPrimitive(encodeString(src));
    }

    private static String encodeString(final String value) {
        if (!ApiServer.isEncodeApiResponse()) {
            return value;
        }
        try {
            return new URLEncoder().encode(value).replaceAll("\\+", "%20");
        } catch (final Exception e) {
            s_logger.warn("Unable to encode: " + value, e);
        }
        return value;
    }
}
