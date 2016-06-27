//

//

package com.cloud.utils;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class FileUtil {

    public static void copyfile(final File source, final File destination) throws IOException {
        FileUtils.copyFile(source, destination);
    }
}
