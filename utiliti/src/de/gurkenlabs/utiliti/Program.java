package de.gurkenlabs.utiliti;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

import de.gurkenlabs.litiengine.DefaultUncaughtExceptionHandler;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.configuration.Quality;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.resources.Strings;
import de.gurkenlabs.utiliti.components.EditorScreen;
import de.gurkenlabs.utiliti.swing.UI;

public class Program {
  private static UserPreferenceConfiguration userPreferences;

  public static void main(String[] args) {
    // setup basic settings
    Game.info().setName("utiLITI");
    Game.info().setSubTitle("LITIengine Creation Kit");
    Game.info().setVersion("v0.4.16.1-alpha");
    Resources.strings().setEncoding(Strings.ENCODING_UTF_8);

    // hook up configuration and initialize the game
    userPreferences = new UserPreferenceConfiguration();
    Game.config().getConfigurationGroups().add(userPreferences);
    Game.init(args);
    forceBasicEditorConfiguration();
    Game.world().camera().onZoomChanged(zoom -> userPreferences.setZoom((float) zoom));

    // the editor should never crash, even if an exception occurs
    Game.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(false));
    
    // prepare UI and start the game
    UI.init();
    Game.start();

    // configure input settings
    Input.mouse().setGrabMouse(false);
    Input.keyboard().consumeAlt(true);

    // load up previously opened project file or the one that is specified in the command line arguments
    handleArgs(args);
    if (!EditorScreen.instance().fileLoaded() && userPreferences.getLastGameFile() != null) {
      EditorScreen.instance().load(new File(userPreferences.getLastGameFile()));
    }
  }

  private static void forceBasicEditorConfiguration() {
    // force configuration elements that are crucial for the editor
    Game.graphics().setBaseRenderScale(1.0f);
    Game.config().debug().setDebugEnabled(true);
    Game.config().graphics().setGraphicQuality(Quality.VERYHIGH);
    Game.config().graphics().setReduceFramesWhenNotFocused(false);
  }

  public static UserPreferenceConfiguration preferences() {
    return userPreferences;
  }

  private static void handleArgs(String[] args) {
    if (args.length == 0 || args[0] == null || args[0].isEmpty()) {
      return;
    }

    // handle file loading
    try {
      Paths.get(args[0]);
    } catch (InvalidPathException e) {
      return;
    }

    File f = new File(args[0]);
    EditorScreen.instance().load(f);
  }
}
