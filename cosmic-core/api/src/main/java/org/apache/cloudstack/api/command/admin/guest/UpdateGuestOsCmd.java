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
package org.apache.cloudstack.api.command.admin.guest;

import com.cloud.event.EventTypes;
import com.cloud.storage.GuestOS;
import com.cloud.user.Account;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiCommandJobType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.GuestOSResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateGuestOs", description = "Updates the information about Guest OS", responseObject = GuestOSResponse.class,
        since = "4.4.0", requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class UpdateGuestOsCmd extends BaseAsyncCmd {

    public static final Logger s_logger = LoggerFactory.getLogger(UpdateGuestOsCmd.class.getName());

    private static final String s_name = "updateguestosresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = GuestOSResponse.class, required = true, description = "UUID of the Guest OS")
    private Long id;

    @Parameter(name = ApiConstants.OS_DISPLAY_NAME, type = CommandType.STRING, required = true, description = "Unique display name for Guest OS")
    private String osDisplayName;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public String getOsDisplayName() {
        return osDisplayName;
    }

    @Override
    public void execute() {
        GuestOS guestOs = _mgr.updateGuestOs(this);
        if (guestOs != null) {
            GuestOSResponse response = _responseGenerator.createGuestOSResponse(guestOs);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to update guest OS type");
        }
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_GUEST_OS_UPDATE;
    }

    @Override
    public String getEventDescription() {
        return "Updating guest OS: " + getId();
    }

    public Long getId() {
        return id;
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.GuestOs;
    }
}
