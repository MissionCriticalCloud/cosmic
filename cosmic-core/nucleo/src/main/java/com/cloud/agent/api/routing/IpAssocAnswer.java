//

//

package com.cloud.agent.api.routing;

import com.cloud.agent.api.Answer;

public class IpAssocAnswer extends Answer {
    public static final String errorResult = "Failed";
    String[] results;

    protected IpAssocAnswer() {
        super();
    }

    public IpAssocAnswer(final IpAssocCommand cmd, final String[] results) {

        boolean finalResult = true;
        for (final String result : results) {
            if (result.equals(errorResult)) {
                finalResult = false;
                break;
            }
        }
        this.result = finalResult;
        this.details = null;
        assert (cmd.getIpAddresses().length == results.length) : "Shouldn't the results match the commands?";
        this.results = results;
    }

    String[] getResults() {
        return results;
    }
}
