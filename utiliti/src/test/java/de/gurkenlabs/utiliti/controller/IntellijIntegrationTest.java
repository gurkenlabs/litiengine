package de.gurkenlabs.utiliti.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class IntellijIntegrationTest {

  @Test
  void findsLinuxLauncherOnPath(@TempDir Path root) throws Exception {
    Path first = Files.createDirectories(root.resolve("missing"));
    Path second = Files.createDirectories(root.resolve("bin"));
    Path launcher = Files.writeString(second.resolve("idea"), "#!/bin/sh\n");

    var result = IntellijIntegration.findOnPath(
        "linux", first + File.pathSeparator + second);

    assertEquals(launcher.toAbsolutePath().normalize(), result.orElseThrow());
  }

  @Test
  void ignoresMalformedAndMissingPathEntries(@TempDir Path root) {
    var result = IntellijIntegration.findOnPath(
        "linux", "\0invalid" + File.pathSeparator + root.resolve("missing"));

    assertTrue(result.isEmpty());
  }
}
