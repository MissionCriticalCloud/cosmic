//

//

package com.cloud.utils.storage.S3;

import java.io.File;

public interface ObjectNamingStrategy {

    String determineKey(File file);
}
