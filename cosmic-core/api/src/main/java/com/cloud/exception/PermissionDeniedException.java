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
package com.cloud.exception;

import com.cloud.user.Account;
import com.cloud.utils.SerialVersionUID;
import com.cloud.utils.exception.CloudRuntimeException;
import org.apache.cloudstack.acl.ControlledEntity;

import java.util.List;

public class PermissionDeniedException extends CloudRuntimeException {

    private static final long serialVersionUID = SerialVersionUID.PermissionDeniedException;
    List<? extends ControlledEntity> violations;
    Account account;

    public PermissionDeniedException(final String message) {
        super(message);
    }

    public PermissionDeniedException(final String message, final Throwable cause) {
        super(message, cause);
    }

    protected PermissionDeniedException() {
        super();
    }

    public PermissionDeniedException(final String message, final Account account, final List<? extends ControlledEntity> violations) {
        super(message);
        this.violations = violations;
        this.account = account;
    }

    public Account getAccount() {
        return account;
    }

    public List<? extends ControlledEntity> getEntitiesInViolation() {
        return violations;
    }

    public void addDetails(final Account account, final List<? extends ControlledEntity> violations) {
        this.account = account;
        this.violations = violations;
    }

    public void addViolations(final List<? extends ControlledEntity> violations) {
        this.violations = violations;
    }
}
