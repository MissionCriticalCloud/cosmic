package com.cloud.hypervisor.kvm.storage;

import static java.lang.annotation.ElementType.TYPE;

import com.cloud.storage.Storage.StoragePoolType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({TYPE})
public @interface StorageAdaptorInfo {
    StoragePoolType storagePoolType();
}
