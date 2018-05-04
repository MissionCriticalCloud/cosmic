package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.to.DataTO;

public final class CreateObjectAnswer extends Answer {
    private DataTO data;

    protected CreateObjectAnswer() {
        super();
    }

    public CreateObjectAnswer(final DataTO data) {
        super();
        this.data = data;
    }

    public CreateObjectAnswer(final String errMsg) {
        super(null, false, errMsg);
    }

    public DataTO getData() {
        return data;
    }
}
