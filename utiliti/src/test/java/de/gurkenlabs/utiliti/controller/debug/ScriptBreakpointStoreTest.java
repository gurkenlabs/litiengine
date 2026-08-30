package de.gurkenlabs.utiliti.controller.debug;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ScriptBreakpointStoreTest {
  @Test
  void roundTripsProjectScopedBreakpointsInStableOrder() {
    List<ScriptBreakpoint> breakpoints = List.of(
        new ScriptBreakpoint("project-b", "second", "scripts/Second.java", 18, false),
        new ScriptBreakpoint("project-a", "first", "scripts/First.java", 9, true));

    List<ScriptBreakpoint> decoded = ScriptBreakpointStore.decode(ScriptBreakpointStore.encode(breakpoints));

    assertEquals(List.of(breakpoints.get(1), breakpoints.get(0)), decoded);
  }

  @Test
  void malformedStorageDoesNotPreventEditorStartup() {
    assertTrue(ScriptBreakpointStore.decode("not-json").isEmpty());
  }

  @Test
  void invalidLinesAreSkippedWithoutDiscardingValidBreakpoints() {
    String stored = "[null,{\"project\":\"p\",\"scriptId\":\"broken\",\"source\":\"S.java\",\"line\":\"bad\"},"
        + "{\"project\":\"p\",\"scriptId\":\"s\",\"source\":\"S.java\",\"line\":0},"
        + "{\"project\":\"p\",\"scriptId\":\"s\",\"source\":\"S.java\",\"line\":7}]";

    List<ScriptBreakpoint> decoded = ScriptBreakpointStore.decode(stored);

    assertEquals(1, decoded.size());
    assertEquals(7, decoded.getFirst().line());
  }

  @Test
  void nullCollectionEncodesAsEmptyStorage() {
    assertEquals(List.of(), ScriptBreakpointStore.decode(ScriptBreakpointStore.encode(null)));
  }

  @Test
  void duplicateBreakpointsAreStoredOnce() {
    ScriptBreakpoint breakpoint = new ScriptBreakpoint("p", "s", "S.java", 7, true);

    assertEquals(List.of(breakpoint),
        ScriptBreakpointStore.decode(ScriptBreakpointStore.encode(List.of(breakpoint, breakpoint))));
  }
}
