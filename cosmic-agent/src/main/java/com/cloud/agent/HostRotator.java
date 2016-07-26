package com.cloud.agent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HostRotator {
    private final List<String> hosts = new ArrayList<>();
    private int hostIndex = -1;

    public void addAll(final Collection<? extends String> hosts) {
        this.hosts.addAll(hosts);
    }

    public String nextHost() {
        validateCanRotate();
        incrementHostIndex();
        return hosts.get(hostIndex);
    }

    private void validateCanRotate() {
        if (hosts.isEmpty()) {
            throw new IllegalStateException("Cannot rotate over empty hosts list");
        }
    }

    private void incrementHostIndex() {
        hostIndex++;
        hostIndex = hostIndex % hosts.size();
    }
}
