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
package org.apache.cloudstack.api.command;

import com.cloud.user.Account;
import com.cloud.utils.Pair;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.LdapConfigurationResponse;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.ldap.LdapConfigurationVO;
import org.apache.cloudstack.ldap.LdapManager;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listLdapConfigurations", responseObject = LdapConfigurationResponse.class, description = "Lists all LDAP configurations", since = "4.2.0",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class LdapListConfigurationCmd extends BaseListCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(LdapListConfigurationCmd.class.getName());

    private static final String s_name = "ldapconfigurationresponse";

    @Inject
    private LdapManager _ldapManager;

    @Parameter(name = "hostname", type = CommandType.STRING, required = false, description = "Hostname")
    private String hostname;

    @Parameter(name = "port", type = CommandType.INTEGER, required = false, description = "Port")
    private int port;

    public LdapListConfigurationCmd() {
        super();
    }

    public LdapListConfigurationCmd(final LdapManager ldapManager) {
        super();
        _ldapManager = ldapManager;
    }

    @Override
    public void execute() {
        final Pair<List<? extends LdapConfigurationVO>, Integer> result = _ldapManager.listConfigurations(this);
        final List<LdapConfigurationResponse> responses = createLdapConfigurationResponses(result.first());
        final ListResponse<LdapConfigurationResponse> response = new ListResponse<>();
        response.setResponses(responses, result.second());
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    private List<LdapConfigurationResponse> createLdapConfigurationResponses(final List<? extends LdapConfigurationVO> configurations) {
        final List<LdapConfigurationResponse> responses = new ArrayList<>();
        for (final LdapConfigurationVO resource : configurations) {
            final LdapConfigurationResponse configurationResponse = _ldapManager.createLdapConfigurationResponse(resource);
            configurationResponse.setObjectName("LdapConfiguration");
            responses.add(configurationResponse);
        }
        return responses;
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(final String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }
}
