package com.cloud.api;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class StringMapTypeAdapter implements JsonDeserializer<Map> {
    @Override
    public Map deserialize(final JsonElement src, final Type srcType, final JsonDeserializationContext context) throws JsonParseException {

        final Map<String, String> obj = new HashMap<>();
        final JsonObject json = src.getAsJsonObject();

        for (final Entry<String, JsonElement> entry : json.entrySet()) {
            obj.put(entry.getKey(), entry.getValue().getAsString());
        }

        return obj;
    }
}
