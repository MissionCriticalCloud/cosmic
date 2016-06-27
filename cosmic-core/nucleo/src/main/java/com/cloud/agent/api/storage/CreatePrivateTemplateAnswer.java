//

//

package com.cloud.agent.api.storage;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.storage.Storage.ImageFormat;

public class CreatePrivateTemplateAnswer extends Answer {
    private String _path;
    private long _virtualSize;
    private long _physicalSize;
    private String _uniqueName;
    private ImageFormat _format;

    public CreatePrivateTemplateAnswer() {
        super();
    }

    public CreatePrivateTemplateAnswer(final Command cmd, final boolean success, final String result, final String path, final long virtualSize, final long physicalSize, final
    String uniqueName,
                                       final ImageFormat format) {
        super(cmd, success, result);
        _path = path;
        _virtualSize = virtualSize;
        _physicalSize = physicalSize;
        _uniqueName = uniqueName;
        _format = format;
    }

    public CreatePrivateTemplateAnswer(final Command cmd, final boolean success, final String result) {
        super(cmd, success, result);
    }

    public String getPath() {
        return _path;
    }

    public void setPath(final String path) {
        _path = path;
    }

    public long getVirtualSize() {
        return _virtualSize;
    }

    public void setVirtualSize(final long virtualSize) {
        _virtualSize = virtualSize;
    }

    public void setphysicalSize(final long physicalSize) {
        this._physicalSize = physicalSize;
    }

    public long getphysicalSize() {
        return _physicalSize;
    }

    public String getUniqueName() {
        return _uniqueName;
    }

    public ImageFormat getImageFormat() {
        return _format;
    }
}
