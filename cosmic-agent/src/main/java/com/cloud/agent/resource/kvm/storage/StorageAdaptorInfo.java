package com.cloud.agent.resource.kvm.storage;

import static java.lang.annotation.ElementType.TYPE;

import com.cloud.model.enumeration.StoragePoolType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({TYPE})
public @interface StorageAdaptorInfo {
    StoragePoolType storagePoolType();
}
