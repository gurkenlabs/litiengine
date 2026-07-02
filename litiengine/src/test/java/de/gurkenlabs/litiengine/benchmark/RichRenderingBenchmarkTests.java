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
public class RichRenderingBenchmarkTests {

  private static final List<BenchmarkResult> RESULTS = new ArrayList<>();
  private static final Path RESULT_FILE = Paths.get(System.getProperty("user.dir"))
    .getParent().resolve("benchmark-results-real.txt");

  @BeforeAll
  static void setup() throws IOException {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
  }

  @AfterAll
  static void tearDown() throws IOException {
    Map<String, Double> baseline = Map.of();

    StringBuilder sb = new StringBuilder();
    sb.append("# LITIENGINE Rendering Benchmark (Real Graphics - Textured)\n");
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

    Files.writeString(RESULT_FILE, sb.toString(),
      StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    System.out.println("\n=== BENCHMARK RESULTS (Real Textured) ===\n" + sb
      + "=== written to " + RESULT_FILE.toAbsolutePath() + " ===\n");

    BenchmarkResult.printResultsNs(RESULTS);

    GameTest.terminateGame();
  }

  @Test
  void benchmarkTexturedEntity_50() {
    var r = RichBenchmarkScene.measureTexturedEntities(50);
    RESULTS.add(r);
  }

  @Test
  void benchmarkTexturedEntity_200() {
    var r = RichBenchmarkScene.measureTexturedEntities(200);
    RESULTS.add(r);
  }

  @Test
  void benchmarkTexturedEntity_500() {
    var r = RichBenchmarkScene.measureTexturedEntities(500);
    RESULTS.add(r);
  }

  @Test
  void benchmarkTileMap_50x50() {
    var r = RichBenchmarkScene.measureTexturedTileMap(50, 50);
    RESULTS.add(r);
  }

  @Test
  void benchmarkTileMap_100x100() {
    var r = RichBenchmarkScene.measureTexturedTileMap(100, 100);
    RESULTS.add(r);
  }

  @Test
  void benchmarkFullScene() {
    var r = RichBenchmarkScene.measureFullScene(100, 50, 4, 20);
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
