package com.cloud.api.query.dao;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import com.cloud.api.query.vo.SecurityGroupJoinVO;
import com.cloud.network.security.SecurityGroupVMMapVO;
import com.cloud.network.security.dao.SecurityGroupVMMapDao;
import com.cloud.server.ResourceTag.ResourceObjectType;
import com.cloud.user.Account;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.dao.UserVmDao;
import org.apache.cloudstack.api.response.SecurityGroupResponse;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SecurityGroupJoinDaoImplTest extends TestCase {

    // Random generated UUIDs
    private final String uuidOne = "463e022a-249d-4212-bdf4-726bc9047aa7";
    private final String uuidTwo = "d8714c5f-766f-4b14-bdf4-17571042b9c5";
    // Mock private variables.
    @Mock(name = "_resourceTagJoinDao")
    private ResourceTagJoinDao _resourceTagJoinDao;
    @Mock(name = "_securityGroupVMMapDao")
    private SecurityGroupVMMapDao _securityGroupVMMapDao;
    @Mock(name = "_userVmDao")
    private UserVmDao _userVmDao;
    // Inject mocks in class to be tested.
    @InjectMocks
    private SecurityGroupJoinDaoImpl _securityGroupJoinDaoImpl;
    // Mock a caller and a SecurityGroupJoinVO
    @Mock
    private Account caller;
    @Mock
    private SecurityGroupJoinVO vsg;
    // Mock securitygroups
    @Mock
    private SecurityGroupVMMapVO securityGroupVMMapVOone;
    @Mock
    private SecurityGroupVMMapVO securityGroupVMMapVOtwo;
    // Mock 2 UserVmVOs
    @Mock
    private UserVmVO userVmVOone;
    @Mock
    private UserVmVO userVmVOtwo;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        // Security group without vms associated.
        final List<SecurityGroupVMMapVO> securityGroupVmMap_empty = new ArrayList<>();

        // Security group with one vm associated.
        final List<SecurityGroupVMMapVO> securityGroupVmMap_one = new ArrayList<>();
        securityGroupVmMap_one.add(securityGroupVMMapVOone);

        // Security group with two or many vms associated
        final List<SecurityGroupVMMapVO> securityGroupVmMap_two = new ArrayList<>();
        securityGroupVmMap_two.add(securityGroupVMMapVOone);
        securityGroupVmMap_two.add(securityGroupVMMapVOtwo);

        // Mock the resource tags to return an empty list.
        when(_resourceTagJoinDao.listBy(anyString(), any(ResourceObjectType.class))).thenReturn(new ArrayList<>());

        // Mock the listBySecurityGroup method to return a specified list when being called.
        when(_securityGroupVMMapDao.listBySecurityGroup(1L)).thenReturn(securityGroupVmMap_empty);
        when(_securityGroupVMMapDao.listBySecurityGroup(2L)).thenReturn(securityGroupVmMap_one);
        when(_securityGroupVMMapDao.listBySecurityGroup(3L)).thenReturn(securityGroupVmMap_two);

        // Mock the securityGroupVMMapVOs to return a specified instance id.
        when(securityGroupVMMapVOone.getInstanceId()).thenReturn(1L);
        when(securityGroupVMMapVOtwo.getInstanceId()).thenReturn(2L);

        // Mock _userVmDao to return a non null instance of UserVmVO.
        when(_userVmDao.findById(1L)).thenReturn(userVmVOone);
        when(_userVmDao.findById(2L)).thenReturn(userVmVOtwo);

        // Mock _userVmDao to return a non null instance of UserVmVO.
        when(userVmVOone.getUuid()).thenReturn(uuidOne);
        when(userVmVOtwo.getUuid()).thenReturn(uuidTwo);
    }

    @Test
    public void virtualMachineCountEmptyTest() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

        when(vsg.getId()).thenReturn(1L);

        final SecurityGroupResponse securityGroupResponse = _securityGroupJoinDaoImpl.newSecurityGroupResponse(vsg, caller);

        final Field virtualMachineCount = securityGroupResponse.getClass().getDeclaredField("virtualMachineCount");
        virtualMachineCount.setAccessible(true);
        assertEquals(0, ((Integer) virtualMachineCount.get(securityGroupResponse)).intValue());
    }

    @Test
    public void virtualMachineCountOneTest() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

        when(vsg.getId()).thenReturn(2L);

        final SecurityGroupResponse securityGroupResponse = _securityGroupJoinDaoImpl.newSecurityGroupResponse(vsg, caller);

        final Field virtualMachineCount = securityGroupResponse.getClass().getDeclaredField("virtualMachineCount");
        virtualMachineCount.setAccessible(true);
        assertEquals(1, ((Integer) virtualMachineCount.get(securityGroupResponse)).intValue());
    }

    @Test
    public void virtualMachineCountTwoTest() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

        when(vsg.getId()).thenReturn(3L);

        final SecurityGroupResponse securityGroupResponse = _securityGroupJoinDaoImpl.newSecurityGroupResponse(vsg, caller);

        final Field virtualMachineCount = securityGroupResponse.getClass().getDeclaredField("virtualMachineCount");
        virtualMachineCount.setAccessible(true);
        assertEquals(2, ((Integer) virtualMachineCount.get(securityGroupResponse)).intValue());
    }

    @Test
    public void virtualMachineIDsEmptyTest() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

        when(vsg.getId()).thenReturn(1L);

        final SecurityGroupResponse securityGroupResponse = _securityGroupJoinDaoImpl.newSecurityGroupResponse(vsg, caller);

        final Field fieldVirtualMachineIds = securityGroupResponse.getClass().getDeclaredField("virtualMachineIds");
        fieldVirtualMachineIds.setAccessible(true);

        final Set<String> virtualMachineIds = (Set<String>) fieldVirtualMachineIds.get(securityGroupResponse);

        assertEquals(0, virtualMachineIds.size());
    }

    @Test
    public void virtualMachineIDsOneTest() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

        when(vsg.getId()).thenReturn(2L);

        final SecurityGroupResponse securityGroupResponse = _securityGroupJoinDaoImpl.newSecurityGroupResponse(vsg, caller);

        final Field fieldVirtualMachineIds = securityGroupResponse.getClass().getDeclaredField("virtualMachineIds");
        fieldVirtualMachineIds.setAccessible(true);

        final Set<String> virtualMachineIds = (Set<String>) fieldVirtualMachineIds.get(securityGroupResponse);

        assertEquals(1, virtualMachineIds.size());
        assertTrue(virtualMachineIds.contains(uuidOne));
    }

    @Test
    public void virtualMachineIDsTwoTest() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

        when(vsg.getId()).thenReturn(3L);

        final SecurityGroupResponse securityGroupResponse = _securityGroupJoinDaoImpl.newSecurityGroupResponse(vsg, caller);

        final Field fieldVirtualMachineIds = securityGroupResponse.getClass().getDeclaredField("virtualMachineIds");
        fieldVirtualMachineIds.setAccessible(true);

        final Set<String> virtualMachineIds = (Set<String>) fieldVirtualMachineIds.get(securityGroupResponse);

        assertEquals(2, virtualMachineIds.size());
        assertTrue(virtualMachineIds.contains(uuidOne));
        assertTrue(virtualMachineIds.contains(uuidTwo));
    }
}
