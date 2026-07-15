package de.gurkenlabs.utiliti;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.configuration.Quality;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.DebugCrasher;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.model.UtiLITIInitializationError;
import de.gurkenlabs.utiliti.view.components.UI;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Locale;

public class Program {

  public static void main(String[] args) {
    try {
      Game.init(() -> { // preInitialization

        // setup basic settings
        Game.info().setName("utiLITI");
        Resources.strings().setEncoding(StandardCharsets.UTF_8);

        // hook up configuration
        Game.config().add(Editor.preferences());

        Game.config().load();
        applyPreferredLocale();

        Game.info().setSubTitle(Resources.strings().get("app_subtitle"));
        Game.info().setVersion(Resources.strings().getFrom("licensing", "version"));
        UI.initLookAndFeel();

      }, () -> { // postInitialization

        // prepare UI and start the game
        UI.init();
        forceBasicEditorConfiguration();
        Game.world().camera().onZoom(event -> Editor.preferences().setZoom((float) event.getZoom()));


        Game.start();

        Input.keyboard().addKeyListener(new DebugCrasher());

        // configure input settings
        Input.mouse().setGrabMouse(false);
        Input.keyboard().consumeAlt(true);

        // load up previously opened project file or the one that is specified in
        // the command line arguments
        handleArgs(args);
        Path gameFile = Editor.preferences().getLastGameFile();
        if (!Editor.instance().fileLoaded() && gameFile != null) {
          Editor.instance().load(gameFile, false);
        }
      }, args);
    } catch (Throwable e) {
      throw new UtiLITIInitializationError("UtiLITI failed to initialize, see the stacktrace below for more information", e);
    }
  }

  private static void applyPreferredLocale() {
    String language = Editor.preferences().getPreferredLanguage();
    String country = Editor.preferences().getPreferredCountry();
    if (language != null && !language.isBlank() && country != null && !country.isBlank()
      && (!language.equals(Game.config().client().getLanguage()) || !country.equals(Game.config().client().getCountry()))) {
      Game.config().client().setLanguage(language);
      Game.config().client().setCountry(country);
      Game.config().save();
    }
    Locale.setDefault(Game.config().client().getLocale());
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
      Path.of(gameFile);
    } catch (InvalidPathException _) {
      return;
    }

    Path f = Path.of(gameFile);
    Editor.instance().load(f, false);
  }
}
