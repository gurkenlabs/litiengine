package de.gurkenlabs.litiengine.benchmark;

import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameTest;
import de.gurkenlabs.litiengine.entities.LightSource;
import de.gurkenlabs.litiengine.test.GameTestSuite;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Tag("benchmark")
@ExtendWith(GameTestSuite.class)
public class RenderingBenchmarkTests {

  private static final List<BenchmarkResult> RESULTS = new ArrayList<>();
  private static final Path RESULT_FILE = Paths.get(System.getProperty("user.dir"))
    .getParent().resolve("benchmark-results.txt");
  private static final Path BASELINE_FILE = Paths.get(System.getProperty("user.dir"))
    .getParent().resolve("benchmark-results-baseline.txt");

  @BeforeAll
  static void setup() throws IOException {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    Files.writeString(RESULT_FILE, "", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
  }

  @AfterAll
  static void tearDown() throws IOException {
    Map<String, Double> baseline = BenchmarkResult.parseBaseline(BASELINE_FILE);

    StringBuilder sb = new StringBuilder();
    sb.append("# LITIENGINE Rendering Benchmark Results\n");
    sb.append("Date: ").append(java.time.LocalDate.now()).append("\n");
    sb.append("Commit: ").append(getGitCommit()).append("\n\n");

    sb.append("## Raw Results (ms)\n\n");
    sb.append(BenchmarkResult.markdownHeader()).append("\n");
    for (BenchmarkResult r : RESULTS) {
      sb.append(r.toMarkdownRow()).append("\n");
    }
    sb.append("\n");

    sb.append("## Results (nanoseconds)\n\n");
    sb.append(BenchmarkResult.markdownNsHeader()).append("\n");
    for (BenchmarkResult r : RESULTS) {
      sb.append(r.toMarkdownNsRow()).append("\n");
    }
    sb.append("\n");

    if (!baseline.isEmpty()) {
      sb.append("## Change vs Baseline\n\n");
      sb.append("| Scene                      | Baseline (ns) | Current (ns) | Change  |\n");
      sb.append("|----------------------------|---------------|--------------|---------|\n");
      for (BenchmarkResult r : RESULTS) {
        Double bl = baseline.get(r.name());
        if (bl == null) {
          sb.append(String.format("| %-30s | %13s | %12.0f | %7s |\n",
            r.name(), "N/A", r.meanNs(), "N/A"));
        } else {
          sb.append(BenchmarkResult.diffRowNsWithBaseline(r.name(), bl, r)).append("\n");
        }
      }
      sb.append("\n");
    }

    Files.writeString(RESULT_FILE, sb.toString(), StandardOpenOption.APPEND);
    System.out.println("\n=== BENCHMARK RESULTS ===\n" + sb + "=== written to " + RESULT_FILE.toAbsolutePath() + " ===\n");

    BenchmarkResult.printResultsNs(RESULTS);
    if (!baseline.isEmpty()) {
      BenchmarkResult.printDiffTable(RESULTS, baseline);
    }

    GameTest.terminateGame();
  }

  @Test
  void benchmarkEmptyScene() {
    var r = BenchmarkScene.measure("empty scene", g -> {});
    RESULTS.add(r);
    assertTrue(r.meanMs() >= 0);
  }

  @Test
  void benchmarkEntityRender_50() {
    var r = BenchmarkScene.measureEntityRender(50, false);
    RESULTS.add(r);
  }

  @Test
  void benchmarkEntityRender_200() {
    var r = BenchmarkScene.measureEntityRender(200, false);
    RESULTS.add(r);
  }

  @Test
  void benchmarkEntityRender_500() {
    var r = BenchmarkScene.measureEntityRender(500, false);
    RESULTS.add(r);
  }

  @Test
  void benchmarkLightRender_Rectangle() {
    var r = BenchmarkScene.measureLightRender(4, 0, LightSource.Type.RECTANGLE);
    RESULTS.add(r);
  }

  @Test
  void benchmarkLightRender_Ellipse() {
    var r = BenchmarkScene.measureLightRender(4, 0, LightSource.Type.ELLIPSE);
    RESULTS.add(r);
  }

  @Test
  void benchmarkLightRender_EllipseWithShadows() {
    var r = BenchmarkScene.measureLightRender(4, 50, LightSource.Type.ELLIPSE);
    RESULTS.add(r);
  }

  @Test
  void benchmarkParticleRender_1x100() {
    var r = BenchmarkScene.measureParticleRender(1, 100);
    RESULTS.add(r);
  }

  @Test
  void benchmarkParticleRender_2x400() {
    var r = BenchmarkScene.measureParticleRender(2, 400);
    RESULTS.add(r);
  }

  private static String getGitCommit() {
    try {
      var process = new ProcessBuilder("git", "rev-parse", "--short", "HEAD")
        .directory(new java.io.File(System.getProperty("user.dir")).getParentFile())
        .redirectErrorStream(true)
        .start();
      return new String(process.getInputStream().readAllBytes()).trim();
    } catch (Exception e) {
      return "unknown";
    }
  }
}
