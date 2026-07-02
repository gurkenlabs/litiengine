package de.gurkenlabs.litiengine.benchmark;

import java.util.Arrays;
import java.util.List;

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

  public long gcPauses() {
    return gcCollectionsAfter - gcCollectionsBefore;
  }

  public int sampleCount() { return samples.length; }

  public String toMarkdownRow() {
    return String.format("| %-30s | %6.2f | %6.2f | %6.2f | %6.2f | %4d | %4d |",
      name, meanMs(), minMs(), maxMs(), p99Ms(), gcPauses(), sampleCount());
  }

  public static String markdownHeader() {
    return "| Scene                      | Mean  | Min   | Max   | P99   | GC   | N    |\n"
         + "|----------------------------|-------|-------|-------|-------|------|------|";
  }

  public static String diffRow(String label, BenchmarkResult before, BenchmarkResult after) {
    double meanDelta = after.meanMs() - before.meanMs();
    double pct = before.meanMs() > 0 ? (meanDelta / before.meanMs()) * 100 : 0;
    String direction = meanDelta < 0 ? "fa" : "slower";
    return String.format("| %-30s | %6.2f -> %6.2f | %+.1f%% |",
      label, before.meanMs(), after.meanMs(), pct);
  }

  public static void printResults(List<BenchmarkResult> results) {
    System.out.println("\n=== BENCHMARK RESULTS ===");
    System.out.println(markdownHeader());
    for (BenchmarkResult r : results) {
      System.out.println(r.toMarkdownRow());
    }
    System.out.println("=========================\n");
  }
}
