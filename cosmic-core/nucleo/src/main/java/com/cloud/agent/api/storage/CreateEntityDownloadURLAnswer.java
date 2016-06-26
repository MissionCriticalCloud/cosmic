//

//

package com.cloud.agent.api.storage;

import com.cloud.agent.api.Answer;

public class CreateEntityDownloadURLAnswer extends Answer {

    public static final short RESULT_SUCCESS = 1;
    public static final short RESULT_FAILURE = 0;
    String resultString;
    short resultCode;

    public CreateEntityDownloadURLAnswer(final String resultString, final short resultCode) {
        super();
        this.resultString = resultString;
        this.resultCode = resultCode;
    }

    public CreateEntityDownloadURLAnswer() {
    }
}
