package org.apache.cloudstack.utils.qemu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.cloud.utils.script.Script;

import org.apache.cloudstack.utils.qemu.QemuImg.PhysicalDiskFormat;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class QemuImgTest {

  @Test
  public void testCreateAndInfo() throws QemuImgException {
    final String filename = "/tmp/" + UUID.randomUUID() + ".qcow2";

    /* 10TB virtual_size */
    final long size = 10995116277760l;
    final QemuImgFile file = new QemuImgFile(filename, size, PhysicalDiskFormat.QCOW2);

    final QemuImg qemu = new QemuImg(0);
    qemu.create(file);
    final Map<String, String> info = qemu.info(file);

    if (info == null) {
      fail("We didn't get any information back from qemu-img");
    }

    final Long infoSize = Long.parseLong(info.get(new String("virtual_size")));
    assertEquals(Long.valueOf(size), Long.valueOf(infoSize));

    final String infoPath = info.get(new String("image"));
    assertEquals(filename, infoPath);

    final File f = new File(filename);
    f.delete();

  }

  @Test
  public void testCreateAndInfoWithOptions() throws QemuImgException {
    final String filename = "/tmp/" + UUID.randomUUID() + ".qcow2";

    /* 10TB virtual_size */
    final long size = 10995116277760l;
    final QemuImgFile file = new QemuImgFile(filename, size, PhysicalDiskFormat.QCOW2);
    final String clusterSize = "131072";
    final Map<String, String> options = new HashMap<String, String>();

    options.put("cluster_size", clusterSize);

    final QemuImg qemu = new QemuImg(0);
    qemu.create(file, options);
    final Map<String, String> info = qemu.info(file);

    final Long infoSize = Long.parseLong(info.get(new String("virtual_size")));
    assertEquals(Long.valueOf(size), Long.valueOf(infoSize));

    final String infoPath = info.get(new String("image"));
    assertEquals(filename, infoPath);

    final String infoClusterSize = info.get(new String("cluster_size"));
    assertEquals(clusterSize, infoClusterSize);

    final File f = new File(filename);
    f.delete();

  }

  @Test
  public void testCreateSparseVolume() throws QemuImgException {
    final String filename = "/tmp/" + UUID.randomUUID() + ".qcow2";

    /* 10TB virtual_size */
    final long size = 10995116277760l;
    final QemuImgFile file = new QemuImgFile(filename, size, PhysicalDiskFormat.QCOW2);
    final String preallocation = "metadata";
    final Map<String, String> options = new HashMap<String, String>();

    options.put("preallocation", preallocation);

    final QemuImg qemu = new QemuImg(0);
    qemu.create(file, options);

    final String allocatedSize = Script.runSimpleBashScript(String.format("ls -alhs %s | awk '{print $1}'", file));
    final String declaredSize = Script.runSimpleBashScript(String.format("ls -alhs %s | awk '{print $6}'", file));

    assertFalse(allocatedSize.equals(declaredSize));

    final File f = new File(filename);
    f.delete();

  }

  @Test
  public void testCreateAndResize() throws QemuImgException {
    final String filename = "/tmp/" + UUID.randomUUID() + ".qcow2";

    final long startSize = 20480;
    final long endSize = 40960;
    final QemuImgFile file = new QemuImgFile(filename, startSize, PhysicalDiskFormat.QCOW2);

    try {
      final QemuImg qemu = new QemuImg(0);
      qemu.create(file);
      qemu.resize(file, endSize);
      final Map<String, String> info = qemu.info(file);

      if (info == null) {
        fail("We didn't get any information back from qemu-img");
      }

      final Long infoSize = Long.parseLong(info.get(new String("virtual_size")));
      assertEquals(Long.valueOf(endSize), Long.valueOf(infoSize));
    } catch (final QemuImgException e) {
      fail(e.getMessage());
    }

    final File f = new File(filename);
    f.delete();

  }

  @Test
  public void testCreateAndResizeDeltaPositive() throws QemuImgException {
    final String filename = "/tmp/" + UUID.randomUUID() + ".qcow2";

    final long startSize = 20480;
    final long increment = 20480;
    final QemuImgFile file = new QemuImgFile(filename, startSize, PhysicalDiskFormat.RAW);

    try {
      final QemuImg qemu = new QemuImg(0);
      qemu.create(file);
      qemu.resize(file, increment, true);
      final Map<String, String> info = qemu.info(file);

      if (info == null) {
        fail("We didn't get any information back from qemu-img");
      }

      final Long infoSize = Long.parseLong(info.get(new String("virtual_size")));
      assertEquals(Long.valueOf(startSize + increment), Long.valueOf(infoSize));
    } catch (final QemuImgException e) {
      fail(e.getMessage());
    }

    final File f = new File(filename);
    f.delete();
  }

  @Test
  public void testCreateAndResizeDeltaNegative() throws QemuImgException {
    final String filename = "/tmp/" + UUID.randomUUID() + ".qcow2";

    final long startSize = 81920;
    final long increment = -40960;
    final QemuImgFile file = new QemuImgFile(filename, startSize, PhysicalDiskFormat.RAW);

    try {
      final QemuImg qemu = new QemuImg(0);
      qemu.create(file);
      qemu.resize(file, increment, true);
      final Map<String, String> info = qemu.info(file);

      if (info == null) {
        fail("We didn't get any information back from qemu-img");
      }

      final Long infoSize = Long.parseLong(info.get(new String("virtual_size")));
      assertEquals(Long.valueOf(startSize + increment), Long.valueOf(infoSize));
    } catch (final QemuImgException e) {
      fail(e.getMessage());
    }

    final File f = new File(filename);
    f.delete();
  }

  @Test(expected = QemuImgException.class)
  public void testCreateAndResizeFail() throws QemuImgException {
    final String filename = "/tmp/" + UUID.randomUUID() + ".qcow2";

    final long startSize = 20480;

    /* Negative new size, expect failure */
    final long endSize = -1;
    final QemuImgFile file = new QemuImgFile(filename, startSize, PhysicalDiskFormat.QCOW2);

    final QemuImg qemu = new QemuImg(0);
    try {
      qemu.create(file);
      qemu.resize(file, endSize);
    } finally {
      final File f = new File(filename);
      f.delete();
    }
  }

  @Test(expected = QemuImgException.class)
  public void testCreateAndResizeZero() throws QemuImgException {
    final String filename = "/tmp/" + UUID.randomUUID() + ".qcow2";

    final QemuImgFile file = new QemuImgFile(filename, 20480, PhysicalDiskFormat.QCOW2);

    final QemuImg qemu = new QemuImg(0);
    qemu.create(file);
    qemu.resize(file, 0);

    final File f = new File(filename);
    f.delete();

  }

  @Test
  public void testCreateWithBackingFile() throws QemuImgException {
    final String firstFileName = "/tmp/" + UUID.randomUUID() + ".qcow2";
    final String secondFileName = "/tmp/" + UUID.randomUUID() + ".qcow2";

    final QemuImgFile firstFile = new QemuImgFile(firstFileName, 20480, PhysicalDiskFormat.QCOW2);
    final QemuImgFile secondFile = new QemuImgFile(secondFileName, PhysicalDiskFormat.QCOW2);

    final QemuImg qemu = new QemuImg(0);
    qemu.create(firstFile);
    qemu.create(secondFile, firstFile);

    final Map<String, String> info = qemu.info(secondFile);
    if (info == null) {
      fail("We didn't get any information back from qemu-img");
    }

    final String backingFile = info.get(new String("backing_file"));
    if (backingFile == null) {
      fail("The second file does not have a property backing_file! Create failed?");
    }
  }

  @Test
  public void testConvertBasic() throws QemuImgException {
    final long srcSize = 20480;
    final String srcFileName = "/tmp/" + UUID.randomUUID() + ".qcow2";
    final String destFileName = "/tmp/" + UUID.randomUUID() + ".qcow2";

    final QemuImgFile srcFile = new QemuImgFile(srcFileName, srcSize);
    final QemuImgFile destFile = new QemuImgFile(destFileName);

    final QemuImg qemu = new QemuImg(0);
    qemu.create(srcFile);
    qemu.convert(srcFile, destFile);
    final Map<String, String> info = qemu.info(destFile);
    if (info == null) {
      fail("We didn't get any information back from qemu-img");
    }

    final File sf = new File(srcFileName);
    sf.delete();

    final File df = new File(destFileName);
    df.delete();

  }

  @Test
  public void testConvertAdvanced() throws QemuImgException {
    final long srcSize = 4019200;
    final String srcFileName = "/tmp/" + UUID.randomUUID() + ".qcow2";
    final String destFileName = "/tmp/" + UUID.randomUUID() + ".qcow2";
    final PhysicalDiskFormat srcFormat = PhysicalDiskFormat.RAW;
    final PhysicalDiskFormat destFormat = PhysicalDiskFormat.QCOW2;

    final QemuImgFile srcFile = new QemuImgFile(srcFileName, srcSize, srcFormat);
    final QemuImgFile destFile = new QemuImgFile(destFileName, destFormat);

    final QemuImg qemu = new QemuImg(0);
    qemu.create(srcFile);
    qemu.convert(srcFile, destFile);

    final Map<String, String> info = qemu.info(destFile);

    final PhysicalDiskFormat infoFormat = PhysicalDiskFormat.valueOf(info.get(new String("format")).toUpperCase());
    assertEquals(destFormat, infoFormat);

    final Long infoSize = Long.parseLong(info.get(new String("virtual_size")));
    assertEquals(Long.valueOf(srcSize), Long.valueOf(infoSize));

    final File sf = new File(srcFileName);
    sf.delete();

    final File df = new File(destFileName);
    df.delete();

  }
}