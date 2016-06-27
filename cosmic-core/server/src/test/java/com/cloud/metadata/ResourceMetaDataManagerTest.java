package com.cloud.metadata;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

import com.cloud.exception.ResourceAllocationException;
import com.cloud.server.ResourceTag;
import com.cloud.server.TaggedResourceService;
import com.cloud.storage.dao.VolumeDetailsDao;
import com.cloud.vm.dao.NicDetailsDao;

import javax.naming.ConfigurationException;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class ResourceMetaDataManagerTest {

    @Spy
    ResourceMetaDataManagerImpl _resourceMetaDataMgr = new ResourceMetaDataManagerImpl();
    @Mock
    VolumeDetailsDao _volumeDetailDao;
    @Mock
    NicDetailsDao _nicDetailDao;
    @Mock
    TaggedResourceService _taggedResourceMgr;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        try {
            _resourceMetaDataMgr.configure(null, null);
        } catch (final ConfigurationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        _resourceMetaDataMgr._volumeDetailDao = _volumeDetailDao;
        _resourceMetaDataMgr._taggedResourceMgr = _taggedResourceMgr;
        _resourceMetaDataMgr._nicDetailDao = _nicDetailDao;
    }

    // Test removing details
    //@Test
    public void testResourceDetails() throws ResourceAllocationException {

        //when(_resourceMetaDataMgr.getResourceId(anyString(), eq(ResourceTag.TaggedResourceType.Volume))).thenReturn(1L);
        doReturn(1L).when(_taggedResourceMgr).getResourceId(anyString(), eq(ResourceTag.ResourceObjectType.Volume));
        //           _volumeDetailDao.removeDetails(id, key);

        doNothing().when(_volumeDetailDao).removeDetail(anyLong(), anyString());
        doNothing().when(_nicDetailDao).removeDetail(anyLong(), anyString());
        _resourceMetaDataMgr.deleteResourceMetaData(anyString(), eq(ResourceTag.ResourceObjectType.Volume), anyString());
    }

    // Test adding details
    public void testAddResourceDetails() throws ResourceAllocationException {

        doReturn(1L).when(_taggedResourceMgr).getResourceId("1", ResourceTag.ResourceObjectType.Volume);
        //           _volumeDetailDao.removeDetails(id, key);

        doNothing().when(_volumeDetailDao).removeDetail(anyLong(), anyString());
        doNothing().when(_nicDetailDao).removeDetail(anyLong(), anyString());
        final Map<String, String> map = new HashedMap();
        map.put("key", "value");
        _resourceMetaDataMgr.addResourceMetaData("1", ResourceTag.ResourceObjectType.Volume, map, true);
    }
}
