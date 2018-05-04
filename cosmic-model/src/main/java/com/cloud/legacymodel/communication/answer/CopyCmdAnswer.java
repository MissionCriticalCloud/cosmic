package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.to.DataTO;

public class CopyCmdAnswer extends Answer {
    private DataTO newData;

    public CopyCmdAnswer(final DataTO newData) {
        super(null);
        this.newData = newData;
    }

    public CopyCmdAnswer(final String errMsg) {
        super(null, false, errMsg);
    }

    public DataTO getNewData() {
        return this.newData;
    }
}
