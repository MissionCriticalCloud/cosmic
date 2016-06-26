package com.cloud.api.response;

import com.cloud.serializer.Param;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class EmptyFieldExclusionStrategy implements ExclusionStrategy {

    @Override
    public boolean shouldSkipField(final FieldAttributes fieldAttributes) {
        if (fieldAttributes.getAnnotation(Param.class) != null) {
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldSkipClass(final Class<?> aClass) {
        return false;
    }
}
