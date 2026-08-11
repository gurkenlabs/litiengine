package de.gurkenlabs.litiengine.scripting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ScriptCompilationContextTests {

  @Test
  void dynamicCompilationUsesExplicitProjectClasspath(@TempDir Path tempDir) throws Exception {
    Path helperSource = tempDir.resolve("example/ProjectHelper.java");
    Path classes = tempDir.resolve("classes");
    Files.createDirectories(helperSource.getParent());
    Files.createDirectories(classes);
    Files.writeString(helperSource, """
      package example;
      public final class ProjectHelper {
        public static int value() { return 42; }
      }
      """, StandardCharsets.UTF_8);
    int result = ToolProvider.getSystemJavaCompiler().run(
        null, null, null, "-d", classes.toString(), helperSource.toString());
    assertEquals(0, result);

    ScriptDefinition definition = new ScriptDefinition(
        "project-script", "java", null, "ProjectScript", ScriptHostType.GAME);
    String source = """
      import de.gurkenlabs.litiengine.scripting.*;
      import example.ProjectHelper;
      public class ProjectScript extends GameScript {
        public int value() { return ProjectHelper.value(); }
      }
      """;
    ScriptCompilationContext context = new ScriptCompilationContext(
        getClass().getClassLoader(), List.of(classes), Runtime.version().feature());

    try (CompiledScript compiled = JavaScriptProvider.compileSource(definition, null, source, context)) {
      assertNotNull(compiled);
      assertEquals("ProjectScript", compiled.implementationType().getSimpleName());
    }
  }
}
