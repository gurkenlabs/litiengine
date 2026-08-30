package de.gurkenlabs.utiliti.controller.debug;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.scripting.ScriptHostType;
import java.net.ServerSocket;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class JdiScriptDebuggerBackendTest {
  @Test
  void installsProjectBreakpointAddedAfterAttachForNestedClassPreparedLater() throws Exception {
    int port;
    try (ServerSocket socket = new ServerSocket(0)) {
      port = socket.getLocalPort();
    }
    Path java = Path.of(System.getProperty("java.home"), "bin", isWindows() ? "java.exe" : "java");
    Process process = new ProcessBuilder(
        java.toString(),
        "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=127.0.0.1:" + port,
        "-cp", System.getProperty("java.class.path"),
        JdiLateLoadedDebuggeeFixture.class.getName())
        .redirectErrorStream(true)
        .start();
    LinkedBlockingQueue<ScriptDebugSnapshot> snapshots = new LinkedBlockingQueue<>();
    JdiScriptDebuggerBackend debugger = new JdiScriptDebuggerBackend(new ScriptDebuggerBackend.Listener() {
      @Override
      public void paused(ScriptDebugSnapshot value) {
        snapshots.add(value);
      }
    });

    try {
      debugger.attach("127.0.0.1", port, List.of());
      debugger.setBreakpoints(List.of(new ScriptBreakpoint(
          "test", JdiLateLoadedDebuggeeFixture.class.getName(),
          "JdiLateLoadedDebuggeeFixture.java", 12, true)));

      ScriptDebugSnapshot snapshot = snapshots.poll(10, TimeUnit.SECONDS);
      assertTrue(snapshot != null);
      ScriptDebugSnapshot.Frame frame = snapshot.frames().stream()
          .filter(item -> item.className().equals(JdiLateLoadedDebuggeeFixture.class.getName() + "$Worker"))
          .findFirst().orElseThrow();
      assertEquals(12, frame.line());
      assertTrue(frame.variables().stream().anyMatch(variable -> variable.name().equals("value")
          && variable.value().equals("41")));

      debugger.resume();
      assertTrue(process.waitFor(10, TimeUnit.SECONDS));
      assertEquals(0, process.exitValue());
    } finally {
      debugger.close();
      if (process.isAlive()) process.destroyForcibly();
    }
  }

  @Test
  void stopsAtBreakpointInOrdinaryProjectClassWithoutScriptDefinition() throws Exception {
    int port;
    try (ServerSocket socket = new ServerSocket(0)) {
      port = socket.getLocalPort();
    }
    Path java = Path.of(System.getProperty("java.home"), "bin", isWindows() ? "java.exe" : "java");
    Process process = new ProcessBuilder(
        java.toString(),
        "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=127.0.0.1:" + port,
        "-cp", System.getProperty("java.class.path"),
        JdiDebuggeeFixture.class.getName())
        .redirectErrorStream(true)
        .start();
    LinkedBlockingQueue<ScriptDebugSnapshot> snapshots = new LinkedBlockingQueue<>();
    JdiScriptDebuggerBackend debugger = new JdiScriptDebuggerBackend(new ScriptDebuggerBackend.Listener() {
      @Override
      public void paused(ScriptDebugSnapshot value) {
        snapshots.add(value);
      }
    });

    try {
      debugger.setBreakpoints(List.of(new ScriptBreakpoint(
          "test", JdiDebuggeeFixture.class.getName(), "JdiDebuggeeFixture.java", 17, true)));
      debugger.attach("127.0.0.1", port, List.of());

      ScriptDebugSnapshot snapshot = snapshots.poll(10, TimeUnit.SECONDS);
      assertTrue(snapshot != null);
      ScriptDebugSnapshot.Frame frame = snapshot.frames().stream()
          .filter(item -> item.className().equals(JdiDebuggeeFixture.class.getName()))
          .findFirst().orElseThrow();
      assertEquals(17, frame.line());
      assertTrue(frame.variables().stream().anyMatch(variable -> variable.name().equals("value")
          && variable.value().equals("41")));

      debugger.resume();
      assertTrue(process.waitFor(10, TimeUnit.SECONDS));
      assertEquals(0, process.exitValue());
    } finally {
      debugger.close();
      if (process.isAlive()) process.destroyForcibly();
    }
  }

  @Test
  void stopsAtScriptBreakpointExposesVariablesAndStepsOver() throws Exception {
    int port;
    try (ServerSocket socket = new ServerSocket(0)) {
      port = socket.getLocalPort();
    }
    Path java = Path.of(System.getProperty("java.home"), "bin", isWindows() ? "java.exe" : "java");
    Process process = new ProcessBuilder(
        java.toString(),
        "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=127.0.0.1:" + port,
        "-cp", System.getProperty("java.class.path"),
        JdiDebuggeeFixture.class.getName())
        .redirectErrorStream(true)
        .start();
    LinkedBlockingQueue<ScriptDebugSnapshot> snapshots = new LinkedBlockingQueue<>();
    JdiScriptDebuggerBackend debugger = new JdiScriptDebuggerBackend(new ScriptDebuggerBackend.Listener() {
      @Override
      public void paused(ScriptDebugSnapshot value) {
        snapshots.add(value);
      }
    });

    try {
      ScriptDefinition definition = new ScriptDefinition(
          "fixture", "java", "JdiDebuggeeFixture.java", JdiDebuggeeFixture.class.getName(), ScriptHostType.GAME);
      debugger.setBreakpoints(List.of(new ScriptBreakpoint(
          "test", definition.getId(), definition.getSource(), 17, true)));
      debugger.attach("127.0.0.1", port, List.of(definition));

      ScriptDebugSnapshot first = snapshots.poll(10, TimeUnit.SECONDS);
      assertTrue(first != null);
      ScriptDebugSnapshot.Frame frame = first.frames().stream()
          .filter(item -> item.className().equals(JdiDebuggeeFixture.class.getName()))
          .findFirst().orElseThrow();
      assertEquals(17, frame.line());
      assertTrue(frame.variables().stream().anyMatch(variable -> variable.name().equals("value") && variable.value().equals("41")));
      assertTrue(frame.variables().stream().anyMatch(variable -> variable.name().equals("this.health")
          && variable.value().equals("100")));
      assertTrue(frame.variables().stream().anyMatch(variable -> variable.name().equals("counter")
          && variable.value().equals("7")));
      ScriptDebugSnapshot.Variable globals = frame.variables().stream()
          .filter(variable -> variable.name().equals("this.globals")).findFirst().orElseThrow();
      assertTrue(globals.expandable());
      assertTrue(debugger.expandVariable(globals.reference()).stream()
          .anyMatch(variable -> variable.name().equals("[score]") && variable.value().equals("42")));

      debugger.stepOver();
      ScriptDebugSnapshot second = snapshots.poll(10, TimeUnit.SECONDS);
      assertTrue(second != null);
      ScriptDebugSnapshot.Frame stepped = second.frames().stream()
          .filter(item -> item.className().equals(JdiDebuggeeFixture.class.getName()))
          .findFirst().orElseThrow();
      assertEquals(18, stepped.line());
      assertTrue(stepped.variables().stream().anyMatch(variable -> variable.name().equals("value")
          && variable.value().equals("42")));

      debugger.resume();
      assertTrue(process.waitFor(10, TimeUnit.SECONDS));
      assertEquals(0, process.exitValue());
    } finally {
      debugger.close();
      if (process.isAlive()) process.destroyForcibly();
    }
  }

  @Test
  void disabledBreakpointDoesNotSuspendTheProject() throws Exception {
    int port;
    try (ServerSocket socket = new ServerSocket(0)) {
      port = socket.getLocalPort();
    }
    Path java = Path.of(System.getProperty("java.home"), "bin", isWindows() ? "java.exe" : "java");
    Process process = new ProcessBuilder(
        java.toString(),
        "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=127.0.0.1:" + port,
        "-cp", System.getProperty("java.class.path"),
        JdiDebuggeeFixture.class.getName())
        .redirectErrorStream(true)
        .start();
    LinkedBlockingQueue<ScriptDebugSnapshot> snapshots = new LinkedBlockingQueue<>();
    JdiScriptDebuggerBackend debugger = new JdiScriptDebuggerBackend(new ScriptDebuggerBackend.Listener() {
      @Override
      public void paused(ScriptDebugSnapshot value) {
        snapshots.add(value);
      }
    });

    try {
      ScriptDefinition definition = new ScriptDefinition(
          "fixture", "java", "JdiDebuggeeFixture.java", JdiDebuggeeFixture.class.getName(), ScriptHostType.GAME);
      debugger.setBreakpoints(List.of(new ScriptBreakpoint(
          "test", definition.getId(), definition.getSource(), 17, false)));
      debugger.attach("127.0.0.1", port, List.of(definition));

      assertTrue(process.waitFor(10, TimeUnit.SECONDS));
      assertEquals(0, process.exitValue());
      assertNull(snapshots.poll(200, TimeUnit.MILLISECONDS));
    } finally {
      debugger.close();
      if (process.isAlive()) process.destroyForcibly();
    }
  }

  private static boolean isWindows() {
    return System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT).contains("win");
  }
}
