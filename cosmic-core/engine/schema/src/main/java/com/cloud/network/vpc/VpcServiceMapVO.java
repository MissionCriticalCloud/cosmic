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
package com.cloud.network.vpc;

import com.cloud.network.Network.Provider;
import com.cloud.network.Network.Service;
import com.cloud.utils.db.GenericDao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "vpc_service_map")
public class VpcServiceMapVO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    long id;

    @Column(name = "vpc_id")
    long vpcId;

    @Column(name = "service")
    String service;

    @Column(name = "provider")
    String provider;

    @Column(name = GenericDao.CREATED_COLUMN)
    Date created;

    public VpcServiceMapVO() {
    }

    public VpcServiceMapVO(final long vpcId, final Service service, final Provider provider) {
        this.vpcId = vpcId;
        this.service = service.getName();
        this.provider = provider.getName();
    }

    public long getId() {
        return id;
    }

    public long getVpcId() {
        return vpcId;
    }

    public String getService() {
        return service;
    }

    public String getProvider() {
        return provider;
    }

    public Date getCreated() {
        return created;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder("[VPC Service[");
        return buf.append(vpcId).append("-").append(service).append("-").append(provider).append("]").toString();
    }
}
