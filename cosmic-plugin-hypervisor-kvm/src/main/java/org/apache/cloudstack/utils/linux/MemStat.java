package org.apache.cloudstack.utils.linux;

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

  private final Map<String, Double> memStats = new HashMap<String, Double>();

  public MemStat() {
  }

  public Double getTotal() {
    return memStats.get(TOTAL_KEY);
  }

  public Double getAvailable() {
    return getFree() + getCache();
  }

  public Double getFree() {
    return memStats.get(FREE_KEY);
  }

  public Double getCache() {
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

  protected void parseFromScanner(Scanner scanner) {
    scanner.useDelimiter("\\n");
    while (scanner.hasNext()) {
      final String[] stats = scanner.next().split("\\:\\s+");
      if (stats.length == 2) {
        memStats.put(stats[0], Double.valueOf(stats[1].replaceAll("\\s+\\w+", "")));
      }
    }
  }
}
