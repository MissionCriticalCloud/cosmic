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
package org.apache.cloudstack.api.command.admin.vm;

import com.cloud.event.EventTypes;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.ManagementServerException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.exception.VirtualMachineMigrationException;
import com.cloud.host.Host;
import com.cloud.user.Account;
import com.cloud.uservm.UserVm;
import com.cloud.vm.VirtualMachine;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.HostResponse;
import org.apache.cloudstack.api.response.UserVmResponse;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "migrateVirtualMachineWithVolume",
        description = "Attempts Migration of a VM with its volumes to a different host",
        responseObject = UserVmResponse.class, entityType = {VirtualMachine.class},
        requestHasSensitiveInfo = false,
        responseHasSensitiveInfo = true)
public class MigrateVirtualMachineWithVolumeCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(MigrateVMCmd.class.getName());

    private static final String s_name = "migratevirtualmachinewithvolumeresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.HOST_ID,
            type = CommandType.UUID,
            entityType = HostResponse.class,
            required = true,
            description = "Destination Host ID to migrate VM to.")
    private Long hostId;

    @Parameter(name = ApiConstants.VIRTUAL_MACHINE_ID,
            type = CommandType.UUID,
            entityType = UserVmResponse.class,
            required = true,
            description = "the ID of the virtual machine")
    private Long virtualMachineId;

    @Parameter(name = ApiConstants.MIGRATE_TO,
            type = CommandType.MAP,
            required = false,
            description = "Storage to pool mapping. This parameter specifies the mapping between a volume and a pool where you want to migrate that volume. Format of this " +
                    "parameter: migrateto[volume-index].volume=<uuid>&migrateto[volume-index].pool=<uuid>Where, [volume-index] indicates the index to identify the volume that " +
                    "you " +
                    "want to migrate, volume=<uuid> indicates the UUID of the volume that you want to migrate, and pool=<uuid> indicates the UUID of the pool where you want to " +
                    "migrate the volume. Example: migrateto[0].volume=<71f43cd6-69b0-4d3b-9fbc-67f50963d60b>&migrateto[0].pool=<a382f181-3d2b-4413-b92d-b8931befa7e1>&" +
                    "migrateto[1].volume=<88de0173-55c0-4c1c-a269-83d0279eeedf>&migrateto[1].pool=<95d6e97c-6766-4d67-9a30-c449c15011d1>&migrateto[2].volume=" +
                    "<1b331390-59f2-4796-9993-bf11c6e76225>&migrateto[2].pool=<41fdb564-9d3b-447d-88ed-7628f7640cbc>")
    private Map migrateVolumeTo;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventType() {
        return EventTypes.EVENT_VM_MIGRATE;
    }

    @Override
    public String getEventDescription() {
        return "Attempting to migrate VM Id: " + getVirtualMachineId() + " to host Id: " + getHostId();
    }

    public Long getVirtualMachineId() {
        return virtualMachineId;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    public Long getHostId() {
        return hostId;
    }

    @Override
    public void execute() {
        final UserVm userVm = _userVmService.getUserVm(getVirtualMachineId());
        if (userVm == null) {
            throw new InvalidParameterValueException("Unable to find the VM by id=" + getVirtualMachineId());
        }

        final Host destinationHost = _resourceService.getHost(getHostId());
        if (destinationHost == null) {
            throw new InvalidParameterValueException("Unable to find the host to migrate the VM, host id =" + getHostId());
        }

        try {
            final VirtualMachine migratedVm = _userVmService.migrateVirtualMachineWithVolume(getVirtualMachineId(), destinationHost, getVolumeToPool());
            if (migratedVm != null) {
                final UserVmResponse response = _responseGenerator.createUserVmResponse(ResponseView.Full, "virtualmachine", (UserVm) migratedVm).get(0);
                response.setResponseName(getCommandName());
                setResponseObject(response);
            } else {
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to migrate vm");
            }
        } catch (final ResourceUnavailableException ex) {
            s_logger.warn("Exception: ", ex);
            throw new ServerApiException(ApiErrorCode.RESOURCE_UNAVAILABLE_ERROR, ex.getMessage());
        } catch (final ConcurrentOperationException e) {
            s_logger.warn("Exception: ", e);
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, e.getMessage());
        } catch (final ManagementServerException e) {
            s_logger.warn("Exception: ", e);
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, e.getMessage());
        } catch (final VirtualMachineMigrationException e) {
            s_logger.warn("Exception: ", e);
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, e.getMessage());
        }
    }

    public Map<String, String> getVolumeToPool() {
        final Map<String, String> volumeToPoolMap = new HashMap<>();
        if (migrateVolumeTo != null && !migrateVolumeTo.isEmpty()) {
            final Collection<?> allValues = migrateVolumeTo.values();
            final Iterator<?> iter = allValues.iterator();
            while (iter.hasNext()) {
                final HashMap<String, String> volumeToPool = (HashMap<String, String>) iter.next();
                final String volume = volumeToPool.get("volume");
                final String pool = volumeToPool.get("pool");
                volumeToPoolMap.put(volume, pool);
            }
        }
        return volumeToPoolMap;
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final UserVm userVm = _entityMgr.findById(UserVm.class, getVirtualMachineId());
        if (userVm != null) {
            return userVm.getAccountId();
        }

        return Account.ACCOUNT_ID_SYSTEM;
    }
}
