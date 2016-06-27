//

//

package com.cloud.utils.ssh;

import com.cloud.utils.SerialVersionUID;

public class SshException extends Exception {
    private static final long serialVersionUID = SerialVersionUID.sshException;

    public SshException(final String msg) {
        super(msg);
    }
}
