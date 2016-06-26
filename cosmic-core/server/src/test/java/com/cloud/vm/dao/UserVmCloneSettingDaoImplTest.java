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
package com.cloud.vm.dao;

import com.cloud.vm.UserVmCloneSettingVO;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/CloneSettingDaoTestContext.xml")
public class UserVmCloneSettingDaoImplTest extends TestCase {
    @Inject
    UserVmCloneSettingDaoImpl _vmcsdao;

    @Test
    public void testPersist() {

        final Long vmId = 2222l;
        final String[] arr = {"full", "linked"};
        for (final String cloneType : arr) {
            _vmcsdao.expunge(vmId);
            makeEntry(vmId, cloneType);
        }
    }

    public void makeEntry(final Long vmId, final String cloneType) {
        UserVmCloneSettingVO vo = new UserVmCloneSettingVO(vmId, cloneType);
        _vmcsdao.persist(vo);
        vo = _vmcsdao.findById(vmId);
        assert (vo.getCloneType().equalsIgnoreCase(cloneType)) : "Unexpected Clone Type retrieved from table! Retrieved: " + vo.getCloneType() + " while expected was: " +
                cloneType;

        // Next test whether the record is retrieved by clone type.
        List<UserVmCloneSettingVO> voList = new ArrayList<>();
        voList = _vmcsdao.listByCloneType(cloneType);
        assert (voList != null && !voList.isEmpty()) : "Failed to retrieve any record of VMs by clone type!";

        // If a vo list is indeed retrieved, also check whether the vm id retrieved matches what we put in there.
        assert (voList.get(0).getVmId() == vmId) : "Retrieved vmId " + voList.get(0).getVmId() + " does not match input vmId: " + vmId;
    }
}
