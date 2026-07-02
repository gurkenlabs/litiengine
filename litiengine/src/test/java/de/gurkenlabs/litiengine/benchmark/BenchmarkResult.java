package de.gurkenlabs.litiengine.benchmark;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BenchmarkResult {
  private final String name;
  private final double[] samples;
  private final long gcCollectionsBefore;
  private final long gcCollectionsAfter;

  public BenchmarkResult(String name, double[] samples, long gcBefore, long gcAfter) {
    this.name = name;
    this.samples = samples;
    this.gcCollectionsBefore = gcBefore;
    this.gcCollectionsAfter = gcAfter;
  }

  public String name() { return name; }

  public double meanMs() {
    return Arrays.stream(samples).average().orElse(0);
  }

  public double meanNs() {
    return meanMs() * 1_000_000;
  }

  public double minMs() {
    return Arrays.stream(samples).min().orElse(0);
  }

  public double maxMs() {
    return Arrays.stream(samples).max().orElse(0);
  }

  public double p99Ms() {
    double[] sorted = samples.clone();
    Arrays.sort(sorted);
    int idx = (int)(sorted.length * 0.99);
    return sorted[Math.min(idx, sorted.length - 1)];
  }

  public double stddevMs() {
    double mean = meanMs();
    double variance = Arrays.stream(samples).map(s -> Math.pow(s - mean, 2)).average().orElse(0);
    return Math.sqrt(variance);
  }

  public double stddevNs() {
    return stddevMs() * 1_000_000;
  }

  public long gcPauses() {
    return gcCollectionsAfter - gcCollectionsBefore;
  }

  public int sampleCount() { return samples.length; }

  public String toMarkdownRow() {
    return String.format("| %-30s | %6.2f | %6.2f | %6.2f | %6.2f | %4d | %4d |",
      name, meanMs(), minMs(), maxMs(), p99Ms(), gcPauses(), sampleCount());
  }

  public String toMarkdownNsRow() {
    return String.format("| %-30s | %10.0f | %8.0f | %8.0f | %8.0f | %4d | %4d |",
      name, meanNs(), minMs() * 1_000_000, maxMs() * 1_000_000, p99Ms() * 1_000_000, gcPauses(), sampleCount());
  }

  public static String markdownHeader() {
    return "| Scene                      | Mean  | Min   | Max   | P99   | GC   | N    |\n"
         + "|----------------------------|-------|-------|-------|-------|------|------|";
  }

  public static String markdownNsHeader() {
    return "| Scene                      | Mean (ns) | Min (ns) | Max (ns) | P99 (ns) | GC   | N    |\n"
         + "|----------------------------|-----------|----------|----------|----------|------|------|";
  }

  public static String diffRowMs(String label, BenchmarkResult before, BenchmarkResult after) {
    double meanDelta = after.meanMs() - before.meanMs();
    double pct = before.meanMs() > 0 ? (meanDelta / before.meanMs()) * 100 : 0;
    return String.format("| %-30s | %6.2f -> %6.2f | %+.2f%% |",
      label, before.meanMs(), after.meanMs(), pct);
  }

  public static String diffRowNs(String label, BenchmarkResult before, BenchmarkResult after) {
    double meanDeltaNs = after.meanNs() - before.meanNs();
    double pct = before.meanMs() > 0 ? (meanDeltaNs / before.meanNs()) * 100 : 0;
    return String.format("| %-30s | %10.0f -> %-10.0f | %+.2f%% |",
      label, before.meanNs(), after.meanNs(), pct);
  }

  public static String diffRowNsWithBaseline(String label, double baselineMeanMs, BenchmarkResult after) {
    double baselineMeanNs = baselineMeanMs * 1_000_000;
    double deltaNs = after.meanNs() - baselineMeanNs;
    if (baselineMeanMs < 0.001) {
      return String.format("| %-30s | %10.0f | %10.0f | %7s |",
        label, baselineMeanNs, after.meanNs(), "N/A");
    }
    double pct = (deltaNs / baselineMeanNs) * 100;
    return String.format("| %-30s | %10.0f | %10.0f | %+.2f%% |",
      label, baselineMeanNs, after.meanNs(), pct);
  }

  public static Map<String, Double> parseBaseline(Path file) throws IOException {
    Map<String, Double> baselines = new HashMap<>();
    if (!Files.exists(file)) return baselines;
    Pattern linePat = Pattern.compile("^\\|\\s+(.+?)\\s+\\|\\s+([\\d.]+)\\s+\\|");
    for (String line : Files.readAllLines(file)) {
      Matcher m = linePat.matcher(line);
      if (m.find()) {
        String name = m.group(1).trim();
        double mean = Double.parseDouble(m.group(2));
        baselines.put(name, mean);
      }
    }
    return baselines;
  }

  public static void printResults(List<BenchmarkResult> results) {
    System.out.println("\n=== BENCHMARK RESULTS ===");
    System.out.println(markdownHeader());
    for (BenchmarkResult r : results) {
      System.out.println(r.toMarkdownRow());
    }
    System.out.println("=========================\n");
  }

  public static void printResultsNs(List<BenchmarkResult> results) {
    System.out.println("\n=== BENCHMARK RESULTS (nanoseconds) ===");
    System.out.println(markdownNsHeader());
    for (BenchmarkResult r : results) {
      System.out.println(r.toMarkdownNsRow());
    }
    System.out.println("======================================\n");
  }

  public static void printDiffTable(List<BenchmarkResult> results, Map<String, Double> baseline) {
    System.out.println("\n=== CHANGE vs BASELINE (nanoseconds) ===");
    System.out.println("| Scene                      | Baseline (ns) | Current (ns) | Change  |");
    System.out.println("|----------------------------|---------------|--------------|---------|");
    for (BenchmarkResult r : results) {
      Double bl = baseline.get(r.name());
      if (bl == null) {
        System.out.println(String.format("| %-30s | %13s | %12.0f | %7s |",
          r.name(), "N/A", r.meanNs(), "N/A"));
      } else {
        System.out.println(diffRowNsWithBaseline(r.name(), bl, r));
      }
    }
    System.out.println("========================================\n");
  }
}
