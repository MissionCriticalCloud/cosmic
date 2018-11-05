package com.cloud.api;

import java.lang.reflect.Field;
import java.util.HashMap;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

/***
 * This Class excludes serialized names which are overwritten in the inherited class.
 * GSON 1.7.2 replaced all the serialized names which are the same but from 2.4 it
 * generates an exception. This class will skip the names in the base classes.
 */
public class SuperclassExclusionStrategy implements ExclusionStrategy {
    private final HashMap<Class, Class> inheritanceMap = new HashMap<>();

    public boolean shouldSkipClass(Class<?> arg0) {
        return false;
    }

    public boolean shouldSkipField(FieldAttributes fieldAttributes) {
        String fieldName = fieldAttributes.getName();
        Class<?> theClass = fieldAttributes.getDeclaringClass();
        Class<?> superclass = theClass.getSuperclass();

        if (this.inheritanceMap.containsKey(theClass)) {
            if (getField(this.inheritanceMap.get(theClass), fieldName) != null) {
                return true;
            }
        }

        this.inheritanceMap.put(superclass, theClass);
        return false;
    }

    private Field getField(Class<?> theClass, String fieldName) {
        try {
            return theClass.getDeclaredField(fieldName);
        } catch (Exception e) {
            return null;
        }
    }
}
