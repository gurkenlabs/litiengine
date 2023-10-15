package de.gurkenlabs.utiliti;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.configuration.Quality;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.components.Editor;
import de.gurkenlabs.utiliti.handlers.DebugCrasher;
import de.gurkenlabs.utiliti.swing.UI;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

public class Program {
  //Testing commiting
  //new branch testing. and seeing how it works.

  public static void main(String[] args) {
    try {
      Game.init(
          () -> { // preInitialization

            // setup basic settings
            Game.info().setName("utiLITI");
            Game.info().setSubTitle("LITIENGINE Creation Kit");
            Game.info().setVersion(Resources.strings().getFrom("licensing","version"));
            Resources.strings().setEncoding(StandardCharsets.UTF_8);

            // hook up configuration
            Game.config().add(Editor.preferences());

            Game.config().load();

            UI.initLookAndFeel();

          },
          () -> { // postInitialization

            // prepare UI and start the game
            UI.init();
            forceBasicEditorConfiguration();
            Game.world()
                .camera()
                .onZoom(event -> Editor.preferences().setZoom((float) event.getZoom()));


            Game.start();

            Input.keyboard().addKeyListener(new DebugCrasher());

            // configure input settings
            Input.mouse().setGrabMouse(false);
            Input.keyboard().consumeAlt(true);

            // load up previously opened project file or the one that is specified in
            // the command line arguments
            handleArgs(args);
            String gameFile = Editor.preferences().getLastGameFile();
            if (!Editor.instance().fileLoaded() && gameFile != null && !gameFile.isEmpty()) {
              Editor.instance().load(new File(gameFile.trim()), false);
            }
          },
          args);
    } catch (Throwable e) {
      throw new UtiLITIInitializationError(
          "UtiLITI failed to initialize, see the stacktrace below for more information", e);
    }
  }

  private static void forceBasicEditorConfiguration() {
    // force configuration elements that are crucial for the editor
    Game.graphics().setBaseRenderScale(1.0f);
    Game.config().debug().setDebugEnabled(true);
    Game.config().graphics().setGraphicQuality(Quality.VERYHIGH);
    Game.config().graphics().setReduceFramesWhenNotFocused(false);
    Game.config().graphics().setEnableResolutionScale(false);
  }

  private static void handleArgs(String[] args) {
    if (args.length == 0) {
      return;
    }

    String gameFile = args[0].trim();
    if (gameFile.isEmpty()) {
      return;
    }

    // handle file loading
    try {
      Paths.get(gameFile);
    } catch (InvalidPathException e) {
      return;
    }

    File f = new File(gameFile);
    Editor.instance().load(f, false);
  }
}
