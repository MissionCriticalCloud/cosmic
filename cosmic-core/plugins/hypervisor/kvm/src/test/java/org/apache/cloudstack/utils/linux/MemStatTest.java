package org.apache.cloudstack.utils.linux;

import java.util.Scanner;

import org.junit.Assert;
import org.junit.Test;

public class MemStatTest {
    @Test
    public void getMemInfoParseTest() {
        final String memInfo = "MemTotal:        5830236 kB\n" +
                "MemFree:          156752 kB\n" +
                "Buffers:          326836 kB\n" +
                "Cached:          2606764 kB\n" +
                "SwapCached:            0 kB\n" +
                "Active:          4260808 kB\n" +
                "Inactive:         949392 kB\n";

        MemStat memStat = null;
        try {
            memStat = new MemStat();
        } catch (final RuntimeException ex) {
            // If test isn't run on linux we'll fail creation of linux-specific MemStat class due
            // to dependency on /proc/meminfo if we don't catch here.
            // We are really only interested in testing the parsing algorithm and getters.
            if (memStat == null) {
                throw ex;
            }
        }
        final Scanner scanner = new Scanner(memInfo);
        memStat.parseFromScanner(scanner);

        Assert.assertEquals(memStat.getTotal(), Double.valueOf(5830236));
        Assert.assertEquals(memStat.getAvailable(), Double.valueOf(2763516));
        Assert.assertEquals(memStat.getFree(), Double.valueOf(156752));
        Assert.assertEquals(memStat.getCache(), Double.valueOf(2606764));
    }
}
