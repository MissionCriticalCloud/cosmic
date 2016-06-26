//

//

package com.cloud.utils.storage.S3;

public interface FileNamingStrategy {

    String determineFileName(String key);
}
