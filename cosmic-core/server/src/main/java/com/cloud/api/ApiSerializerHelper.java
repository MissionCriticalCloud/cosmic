// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package com.cloud.api;

import org.apache.cloudstack.api.ResponseObject;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiSerializerHelper {
    public static final Logger s_logger = LoggerFactory.getLogger(ApiSerializerHelper.class.getName());
    private static final String token = "/";

    public static String toSerializedString(final Object result) {
        if (result != null) {
            final Class<?> clz = result.getClass();
            final Gson gson = ApiGsonHelper.getBuilder().create();

            if (result instanceof ResponseObject) {
                return clz.getName() + token + ((ResponseObject) result).getObjectName() + token + gson.toJson(result);
            } else {
                return clz.getName() + token + gson.toJson(result);
            }
        }
        return null;
    }

    public static Object fromSerializedString(final String result) {
        try {
            if (result != null && !result.isEmpty()) {

                final String[] serializedParts = result.split(token);

                if (serializedParts.length < 2) {
                    return null;
                }
                final String clzName = serializedParts[0];
                String nameField = null;
                String content = null;
                if (serializedParts.length == 2) {
                    content = serializedParts[1];
                } else {
                    nameField = serializedParts[1];
                    final int index = result.indexOf(token + nameField + token);
                    content = result.substring(index + nameField.length() + 2);
                }

                final Class<?> clz;
                try {
                    clz = Class.forName(clzName);
                } catch (final ClassNotFoundException e) {
                    return null;
                }

                final Gson gson = ApiGsonHelper.getBuilder().create();
                final Object obj = gson.fromJson(content, clz);
                if (nameField != null) {
                    ((ResponseObject) obj).setObjectName(nameField);
                }
                return obj;
            }
            return null;
        } catch (final RuntimeException e) {
            s_logger.error("Caught runtime exception when doing GSON deserialization on: " + result);
            throw e;
        }
    }
}
