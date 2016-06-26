//
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
//

package com.cloud.agent.resource.virtualnetwork.model;

public class IcmpAclRule extends AclRule {
    private final String type = "icmp";
    private int icmpType;
    private int icmpCode;

    public IcmpAclRule() {
        // Empty constructor for (de)serialization
    }

    public IcmpAclRule(final String cidr, final boolean allowed, final int icmpType, final int icmpCode) {
        super(cidr, allowed);
        this.icmpType = icmpType;
        this.icmpCode = icmpCode;
    }

    public int getIcmpType() {
        return icmpType;
    }

    public void setIcmpType(final int icmpType) {
        this.icmpType = icmpType;
    }

    public int getIcmpCode() {
        return icmpCode;
    }

    public void setIcmpCode(final int icmpCode) {
        this.icmpCode = icmpCode;
    }
}
