package com.cloud.utils.linux;

import java.util.Scanner;

import org.junit.Assert;
import org.junit.Test;
import org.junit.Assert;
import org.junit.Test;

import java.util.Scanner;

public class MemStatTest {
    final String memInfo = "MemTotal:        5830236 kB\n" +
            "MemFree:          156752 kB\n" +
            "Buffers:          326836 kB\n" +
            "Cached:          2606764 kB\n" +
            "SwapCached:            0 kB\n" +
            "Active:          4260808 kB\n" +
            "Inactive:         949392 kB\n";

    @Test
    public void getMemInfoParseTest() {
        MemStat memStat = null;
        try {
            memStat = new MemStat();
        } catch (RuntimeException ex) {
            // If test isn't run on linux we'll fail creation of linux-specific MemStat class due
            // to dependency on /proc/meminfo if we don't catch here.
            // We are really only interested in testing the parsing algorithm and getters.
            if (memStat == null) {
                throw ex;
            }
        }
        Scanner scanner = new Scanner(memInfo);
        memStat.parseFromScanner(scanner);

        Assert.assertEquals(memStat.getTotal(), Long.valueOf(5970161664L));
        Assert.assertEquals(memStat.getAvailable(), Long.valueOf(2829840384L));
        Assert.assertEquals(memStat.getFree(), Long.valueOf(160514048L));
        Assert.assertEquals(memStat.getCache(), Long.valueOf(2669326336L));
    }

    @Test
    public void reservedMemoryTest() {
        MemStat memStat = null;
        try {
            memStat = new MemStat(1024);
        } catch (RuntimeException ex) {
            if (memStat == null) {
                throw ex;
            }
        }
        Scanner scanner = new Scanner(memInfo);
        memStat.parseFromScanner(scanner);

        Assert.assertEquals(memStat.getTotal(), Long.valueOf(5970160640L));
    }
}