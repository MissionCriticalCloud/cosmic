package org.apache.cloudstack.engine.subsystem.api.storage.type;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

public class VolumeTypeHelperTest {

    private VolumeTypeHelper helper;

    @Before
    public void setu() {
        helper = new VolumeTypeHelper();

        final List<VolumeType> types = new ArrayList<>();
        types.add(new BaseImage());
        types.add(new DataDisk());
        types.add(new Iso());
        types.add(new Unknown());
        types.add(new RootDisk());
        types.add(new VolumeTypeBase());

        helper.setTypes(types);
    }

    @Test
    public void testGetTypeBaseImage() throws Exception {
        final VolumeType type = helper.getType("BaseImage");

        Assert.assertTrue(type instanceof BaseImage);
    }

    @Test
    public void testGetTypeDataDisk() throws Exception {
        final VolumeType type = helper.getType("DataDisk");

        Assert.assertTrue(type instanceof DataDisk);
    }

    @Test
    public void testGetTypeIso() throws Exception {
        final VolumeType type = helper.getType("Iso");

        Assert.assertTrue(type instanceof Iso);
    }

    @Test
    public void testGetTypeUnknown() throws Exception {
        final VolumeType type = helper.getType("Unknown");

        Assert.assertTrue(type instanceof Unknown);
    }

    @Test
    public void testGetTypeRootDisk() throws Exception {
        final VolumeType type = helper.getType("RootDisk");

        Assert.assertTrue(type instanceof RootDisk);
    }

    @Test
    public void testGetTypeVolumeTypeBase() throws Exception {
        final VolumeType type = helper.getType("VolumeTypeBase");

        Assert.assertTrue(type instanceof VolumeTypeBase);
    }

    @Test
    public void testGetTypeVolumeString() throws Exception {
        final VolumeType type = helper.getType("String");

        Assert.assertTrue(type instanceof Unknown);
    }

    @Test(expected = NullPointerException.class)
    public void testGetTypeVolumeNull() throws Exception {
        helper.getType(null);
    }
}
