package de.gurkenlabs.utiliti.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class EditorLoadAsyncTest {

  @Test
  void loadAsyncHandlesNonExistentFileGracefully() {
    Editor editor = Editor.instance();
    editor.loadAsync(Path.of("non_existent_project_file.litidata"), true);
    assertFalse(editor.isLoading());
  }
}
