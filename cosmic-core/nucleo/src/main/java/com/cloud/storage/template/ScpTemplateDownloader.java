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

package com.cloud.storage.template;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import com.cloud.storage.StorageLayer;
import com.cloud.utils.exception.CloudRuntimeException;
import com.trilead.ssh2.SCPClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScpTemplateDownloader extends TemplateDownloaderBase implements TemplateDownloader {
    private static final Logger s_logger = LoggerFactory.getLogger(ScpTemplateDownloader.class);

    public ScpTemplateDownloader(StorageLayer storageLayer, String downloadUrl, String toDir, long maxTemplateSizeInBytes, DownloadCompleteCallback callback) {
        super(storageLayer, downloadUrl, toDir, maxTemplateSizeInBytes, callback);

        URI uri;
        try {
            uri = new URI(_downloadUrl);
        } catch (final URISyntaxException e) {
            s_logger.warn("URI syntax error: " + _downloadUrl);
            _status = Status.UNRECOVERABLE_ERROR;
            return;
        }

        final String path = uri.getPath();
        final String filename = path.substring(path.lastIndexOf("/") + 1);
        _toFile = toDir + File.separator + filename;
    }

    @Override
    public long download(boolean resume, DownloadCompleteCallback callback) {
        if (_status == Status.ABORTED || _status == Status.UNRECOVERABLE_ERROR || _status == Status.DOWNLOAD_FINISHED) {
            return 0;
        }

        _resume = resume;

        _start = System.currentTimeMillis();

        URI uri;
        try {
            uri = new URI(_downloadUrl);
        } catch (final URISyntaxException e1) {
            _status = Status.UNRECOVERABLE_ERROR;
            return 0;
        }

        final String username = uri.getUserInfo();
        final String queries = uri.getQuery();
        String password = null;
        if (queries != null) {
            final String[] qs = queries.split("&");
            for (final String q : qs) {
                final String[] tokens = q.split("=");
                if (tokens[0].equalsIgnoreCase("password")) {
                    password = tokens[1];
                    break;
                }
            }
        }
        int port = uri.getPort();
        if (port == -1) {
            port = 22;
        }
        final File file = new File(_toFile);

        final com.trilead.ssh2.Connection sshConnection = new com.trilead.ssh2.Connection(uri.getHost(), port);
        try {
            if (_storage != null) {
                file.createNewFile();
                _storage.setWorldReadableAndWriteable(file);
            }

            sshConnection.connect(null, 60000, 60000);
            if (!sshConnection.authenticateWithPassword(username, password)) {
                throw new CloudRuntimeException("Unable to authenticate");
            }

            final SCPClient scp = new SCPClient(sshConnection);

            final String src = uri.getPath();

            _status = Status.IN_PROGRESS;
            scp.get(src, _toDir);

            if (!file.exists()) {
                _status = Status.UNRECOVERABLE_ERROR;
                s_logger.debug("unable to scp the file " + _downloadUrl);
                return 0;
            }

            _status = Status.DOWNLOAD_FINISHED;

            _totalBytes = file.length();

            final String downloaded = "(download complete)";

            _errorString = "Downloaded " + _remoteSize + " bytes " + downloaded;
            _downloadTime += System.currentTimeMillis() - _start;
            return _totalBytes;

        } catch (final Exception e) {
            s_logger.warn("Unable to download " + _downloadUrl, e);
            _status = TemplateDownloader.Status.UNRECOVERABLE_ERROR;
            _errorString = e.getMessage();
            return 0;
        } finally {
            sshConnection.close();
            if (_status == Status.UNRECOVERABLE_ERROR && file.exists()) {
                file.delete();
            }
            if (callback != null) {
                callback.downloadComplete(_status);
            }
        }
    }

    @Override
    public int getDownloadPercent() {
        if (_status == Status.DOWNLOAD_FINISHED) {
            return 100;
        } else if (_status == Status.IN_PROGRESS) {
            return 50;
        } else {
            return 0;
        }
    }
}
