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

package com.cloud.hypervisor.ovm3.objects;

public class Common extends OvmObject {

    public Common(final Connection connection) {
        setClient(connection);
    }

    public Integer getApiVersion() throws Ovm3ResourceException {
        final Object[] x = (Object[]) callWrapper("get_api_version");
        return (Integer) x[0];
    }

    public Boolean sleep(final int seconds) throws Ovm3ResourceException {
        return nullIsTrueCallWrapper("sleep", seconds);
    }

    public <T> String dispatch(final String url, final String function, final T... args) throws Ovm3ResourceException {
        return callString("dispatch", url, function, args);
    }

    public String echo(final String msg) throws Ovm3ResourceException {
        return callString("echo", msg);
    }
}
