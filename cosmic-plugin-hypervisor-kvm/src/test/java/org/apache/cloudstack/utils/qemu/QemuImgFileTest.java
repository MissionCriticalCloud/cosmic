package org.apache.cloudstack.utils.qemu;

import static org.junit.Assert.assertEquals;

import org.apache.cloudstack.utils.qemu.QemuImg.PhysicalDiskFormat;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class QemuImgFileTest {
  @Test
  public void testFileNameAtContructor() {
    final String filename = "/tmp/test-image.qcow2";
    final QemuImgFile file = new QemuImgFile(filename);
    assertEquals(file.getFileName(), filename);
  }

  @Test
  public void testFileNameAndSizeAtContructor() {
    final long size = 1024;
    final String filename = "/tmp/test-image.qcow2";
    final QemuImgFile file = new QemuImgFile(filename, size);
    assertEquals(file.getFileName(), filename);
    assertEquals(file.getSize(), size);
  }

  @Test
  public void testFileNameAndSizeAndFormatAtContructor() {
    final PhysicalDiskFormat format = PhysicalDiskFormat.RAW;
    final long size = 1024;
    final String filename = "/tmp/test-image.qcow2";
    final QemuImgFile file = new QemuImgFile(filename, size, format);
    assertEquals(file.getFileName(), filename);
    assertEquals(file.getSize(), size);
    assertEquals(file.getFormat(), format);
  }

  @Test
  public void testFileNameAndFormatAtContructor() {
    final PhysicalDiskFormat format = PhysicalDiskFormat.RAW;
    final String filename = "/tmp/test-image.qcow2";
    final QemuImgFile file = new QemuImgFile(filename, format);
    assertEquals(file.getFileName(), filename);
    assertEquals(file.getFormat(), format);
  }
}