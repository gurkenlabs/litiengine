package de.gurkenlabs.utiliti.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class ProjectLaunchRequestTest {
  @Test
  void parsesOptionalGradleArgumentsAndQuotedValues() {
    assertEquals(List.of(), ProjectLaunchRequest.parseBuildArguments(" "));
    assertEquals(
        List.of("--stacktrace", "--info", "-Pprofile=local dev", "-Dpath=C:\\Program Files", ""),
        ProjectLaunchRequest.parseBuildArguments(
            "--stacktrace --info -Pprofile=\"local dev\" -Dpath=\"C:\\Program Files\" \"\""));
    assertEquals(
        List.of("-Dshare=\\\\server\\gradle-cache"),
        ProjectLaunchRequest.parseBuildArguments("-Dshare=\\\\server\\gradle-cache"));
  }

  @Test
  void rejectsUnclosedQuotes() {
    assertThrows(
        IllegalArgumentException.class,
        () -> ProjectLaunchRequest.parseBuildArguments("--args=\"unfinished"));
  }
}
