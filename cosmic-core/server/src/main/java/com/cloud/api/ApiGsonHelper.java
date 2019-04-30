package com.cloud.api;

import com.cloud.utils.SuperclassExclusionStrategy;

import java.util.Map;

import com.google.gson.GsonBuilder;

public class ApiGsonHelper {
    private static final GsonBuilder s_gBuilder;

    static {
        s_gBuilder = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        s_gBuilder.setVersion(1.3);
        s_gBuilder.registerTypeAdapter(ResponseObject.class, new ResponseObjectTypeAdapter());
        s_gBuilder.setExclusionStrategies(new SuperclassExclusionStrategy());
        s_gBuilder.registerTypeAdapter(Map.class, new StringMapTypeAdapter());
    }

    public static GsonBuilder getBuilder() {
        return s_gBuilder;
    }
}
