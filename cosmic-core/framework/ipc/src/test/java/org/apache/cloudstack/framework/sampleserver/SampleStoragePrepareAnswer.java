package org.apache.cloudstack.framework.sampleserver;

import org.apache.cloudstack.framework.serializer.OnwireName;

@OnwireName(name = "SampleStoragePrepareAnswer")
public class SampleStoragePrepareAnswer {
    String result;

    public SampleStoragePrepareAnswer() {
    }

    public String getResult() {
        return result;
    }

    public void setResult(final String result) {
        this.result = result;
    }
}
