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
package com.cloud.storage;

import org.apache.cloudstack.api.InternalIdentity;

import java.util.Date;

public interface VMTemplateStorageResourceAssoc extends InternalIdentity {
    String getInstallPath();

    void setInstallPath(String installPath);

    long getTemplateId();

    void setTemplateId(long templateId);

    int getDownloadPercent();

    void setDownloadPercent(int downloadPercent);

    Date getCreated();

    Date getLastUpdated();

    void setLastUpdated(Date date);

    Status getDownloadState();

    void setDownloadState(Status downloadState);

    String getLocalDownloadPath();

    void setLocalDownloadPath(String localPath);

    String getErrorString();

    void setErrorString(String errorString);

    String getJobId();

    void setJobId(String jobId);

    long getTemplateSize();

    public static enum Status {
        UNKNOWN, DOWNLOAD_ERROR, NOT_DOWNLOADED, DOWNLOAD_IN_PROGRESS, DOWNLOADED, ABANDONED, UPLOADED, NOT_UPLOADED, UPLOAD_ERROR, UPLOAD_IN_PROGRESS, CREATING, CREATED
    }
}
