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

package com.cloud.network.rules;

import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.Network;
import com.cloud.network.VpnUser;
import com.cloud.network.router.VirtualRouter;
import org.apache.cloudstack.network.topology.NetworkTopologyVisitor;

import java.util.List;

public class BasicVpnRules extends RuleApplier {

    private final List<? extends VpnUser> _users;

    public BasicVpnRules(final Network network, final List<? extends VpnUser> users) {
        super(network);
        _users = users;
    }

    @Override
    public boolean accept(final NetworkTopologyVisitor visitor, final VirtualRouter router) throws ResourceUnavailableException {
        _router = router;

        return visitor.visit(this);
    }

    public List<? extends VpnUser> getUsers() {
        return _users;
    }
}
