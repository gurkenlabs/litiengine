package de.gurkenlabs.utiliti.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class MavenCentralVersionsTest {
  @Test
  void parsesPublishedVersionsNewestFirstAndExcludesSnapshots() {
    String metadata = """
      <metadata><versioning><versions>
        <version>0.10.0</version>
        <version>0.11.0</version>
        <version>0.13.0-SNAPSHOT</version>
        <version>0.12.0</version>
      </versions></versioning></metadata>
      """;

    assertEquals(List.of("0.12.0", "0.11.0", "0.10.0"), MavenCentralVersions.parse(metadata));
  }
}
