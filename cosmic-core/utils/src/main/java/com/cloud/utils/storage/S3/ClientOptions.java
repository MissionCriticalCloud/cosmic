//

//

package com.cloud.utils.storage.S3;

public interface ClientOptions {
    String getAccessKey();

    String getSecretKey();

    String getEndPoint();

    String getSigner();

    Boolean isHttps();

    Integer getConnectionTimeout();

    Integer getMaxErrorRetry();

    Integer getSocketTimeout();

    Boolean getUseTCPKeepAlive();

    Integer getConnectionTtl();
}
