//

//

package com.cloud.utils.component;

import java.util.List;

public class AdapterList<T extends Adapter> {
    protected List<T> adapters;

    public AdapterList() {
    }

    public List<T> getAdapters() {
        return adapters;
    }

    public void setAdapters(final List<T> adapters) {
        this.adapters = adapters;
    }
}
