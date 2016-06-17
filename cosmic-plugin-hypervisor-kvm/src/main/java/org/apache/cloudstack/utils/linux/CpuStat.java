package org.apache.cloudstack.utils.linux;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CpuStat {

  private final Logger logger = LoggerFactory.getLogger(CpuStat.class);

  private Integer cores;
  private UptimeStats lastStats;
  private final String sysfsCpuDir = "/sys/devices/system/cpu";
  private final String uptimeFile = "/proc/uptime";

  class UptimeStats {
    public Double upTime = 0d;
    public Double cpuIdleTime = 0d;

    public UptimeStats(Double upTime, Double cpuIdleTime) {
      this.upTime = upTime;
      this.cpuIdleTime = cpuIdleTime;
    }
  }

  public CpuStat() {
    init();
  }

  private void init() {
    cores = getCoresFromLinux();
    lastStats = getUptimeAndCpuIdleTime();
  }

  private UptimeStats getUptimeAndCpuIdleTime() {
    UptimeStats uptime = new UptimeStats(0d, 0d);
    final File f = new File(uptimeFile);
    try (Scanner scanner = new Scanner(f, "UTF-8");) {
      final String[] stats = scanner.useDelimiter("\\Z").next().split("\\s+");
      uptime = new UptimeStats(Double.parseDouble(stats[0]), Double.parseDouble(stats[1]));
    } catch (final FileNotFoundException ex) {
      logger.warn("File " + uptimeFile + " not found:" + ex.toString());
    }
    return uptime;
  }

  private Integer getCoresFromLinux() {
    Integer cpus = 0;
    final File cpuDir = new File(sysfsCpuDir);
    final File[] files = cpuDir.listFiles();
    if (files != null) {
      for (final File file : files) {
        if (file.getName().matches("cpu\\d+")) {
          cpus++;
        }
      }
    }
    return cpus;
  }

  public Integer getCores() {
    return cores;
  }

  public Double getCpuUsedPercent() {
    Double cpuUsed = 0d;
    if (cores == null || cores == 0) {
      cores = getCoresFromLinux();
    }

    final UptimeStats currentStats = getUptimeAndCpuIdleTime();
    if (currentStats == null) {
      return cpuUsed;
    }

    final Double timeElapsed = currentStats.upTime - lastStats.upTime;
    final Double cpuElapsed = (currentStats.cpuIdleTime - lastStats.cpuIdleTime) / cores;
    if (timeElapsed > 0) {
      cpuUsed = (1 - cpuElapsed / timeElapsed) * 100;
    }
    if (cpuUsed < 0) {
      cpuUsed = 0d;
    }
    lastStats = currentStats;
    return cpuUsed;
  }
}