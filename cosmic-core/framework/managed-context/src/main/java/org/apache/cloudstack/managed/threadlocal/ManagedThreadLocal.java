/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cloudstack.managed.threadlocal;

import org.apache.cloudstack.managed.context.ManagedContextUtils;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagedThreadLocal<T> extends ThreadLocal<T> {

    private static final ThreadLocal<Map<Object, Object>> MANAGED_THREAD_LOCAL = new ThreadLocal<Map<Object, Object>>() {
        @Override
        protected Map<Object, Object> initialValue() {
            return new HashMap<>();
        }
    };
    private static final Logger log = LoggerFactory.getLogger(ManagedThreadLocal.class);
    private static boolean s_validateContext = false;

    public static void reset() {
        validateInContext(null);
        MANAGED_THREAD_LOCAL.remove();
    }

    private static void validateInContext(final Object tl) {
        if (s_validateContext && !ManagedContextUtils.isInContext()) {
            final String msg = "Using a managed thread local in a non managed context this WILL cause errors at runtime. TL [" + tl + "]";
            log.error(msg, new IllegalStateException(msg));
        }
    }

    @Override
    public T get() {
        validateInContext(this);
        final Map<Object, Object> map = MANAGED_THREAD_LOCAL.get();
        Object result = map.get(this);
        if (result == null) {
            result = initialValue();
            map.put(this, result);
        }
        return (T) result;
    }

    public static void setValidateInContext(final boolean validate) {
        s_validateContext = validate;
    }

    @Override
    public void set(final T value) {
        validateInContext(this);
        final Map<Object, Object> map = MANAGED_THREAD_LOCAL.get();
        map.put(this, value);
    }

    @Override
    public void remove() {
        final Map<Object, Object> map = MANAGED_THREAD_LOCAL.get();
        map.remove(this);
    }
}
