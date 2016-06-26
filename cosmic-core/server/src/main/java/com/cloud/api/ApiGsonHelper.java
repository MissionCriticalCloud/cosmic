package com.cloud.api;

import org.apache.cloudstack.api.ResponseObject;

import java.util.Map;

import com.google.gson.GsonBuilder;

public class ApiGsonHelper {
    private static final GsonBuilder s_gBuilder;

    static {
        s_gBuilder = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        s_gBuilder.setVersion(1.3);
        s_gBuilder.registerTypeAdapter(ResponseObject.class, new ResponseObjectTypeAdapter());
        s_gBuilder.registerTypeAdapter(Map.class, new StringMapTypeAdapter());
    }

    public static GsonBuilder getBuilder() {
        return s_gBuilder;
    }
}
