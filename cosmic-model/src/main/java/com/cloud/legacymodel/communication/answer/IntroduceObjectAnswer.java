package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.to.DataTO;

public class IntroduceObjectAnswer extends Answer {
    private final DataTO dataTO;

    public IntroduceObjectAnswer(final DataTO dataTO) {
        this.dataTO = dataTO;
    }

    public DataTO getDataTO() {
        return dataTO;
    }
}
