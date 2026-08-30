package de.gurkenlabs.utiliti.controller;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.resources.ResourceBundle;
import de.gurkenlabs.litiengine.test.SwingTestSuite;
import de.gurkenlabs.utiliti.view.components.ConsoleComponent;
import de.gurkenlabs.utiliti.view.components.UI;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

@ExtendWith(SwingTestSuite.class)
class EditorLoadConsoleTest {

  private ConsoleComponent consoleComponent;

  @BeforeEach
  void setUp() throws Exception {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    consoleComponent = new ConsoleComponent();
    setConsoleComponent(consoleComponent);
  }

  @AfterEach
  void tearDown() throws Exception {
    setConsoleComponent(null);
  }

  private static void setConsoleComponent(ConsoleComponent comp) throws Exception {
    Field field = UI.class.getDeclaredField("consoleComponent");
    field.setAccessible(true);
    field.set(null, comp);
  }

  @Test
  void clearConsoleSafeWhenNull() throws Exception {
    setConsoleComponent(null);
    assertDoesNotThrow(UI::clearConsole);
  }

  @Test
  void clearConsoleFlushesConsole() {
    LogHandler logHandler = consoleComponent.getLogHandler();
    logHandler.publish(new LogRecord(Level.INFO, "Test message"));
    assertEquals(1, logHandler.getRecentLogs().size());

    UI.clearConsole();

    assertEquals(0, logHandler.getRecentLogs().size());
    assertEquals(0, logHandler.getWarningCount());
    assertEquals(0, logHandler.getErrorCount());
  }

  @Test
  void loadingDifferentProjectClearsConsole(@TempDir Path tempDir) {
    Path project1 = tempDir.resolve("project1.litidata");
    Path project2 = tempDir.resolve("project2.litidata");

    ResourceBundle bundle1 = new ResourceBundle();
    bundle1.save(project1.toString(), false);

    ResourceBundle bundle2 = new ResourceBundle();
    bundle2.save(project2.toString(), false);

    LogHandler logHandler = consoleComponent.getLogHandler();
    logHandler.publish(new LogRecord(Level.WARNING, "Warning in previous project"));
    assertEquals(1, logHandler.getWarningCount());

    // Loading project 1 (different from null/none)
    Editor.instance().load(project1, true);

    // Warning from previous project should be cleared
    assertEquals(0, logHandler.getWarningCount());

    // Emit another warning
    logHandler.publish(new LogRecord(Level.WARNING, "Warning in project 1"));
    assertEquals(1, logHandler.getWarningCount());

    // Loading project 2 (different project)
    Editor.instance().load(project2, true);

    // Warning from project 1 should be cleared
    assertEquals(0, logHandler.getWarningCount());
  }
}
