//

//

package com.cloud.storage;

import javax.ejb.Local;
import javax.naming.ConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Local(value = StorageLayer.class)
public class JavaStorageLayer implements StorageLayer {

    String _name;
    boolean _makeWorldWriteable = true;

    public JavaStorageLayer(final boolean makeWorldWriteable) {
        this();
        _makeWorldWriteable = makeWorldWriteable;
    }

    public JavaStorageLayer() {
        super();
    }

    @Override
    public long getSize(final String path) {
        final File file = new File(path);
        return file.length();
    }

    @Override
    public File createUniqDir() {
        final String dirName = System.getProperty("java.io.tmpdir");
        if (dirName != null) {
            final File dir = new File(dirName);
            if (dir.exists()) {
                final String uniqDirName = dir.getAbsolutePath() + File.separator + UUID.randomUUID().toString();
                if (mkdir(uniqDirName)) {
                    return new File(uniqDirName);
                }
            }
        }
        return null;
    }

    @Override
    public boolean isDirectory(final String path) {
        final File file = new File(path);
        return file.isDirectory();
    }

    @Override
    public boolean isFile(final String path) {
        final File file = new File(path);
        return file.isFile();
    }

    @Override
    public boolean mkdir(final String path) {
        synchronized (path.intern()) {
            final File file = new File(path);

            if (file.exists()) {
                return file.isDirectory();
            }
            if (_makeWorldWriteable) {
                return (file.mkdirs() && setWorldReadableAndWriteable(file));
            } else {
                return file.mkdirs();
            }
        }
    }

    @Override
    public boolean mkdirs(final String path) {
        synchronized (path.intern()) {
            File dir = new File(path);

            if (dir.exists()) {
                return dir.isDirectory();
            }

            boolean success = true;
            final List<String> dirPaths = listDirPaths(path);
            for (final String dirPath : dirPaths) {
                dir = new File(dirPath);
                if (!dir.exists()) {
                    success = dir.mkdir();
                    if (_makeWorldWriteable) {
                        success = success && setWorldReadableAndWriteable(dir);
                    }
                }
            }

            return success;
        }
    }

    @Override
    public boolean exists(final String path) {
        synchronized (path.intern()) {
            final File file = new File(path);
            return file.exists();
        }
    }

    @Override
    public String[] listFiles(final String path) {
        final File file = new File(path);
        final File[] files = file.listFiles();
        if (files == null) {
            return new String[0];
        }
        final String[] paths = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            paths[i] = files[i].getAbsolutePath();
        }
        return paths;
    }

    @Override
    public long getTotalSpace(final String path) {
        final File file = new File(path);
        return file.getTotalSpace();
    }

    @Override
    public long getUsedSpace(final String path) {
        final File file = new File(path);
        return file.getTotalSpace() - file.getFreeSpace();
    }

    @Override
    public long getUsableSpace(final String path) {
        final File file = new File(path);
        return file.getUsableSpace();
    }

    @Override
    public boolean delete(final String path) {
        synchronized (path.intern()) {
            final File file = new File(path);
            return file.delete();
        }
    }

    @Override
    public boolean create(final String path, final String filename) throws IOException {
        synchronized (path.intern()) {
            final String newFile = path + File.separator + filename;
            final File file = new File(newFile);
            if (file.exists()) {
                return true;
            }

            return file.createNewFile();
        }
    }

    @Override
    public boolean cleanup(final String path, final String rootPath) throws IOException {
        assert path.startsWith(rootPath) : path + " does not start with " + rootPath;

        synchronized (path) {
            File file = new File(path);
            if (!file.delete()) {
                return false;
            }
            int index = -1;
            final long rootLength = rootPath.length();
            while ((index = path.lastIndexOf(File.separator)) != -1 && path.length() > rootLength) {
                file = new File(path.substring(0, index));
                final String[] children = file.list();
                if (children != null && children.length > 0) {
                    break;
                }
                if (!file.delete()) {
                    throw new IOException("Unable to delete " + file.getAbsolutePath());
                }
            }
            return true;
        }
    }

    @Override
    public File getFile(final String path) {
        return new File(path);
    }

    @Override
    public boolean setWorldReadableAndWriteable(final File file) {
        return (file.setReadable(true, false) && file.setWritable(true, false));
    }

    @Override
    public boolean deleteDir(final String dir) {
        final File Dir = new File(dir);
        if (!Dir.isDirectory()) {
            return false;
        }

        synchronized (dir.intern()) {
            final File[] files = Dir.listFiles();
            for (final File file : files) {
                if (!file.delete()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public List<String> listMountPointsByMsHost(final String path, final long msHostId) {
        final List<String> mountPaths = new ArrayList<>();
        final File[] files = new File(path).listFiles();
        if (files == null) {
            return mountPaths;
        }
        for (final File file : files) {
            if (file.getName().startsWith(String.valueOf(msHostId) + ".")) {
                mountPaths.add(file.getAbsolutePath());
            }
        }
        return mountPaths;
    }

    private List<String> listDirPaths(final String path) {
        final String[] dirNames = path.split("/");
        final List<String> dirPaths = new ArrayList<>();

        String currentPath = "";
        for (int i = 0; i < dirNames.length; i++) {
            final String currentName = dirNames[i].trim();
            if (!currentName.isEmpty()) {
                currentPath += "/" + currentName;
                dirPaths.add(currentPath);
            }
        }

        return dirPaths;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public void setName(final String name) {
        // TODO Auto-generated method stub

    }

    @Override
    public Map<String, Object> getConfigParams() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setConfigParams(final Map<String, Object> params) {
        // TODO Auto-generated method stub

    }

    @Override
    public int getRunLevel() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setRunLevel(final int level) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        _name = name;
        return true;
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }
}
