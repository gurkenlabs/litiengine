package de.gurkenlabs.utiliti.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.gurkenlabs.litiengine.Game;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class EditorLoadAsyncTest {

  @Test
  void loadAsyncHandlesNonExistentFileGracefully() {
    Editor editor = Editor.instance();
    editor.loadAsync(Path.of("non_existent_project_file.litidata"), true);
    assertFalse(editor.isLoading());
  }

  @Test
  void synchronousLoadClearsLoadingStateAfterFailure(@TempDir Path directory) throws Exception {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    Path invalidProject = directory.resolve("invalid.litidata");
    Files.writeString(invalidProject, "not a resource bundle");
    Editor editor = Editor.instance();
    var previousGameFile = editor.getGameFile();

    assertThrows(IllegalArgumentException.class, () -> editor.load(invalidProject, true));
    assertFalse(editor.isLoading());
    assertSame(previousGameFile, editor.getGameFile());
  }
}
