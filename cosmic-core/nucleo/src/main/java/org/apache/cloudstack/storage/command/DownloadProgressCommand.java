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

package org.apache.cloudstack.storage.command;

public class DownloadProgressCommand extends DownloadCommand {
    private String jobId;
    private RequestType request;

    protected DownloadProgressCommand() {
        super();
    }

    public DownloadProgressCommand(final DownloadCommand cmd, final String jobId, final RequestType req) {
        super(cmd);

        this.jobId = jobId;
        this.setRequest(req);
    }

    public String getJobId() {
        return jobId;
    }

    public RequestType getRequest() {
        return request;
    }

    public void setRequest(final RequestType request) {
        this.request = request;
    }

    public static enum RequestType {
        GET_STATUS, ABORT, RESTART, PURGE, GET_OR_RESTART
    }
}
