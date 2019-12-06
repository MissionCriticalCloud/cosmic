package com.cloud.utils.linux;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class MemStat {

    protected static final String MEMINFO_FILE = "/proc/meminfo";
    protected static final String FREE_KEY = "MemFree";
    protected static final String CACHE_KEY = "Cached";
    protected static final String TOTAL_KEY = "MemTotal";
    long reservedMemory;

    private final Map<String, Long> memStats = new HashMap<>();

    public MemStat() {
        this(0);
    }

    public MemStat(long reservedMemory) {
        this.reservedMemory = reservedMemory;
        this.refresh();
    }

    public Long getTotal() {
        return memStats.get(TOTAL_KEY) - reservedMemory;
    }

    public Long getAvailable() {
        return getFree() + getCache();
    }

    public Long getFree() {
        return memStats.get(FREE_KEY) - reservedMemory;
    }

    public Long getCache() {
        return memStats.get(CACHE_KEY);
    }

    public void refresh() {
        final File f = new File(MEMINFO_FILE);
        try (Scanner scanner = new Scanner(f, "UTF-8")) {
            parseFromScanner(scanner);
        } catch (final FileNotFoundException ex) {
            throw new RuntimeException("File " + MEMINFO_FILE + " not found:" + ex.toString());
        }
    }

    protected void parseFromScanner(final Scanner scanner) {
        scanner.useDelimiter("\\n");
        while (scanner.hasNext()) {
            final String[] stats = scanner.next().split("\\:\\s+");
            if (stats.length == 2) {
                memStats.put(stats[0], Long.valueOf(stats[1].replaceAll("\\s+\\w+", ""))  * 1024L);
            }
        }
    }
}
