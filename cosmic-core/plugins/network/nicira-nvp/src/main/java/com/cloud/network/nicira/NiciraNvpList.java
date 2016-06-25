//

//

package com.cloud.network.nicira;

import java.util.List;

public class NiciraNvpList<T> {
    private List<T> results;
    private int resultCount;

    public List<T> getResults() {
        return this.results;
    }

    public void setResults(final List<T> results) {
        this.results = results;
    }

    public int getResultCount() {
        return resultCount;
    }

    public void setResultCount(final int resultCount) {
        this.resultCount = resultCount;
    }

    public boolean isEmpty() {
        return this.resultCount == 0;
    }
}
