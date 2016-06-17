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

package com.cloud.utils.component;

import com.cloud.exception.InvalidParameterValueException;
import com.cloud.utils.UriUtils;

import java.net.URI;
import java.net.URISyntaxException;

public class ManagerBase extends ComponentLifecycleBase implements ComponentMethodInterceptable {
    public ManagerBase() {
        super();
        // set default run level for manager components
        setRunLevel(ComponentLifecycle.RUN_LEVEL_COMPONENT_BOOTSTRAP);
    }

    protected URI generateUri(final String url) {
        final URI uri;
        try {
            uri = new URI(UriUtils.encodeURIComponent(url));
            if (uri.getScheme() == null) {
                throw new InvalidParameterValueException("uri.scheme is null " + url + ", add nfs:// (or cifs://) as a prefix");
            } else if (uri.getScheme().equalsIgnoreCase("nfs")) {
                if (uri.getHost() == null || uri.getHost().equalsIgnoreCase("") || uri.getPath() == null || uri.getPath().equalsIgnoreCase("")) {
                    throw new InvalidParameterValueException("Your host and/or path is wrong.  Make sure it's of the format nfs://hostname/path");
                }
            } else if (uri.getScheme().equalsIgnoreCase("cifs")) {
                // Don't validate against a URI encoded URI.
                final URI cifsUri = new URI(url);
                final String warnMsg = UriUtils.getCifsUriParametersProblems(cifsUri);
                if (warnMsg != null) {
                    throw new InvalidParameterValueException(warnMsg);
                }
            }
        } catch (final URISyntaxException e) {
            throw new InvalidParameterValueException(url + " is not a valid uri");
        }
        return uri;
    }
}
