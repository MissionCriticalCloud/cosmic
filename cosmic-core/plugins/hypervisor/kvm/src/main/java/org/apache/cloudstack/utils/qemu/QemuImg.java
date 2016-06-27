package org.apache.cloudstack.utils.qemu;

import com.cloud.storage.Storage;
import com.cloud.utils.script.OutputInterpreter;
import com.cloud.utils.script.Script;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class QemuImg {

    /* The qemu-img binary. We expect this to be in $PATH */
    public String qemuImgPath = "qemu-img";

    private int timeout;

    public QemuImg(final int timeout) {
        this.timeout = timeout;
    }

    public QemuImg(final String qemuImgPath) {
        this.qemuImgPath = qemuImgPath;
    }

    public void setTimeout(final int timeout) {
        this.timeout = timeout;
    }

    /* Perform a consistency check on the disk image */
    public void check(final QemuImgFile file) {

    }

    public void create(final QemuImgFile file) throws QemuImgException {
        this.create(file, null, null);
    }

  /* These are all methods supported by the qemu-img tool */

    public void create(final QemuImgFile file, final QemuImgFile backingFile, final Map<String, String> options)
            throws QemuImgException {
        final Script s = new Script(qemuImgPath, timeout);
        s.add("create");

        if (options != null && !options.isEmpty()) {
            s.add("-o");
            final StringBuilder optionsStr = new StringBuilder();
            final Iterator<Map.Entry<String, String>> optionsIter = options.entrySet().iterator();
            while (optionsIter.hasNext()) {
                final Map.Entry option = optionsIter.next();
                optionsStr.append(option.getKey()).append('=').append(option.getValue());
                if (optionsIter.hasNext()) {
                    // Add "," only if there are more options
                    optionsStr.append(',');
                }
            }
            s.add(optionsStr.toString());
        }

    /*
     * -b for a backing file does not show up in the docs, but it works. Shouldn't this be -o backing_file=filename
     * instead?
     */
        s.add("-f");
        if (backingFile != null) {
            s.add(backingFile.getFormat().toString());
            s.add("-b");
            s.add(backingFile.getFileName());
        } else {
            s.add(file.getFormat().toString());
        }

        s.add(file.getFileName());
        if (file.getSize() != 0L) {
            s.add(Long.toString(file.getSize()));
        } else if (backingFile == null) {
            throw new QemuImgException("No size was passed, and no backing file was passed");
        }

        final String result = s.execute();
        if (result != null) {
            throw new QemuImgException(result);
        }
    }

    public void create(final QemuImgFile file, final QemuImgFile backingFile) throws QemuImgException {
        this.create(file, backingFile, null);
    }

    public void create(final QemuImgFile file, final Map<String, String> options) throws QemuImgException {
        this.create(file, null, options);
    }

    public void convert(final QemuImgFile srcFile, final QemuImgFile destFile) throws QemuImgException {
        this.convert(srcFile, destFile, null);
    }

    public void convert(final QemuImgFile srcFile, final QemuImgFile destFile, final Map<String, String> options)
            throws QemuImgException {
        final Script script = new Script(qemuImgPath, timeout);
        script.add("convert");
        // autodetect source format. Sometime int he future we may teach KVMPhysicalDisk about more formats, then we can
        // explicitly pass them if necessary
        // s.add("-f");
        // s.add(srcFile.getFormat().toString());
        script.add("-O");
        script.add(destFile.getFormat().toString());

        if (options != null && !options.isEmpty()) {
            script.add("-o");
            final StringBuffer optionsBuffer = new StringBuffer();
            for (final Map.Entry<String, String> option : options.entrySet()) {
                optionsBuffer.append(option.getKey()).append('=').append(option.getValue()).append(',');
            }
            String optionsStr = optionsBuffer.toString();
            optionsStr = optionsStr.replaceAll(",$", "");
            script.add(optionsStr);
        }

        script.add(srcFile.getFileName());
        script.add(destFile.getFileName());

        final String result = script.execute();
        if (result != null) {
            throw new QemuImgException(result);
        }

        if (srcFile.getSize() < destFile.getSize()) {
            this.resize(destFile, destFile.getSize());
        }
    }

    public void resize(final QemuImgFile file, final long size) throws QemuImgException {
        this.resize(file, size, false);
    }

    public void resize(final QemuImgFile file, final long size, final boolean delta) throws QemuImgException {
        String newSize = null;

        if (size == 0) {
            throw new QemuImgException("size should never be exactly zero");
        }

        if (delta) {
            if (size > 0) {
                newSize = "+" + Long.toString(size);
            } else {
                newSize = Long.toString(size);
            }
        } else {
            if (size <= 0) {
                throw new QemuImgException("size should not be negative if 'delta' is false!");
            }
            newSize = Long.toString(size);
        }

        final Script s = new Script(qemuImgPath);
        s.add("resize");
        s.add(file.getFileName());
        s.add(newSize);
        s.execute();
    }

    public void commit(final QemuImgFile file) throws QemuImgException {

    }

    public Map<String, String> info(final QemuImgFile file) throws QemuImgException {
        final Script s = new Script(qemuImgPath);
        s.add("info");
        s.add(file.getFileName());
        final OutputInterpreter.AllLinesParser parser = new OutputInterpreter.AllLinesParser();
        final String result = s.execute(parser);
        if (result != null) {
            throw new QemuImgException(result);
        }

        final HashMap<String, String> info = new HashMap<>();
        final String[] outputBuffer = parser.getLines().trim().split("\n");
        for (final String element : outputBuffer) {
            final String[] lineBuffer = element.split(":", 2);
            if (lineBuffer.length == 2) {
                final String key = lineBuffer[0].trim().replace(" ", "_");
                String value = null;

                if (key.equals("virtual_size")) {
                    value = lineBuffer[1].trim().replaceAll("^.*\\(([0-9]+).*$", "$1");
                } else {
                    value = lineBuffer[1].trim();
                }

                info.put(key, value);
            }
        }
        return info;
    }

    /* List, apply, create or delete snapshots in image */
    public void snapshot() throws QemuImgException {

    }

    /* Changes the backing file of an image */
    public void rebase() throws QemuImgException {

    }

    /* Shouldn't we have KVMPhysicalDisk and LibvirtVMDef read this? */
    public static enum PhysicalDiskFormat {
        RAW("raw"), QCOW2("qcow2"), VMDK("vmdk"), FILE("file"), RBD("rbd"), SHEEPDOG("sheepdog"), HTTP("http"), HTTPS(
                "https"), TAR("tar"), DIR("dir");
        String format;

        private PhysicalDiskFormat(final String format) {
            this.format = format;
        }

        @Override
        public String toString() {
            return format;
        }
    }

    public static enum PreallocationType {
        Off("off"), Metadata("metadata"), Full("full");

        private final String preallocationType;

        private PreallocationType(final String preallocationType) {
            this.preallocationType = preallocationType;
        }

        public static PreallocationType getPreallocationType(final Storage.ProvisioningType provisioningType) {
            switch (provisioningType) {
                case THIN:
                    return PreallocationType.Off;
                case SPARSE:
                    return PreallocationType.Metadata;
                case FAT:
                    return PreallocationType.Full;
                default:
                    throw new NotImplementedException();
            }
        }

        @Override
        public String toString() {
            return preallocationType;
        }

    }
}
