package com.litiengine.utiliti.components;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.bind.JAXBException;

import com.litiengine.Game;
import com.litiengine.environment.tilemap.IImageLayer;
import com.litiengine.environment.tilemap.ITileset;
import com.litiengine.environment.tilemap.xml.Blueprint;
import com.litiengine.environment.tilemap.xml.Tileset;
import com.litiengine.environment.tilemap.xml.TmxMap;
import com.litiengine.graphics.Spritesheet;
import com.litiengine.graphics.TextRenderer;
import com.litiengine.graphics.emitters.xml.EmitterData;
import com.litiengine.graphics.emitters.xml.EmitterLoader;
import com.litiengine.gui.screens.Screen;
import com.litiengine.resources.ImageFormat;
import com.litiengine.resources.ResourceBundle;
import com.litiengine.resources.Resources;
import com.litiengine.resources.SoundFormat;
import com.litiengine.resources.SoundResource;
import com.litiengine.resources.SpritesheetResource;
import com.litiengine.resources.TextureAtlas;
import com.litiengine.util.ArrayUtilities;
import com.litiengine.util.MathUtilities;
import com.litiengine.util.io.FileUtilities;
import com.litiengine.util.io.XmlUtilities;
import com.litiengine.utiliti.Cursors;
import com.litiengine.utiliti.Style;
import com.litiengine.utiliti.UndoManager;
import com.litiengine.utiliti.UserPreferences;
import com.litiengine.utiliti.swing.StatusBar;
import com.litiengine.utiliti.swing.Tray;
import com.litiengine.utiliti.swing.UI;
import com.litiengine.utiliti.swing.dialogs.ConfirmDialog;
import com.litiengine.utiliti.swing.dialogs.EditorFileChooser;
import com.litiengine.utiliti.swing.dialogs.SpritesheetImportPanel;
import com.litiengine.utiliti.swing.dialogs.XmlImportDialog;

public class Editor extends Screen {
  private static final Logger log = Logger.getLogger(Editor.class.getName());
  private static final int STATUS_DURATION = 5000;
  private static final String DEFAULT_GAME_NAME = "game";
  private static final String NEW_GAME_STRING = "NEW GAME *";

  private static final String GAME_FILE_NAME = "Game Resource File";
  private static final String SPRITE_FILE_NAME = "Sprite Info File";
  private static final String AUDIO_FILE_NAME = "Audio File";
  private static final String SPRITESHEET_FILE_NAME = "Spritesheet Image";
  private static final String TEXTUREATLAS_FILE_NAME = "Texture Atlas XML (generic)";

  private static final String IMPORT_DIALOGUE = "import_something";

  private static Editor instance;
  private static UserPreferences preferences;

  private final List<Runnable> loadedCallbacks;

  private MapComponent mapComponent;
  private ResourceBundle gameFile = new ResourceBundle();
  private String projectPath;
  private String currentResourceFile;

  private long statusTick;
  private String currentStatus;
  private boolean loading;

  private Editor() {
    super("Editor");
    this.loadedCallbacks = new CopyOnWriteArrayList<>();
    this.mapComponent = new MapComponent();
  }

  public static Editor instance() {
    if (instance == null) {
      instance = new Editor();
    }

    return instance;
  }

  public boolean fileLoaded() {
    return this.currentResourceFile != null;
  }

  public static UserPreferences preferences() {
    if (preferences == null) {
      preferences = new UserPreferences();
    }

    return preferences;
  }

  @Override
  public void prepare() {
    this.getComponents().add(this.mapComponent);
    super.prepare();
  }

  @Override
  public void render(final Graphics2D g) {

    if (Game.world().environment() != null) {
      Game.world().environment().render(g);
    }

    if (Resources.images().count() > 200) {
      Resources.images().clear();
      log.log(Level.INFO, "cache cleared!");
    }

    if (this.currentResourceFile != null) {
      Game.window().setTitle(Game.info().getName() + " " + Game.info().getVersion() + " - " + this.currentResourceFile);

      String mapName = Game.world().environment() != null && Game.world().environment().getMap() != null ? "\nMap: " + Game.world().environment().getMap().getName() : "";
      Tray.setToolTip(Game.info().getName() + " " + Game.info().getVersion() + "\n" + this.currentResourceFile + mapName);
    } else if (this.getProjectPath() != null) {
      Game.window().setTitle(Game.info().getTitle() + " - " + NEW_GAME_STRING);
      Tray.setToolTip(Game.info().getTitle() + "\n" + NEW_GAME_STRING);
    } else {
      Game.window().setTitle(Game.info().getTitle());
    }

    super.render(g);

    // render fps
    g.setFont(Style.getDefaultFont());
    g.setColor(Color.WHITE);
    TextRenderer.render(g, Game.metrics().getFramesPerSecond() + " FPS", 10, Game.window().getResolution().getHeight() - 20, true);

    if (Game.time().now() % 4 == 0) {
      StatusBar.update();
    }

    // render status
    if (this.currentStatus != null && !this.currentStatus.isEmpty()) {
      long deltaTime = Game.time().since(this.statusTick);
      if (deltaTime > STATUS_DURATION) {
        this.currentStatus = null;
      }

      // fade out status color
      final double fadeOutTime = 0.75 * STATUS_DURATION;
      if (deltaTime > fadeOutTime) {
        double fade = deltaTime - fadeOutTime;
        int alpha = (int) (255 - (fade / (STATUS_DURATION - fadeOutTime)) * 255);
        g.setColor(new Color(Style.COLOR_STATUS.getRed(), Style.COLOR_STATUS.getGreen(), Style.COLOR_STATUS.getBlue(), MathUtilities.clamp(alpha, 0, 255)));
      }

      Font old = g.getFont();
      g.setFont(g.getFont().deriveFont(20.0f * Editor.preferences().getUiScale()));
      TextRenderer.render(g, this.currentStatus, 10, Game.window().getResolution().getHeight() - 60, true);
      g.setFont(old);
    }
  }

  public ResourceBundle getGameFile() {
    return this.gameFile;
  }

  public String getProjectPath() {
    return projectPath;
  }

  public void setProjectPath(String projectPath) {
    this.projectPath = projectPath;
  }

  public void create() {
    JFileChooser chooser;
    try {
      chooser = new JFileChooser(new File(".").getCanonicalPath());
      chooser.setDialogTitle(Resources.strings().get("input_select_project_folder"));
      chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      if (chooser.showOpenDialog(Game.window().getHostControl()) != JFileChooser.APPROVE_OPTION) {
        return;
      }

      if (Game.world().environment() != null) {
        Game.world().unloadEnvironment();
      }

      // set up project settings
      this.setProjectPath(chooser.getSelectedFile().getCanonicalPath());

      // load all maps in the directory
      this.mapComponent.loadMaps(this.getProjectPath());
      this.currentResourceFile = null;
      this.gameFile = new ResourceBundle();

      // add sprite sheets by tile sets of all maps in the project director
      for (TmxMap map : this.mapComponent.getMaps()) {
        this.loadSpriteSheets(map);
      }

      UI.getAssetController().refresh();

      // load custom emitter files
      loadCustomEmitters(this.getGameFile().getEmitters());

      // update new game file by the loaded information
      this.updateGameFileMaps();

      // display first available map after loading all stuff
      if (!this.mapComponent.getMaps().isEmpty()) {
        this.mapComponent.loadEnvironment(this.mapComponent.getMaps().get(0));
      }

      this.gamefileLoaded();
    } catch (IOException e) {
      log.log(Level.SEVERE, e.getLocalizedMessage(), e);
    }

    this.setCurrentStatus("created new project");
  }

  public void load() {
    if (EditorFileChooser.showFileDialog(ResourceBundle.FILE_EXTENSION, GAME_FILE_NAME, false, ResourceBundle.FILE_EXTENSION) == JFileChooser.APPROVE_OPTION) {
      this.load(EditorFileChooser.instance().getSelectedFile(), false);
    }
  }

  public void close(boolean force) {
    if (!force) {
      boolean proceedClosing = UI.notifyPendingChanges();
      if (!proceedClosing) {
        return;
      }
    }

    Game.world().unloadEnvironment();
    UndoManager.clearAll();
    getMapComponent().clearAll();
    this.currentResourceFile = null;
    this.gameFile = null;
    this.setProjectPath(null);
    this.mapComponent.loadMaps(Arrays.asList(), true);
    Resources.clearAll();
    UI.getAssetController().refresh();
    this.gamefileLoaded();
    this.setCurrentStatus(Resources.strings().get("status_gamefile_closed"));
  }

  public void load(File gameFile, boolean force) {
    if (!force) {
      boolean proceedLoading = UI.notifyPendingChanges();
      if (!proceedLoading) {
        return;
      }
    }

    if (!gameFile.exists()) {
      log.log(Level.SEVERE, "gameFile {0} does not exist", gameFile);
      return;
    }

    if (!FileUtilities.getExtension(gameFile).equals(ResourceBundle.FILE_EXTENSION)) {
      log.log(Level.SEVERE, "[{0}] unsupported file format [{1}]", new Object[] { gameFile, FileUtilities.getExtension(gameFile) });
      return;
    }

    final long currentTime = System.nanoTime();
    Game.window().cursor().set(Cursors.LOAD, 0, 0);
    Game.window().cursor().setOffsetX(0);
    Game.window().cursor().setOffsetY(0);

    this.loading = true;
    try {
      UndoManager.clearAll();

      // set up project settings
      this.currentResourceFile = gameFile.getPath();
      this.gameFile = ResourceBundle.load(gameFile.getPath());
      if (this.gameFile == null) {
        throw new IllegalArgumentException("The game file " + gameFile + " could not be loaded!");
      }

      this.setProjectPath(gameFile.getPath());

      // load maps from game file
      this.mapComponent.loadMaps(this.getGameFile().getMaps(), true);

      Resources.images().clear();
      Resources.spritesheets().clear();

      // load sprite sheets from different sources:
      // 1. add sprite sheets from game file
      // 2. add sprite sheets by tile sets of all maps in the game file
      this.loadSpriteSheets(this.getGameFile().getSpriteSheets(), true);

      this.getGameFile().getSounds().parallelStream().forEach(Resources.sounds()::load);

      log.log(Level.INFO, "{0} maps loaded from {1}", new Object[] { this.getGameFile().getMaps().size(), this.currentResourceFile });
      log.log(Level.INFO, "{0} spritesheets loaded from {1}", new Object[] { this.getGameFile().getSpriteSheets().size(), this.currentResourceFile });
      log.log(Level.INFO, "{0} tilesets loaded from {1}", new Object[] { this.getGameFile().getTilesets().size(), this.currentResourceFile });
      log.log(Level.INFO, "{0} emitters loaded from {1}", new Object[] { this.getGameFile().getEmitters().size(), this.currentResourceFile });
      log.log(Level.INFO, "{0} blueprints loaded from {1}", new Object[] { this.getGameFile().getBluePrints().size(), this.currentResourceFile });
      log.log(Level.INFO, "{0} sounds loaded from {1}", new Object[] { this.getGameFile().getSounds().size(), this.currentResourceFile });

      for (TmxMap map : this.mapComponent.getMaps()) {
        this.loadSpriteSheets(map);
      }

      // load custom emitter files
      loadCustomEmitters(this.getGameFile().getEmitters());
      UI.getAssetController().refresh();

      // display first available map after loading all stuff
      // also switch to map component
      if (!this.mapComponent.getMaps().isEmpty()) {
        this.mapComponent.loadEnvironment(this.mapComponent.getMaps().get(0));
      } else {
        Game.world().unloadEnvironment();
      }

      this.gamefileLoaded();
      this.setCurrentStatus(Resources.strings().get("status_gamefile_loaded"));
    } finally {
      Game.window().cursor().set(Cursors.DEFAULT, 0, 0);
      log.log(Level.INFO, "Loading gamefile {0} took: {1} ms", new Object[] { gameFile, (System.nanoTime() - currentTime) / 1000000.0 });
      this.loading = false;
    }
  }

  public void onLoaded(Runnable callback) {
    this.loadedCallbacks.add(callback);
  }

  public void importSpriteFile() {

    if (EditorFileChooser.showFileDialog(SPRITE_FILE_NAME, Resources.strings().get(IMPORT_DIALOGUE, SPRITE_FILE_NAME), false, SpritesheetResource.PLAIN_TEXT_FILE_EXTENSION) == JFileChooser.APPROVE_OPTION) {
      File spriteFile = EditorFileChooser.instance().getSelectedFile();
      if (spriteFile == null) {
        return;
      }

      List<Spritesheet> loaded = Resources.spritesheets().loadFrom(spriteFile.toString());
      List<SpritesheetResource> infos = new ArrayList<>();
      for (Spritesheet sprite : loaded) {
        SpritesheetResource info = new SpritesheetResource(sprite);
        infos.add(info);
        this.getGameFile().getSpriteSheets().removeIf(x -> x.getName().equals(info.getName()));
        this.getGameFile().getSpriteSheets().add(info);
      }

      this.loadSpriteSheets(infos, true);
    }
  }

  public void importSpriteSheets() {
    if (EditorFileChooser.showFileDialog(SPRITESHEET_FILE_NAME, Resources.strings().get(IMPORT_DIALOGUE, SPRITE_FILE_NAME), true, ImageFormat.getAllExtensions()) == JFileChooser.APPROVE_OPTION) {
      this.importSpriteSheets(EditorFileChooser.instance().getSelectedFiles());
    }
  }

  public void importSounds() {
    if (EditorFileChooser.showFileDialog(AUDIO_FILE_NAME, Resources.strings().get(IMPORT_DIALOGUE, AUDIO_FILE_NAME), true, SoundFormat.getAllExtensions()) == JFileChooser.APPROVE_OPTION) {
      this.importSounds(EditorFileChooser.instance().getSelectedFiles());
    }
  }

  public void exportSpriteSheets() {
    if (Resources.spritesheets().getAll().isEmpty()) {
      return;
    }
    final String source = this.getProjectPath();
    JFileChooser chooser = null;
    int result = -1;
    try {
      chooser = new JFileChooser(source != null ? source : new File(".").getCanonicalPath());
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      chooser.setDialogType(JFileChooser.SAVE_DIALOG);
      FileFilter filter = new FileNameExtensionFilter(SPRITE_FILE_NAME, "info");
      chooser.setFileFilter(filter);
      chooser.addChoosableFileFilter(filter);
      chooser.setSelectedFile(new File("sprites.info"));

      result = chooser.showSaveDialog(Game.window().getHostControl());

    } catch (IOException e1) {
      log.log(Level.SEVERE, e1.getLocalizedMessage(), e1);
    }
    if (chooser == null || result != JFileChooser.APPROVE_OPTION) {
      return;
    }
    // get all spritesheets
    List<Spritesheet> allSpriteSheets = new ArrayList<>(Resources.spritesheets().getAll());
    Collections.sort(allSpriteSheets);
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(chooser.getSelectedFile()));) {
      // print the spritesheet information to the info file
      for (Spritesheet spritesheet : allSpriteSheets) {
        // check for keyframes
        int[] keyFrames = Resources.spritesheets().getCustomKeyFrameDurations(spritesheet);
        String fileExtension = spritesheet.getImageFormat() == ImageFormat.UNSUPPORTED ? "" : spritesheet.getImageFormat().toFileExtension();
        writer.write(String.format("%s%s,%d,%d", spritesheet.getName(), fileExtension, spritesheet.getSpriteWidth(), spritesheet.getSpriteHeight()));
        // print keyframes (if they exist)
        if (keyFrames.length > 0) {
          writer.write(";");
          writer.write(ArrayUtilities.join(keyFrames));
        }
        writer.write("\n");
      }
      log.log(Level.INFO, "Exported Spritesheet metadata to {0}", new Object[] { chooser.getSelectedFile().getCanonicalPath() });
    } catch (IOException e2) {
      log.log(Level.SEVERE, e2.getLocalizedMessage(), e2);
    }
  }

  public void importTextureAtlas() {
    if (EditorFileChooser.showFileDialog(TEXTUREATLAS_FILE_NAME, Resources.strings().get(IMPORT_DIALOGUE, TEXTUREATLAS_FILE_NAME), false, "xml") == JFileChooser.APPROVE_OPTION) {
      TextureAtlas atlas = TextureAtlas.read(EditorFileChooser.instance().getSelectedFile().getAbsolutePath());
      if (atlas == null) {
        return;
      }

      Resources.images().load(atlas);
      importSpriteSheets(atlas);
    }
  }

  public void importSpriteSheets(File... files) {
    SpritesheetImportPanel spritePanel = new SpritesheetImportPanel(files);
    this.processSpritesheets(spritePanel);
  }

  private void importSounds(File... selectedFiles) {
    for (File file : selectedFiles) {
      try (InputStream stream = new FileInputStream(file.getAbsolutePath())) {
        SoundFormat format = SoundFormat.get(FileUtilities.getExtension(file));
        SoundResource resource = new SoundResource(new BufferedInputStream(stream), FileUtilities.getFileName(file.getName()), format);
        this.getGameFile().getSounds().removeIf(x -> x.getName().equals(resource.getName()));
        this.getGameFile().getSounds().add(resource);
        log.log(Level.INFO, "imported sound {0}", new Object[] { resource.getName() });
      } catch (IOException | UnsupportedAudioFileException e) {
        log.log(Level.SEVERE, e.getMessage(), e);
      }
    }
  }

  public void importSpriteSheets(TextureAtlas atlas) {
    SpritesheetImportPanel spritePanel = new SpritesheetImportPanel(atlas);
    this.processSpritesheets(spritePanel);
  }

  private void processSpritesheets(SpritesheetImportPanel spritePanel) {
    int option = JOptionPane.showConfirmDialog(Game.window().getRenderComponent(), spritePanel, Resources.strings().get("menu_assets_editSprite"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (option != JOptionPane.OK_OPTION) {
      return;
    }

    // TODO: somehow improve this to allow keeping the animation frames and only
    // update the image
    Collection<SpritesheetResource> sprites = spritePanel.getSpriteSheets();
    for (SpritesheetResource info : sprites) {
      Resources.spritesheets().getAll().removeIf(x -> x.getName().equals(info.getName() + "-preview"));
      this.getGameFile().getSpriteSheets().removeIf(x -> x.getName().equals(info.getName()));
      this.getGameFile().getSpriteSheets().add(info);
      log.log(Level.INFO, "imported spritesheet {0}", new Object[] { info.getName() });
    }

    this.loadSpriteSheets(sprites, true);
  }

  public void importEmitters() {
    XmlImportDialog.importXml("Emitter", file -> {
      EmitterData emitter;
      try {
        emitter = XmlUtilities.read(EmitterData.class, file.toURI().toURL());
      } catch (IOException | JAXBException e) {
        log.log(Level.SEVERE, String.format("could not load emitter data from %s", file), e);
        return;
      }

      if (this.gameFile.getEmitters().stream().anyMatch(x -> x.getName().equals(emitter.getName()))) {
        if (!ConfirmDialog.show(Resources.strings().get("import_emitter_title"), Resources.strings().get("import_emitter_question", emitter.getName()))) {
          return;
        }

        this.gameFile.getEmitters().removeIf(x -> x.getName().equals(emitter.getName()));
      }

      this.gameFile.getEmitters().add(emitter);
      log.log(Level.INFO, "imported emitter {0} from {1}", new Object[] { emitter.getName(), file });
    });
  }

  public void importBlueprints() {
    XmlImportDialog.importXml("Blueprint", file -> {
      Blueprint blueprint;
      try {
        blueprint = XmlUtilities.read(Blueprint.class, file.toURI().toURL());
      } catch (IOException | JAXBException e) {
        log.log(Level.SEVERE, String.format("could not load blueprint from %s", file), e);
        return;
      }
      if (blueprint == null) {
        return;
      }

      if (blueprint.getName() == null || blueprint.getName().isEmpty()) {
        blueprint.setName(FileUtilities.getFileName(file.getPath()));
      }

      if (this.gameFile.getBluePrints().stream().anyMatch(x -> x.getName().equals(blueprint.getName())) && !ConfirmDialog.show(Resources.strings().get("import_blueprint_title"), Resources.strings().get("import_blueprint_question", blueprint.getName()))) {
        return;
      }

      this.gameFile.getBluePrints().add(blueprint);
      log.log(Level.INFO, "imported blueprint {0} from {1}", new Object[] { blueprint.getName(), file });

    }, Blueprint.BLUEPRINT_FILE_EXTENSION, Blueprint.TEMPLATE_FILE_EXTENSION);
  }

  public void importTilesets() {
    XmlImportDialog.importXml("Tilesets", file -> {
      Tileset tileset;
      try {
        URL path = file.toURI().toURL();
        tileset = XmlUtilities.read(Tileset.class, path);
        tileset.finish(path);
      } catch (IOException | JAXBException e) {
        log.log(Level.SEVERE, String.format("could not load tileset from %s", file), e);
        return;
      }

      if (this.gameFile.getTilesets().stream().anyMatch(x -> x.getName().equals(tileset.getName())) && !ConfirmDialog.show(Resources.strings().get("import_tileset_title"), Resources.strings().get("import_tileset_title", tileset.getName()))) {
        return;
      }

      loadTileset(tileset, false);

      log.log(Level.INFO, "imported tileset {0} from {1}", new Object[] { tileset.getName(), file });
    }, Tileset.FILE_EXTENSION);
  }

  public boolean isLoading() {
    return this.loading;
  }

  public void loadSpriteSheets(Collection<SpritesheetResource> infos, boolean forceAssetTreeUpdate) {
    infos.parallelStream().forEach(info -> {
      Spritesheet opt = Resources.spritesheets().get(info.getName());
      if (opt != null) {
        Resources.spritesheets().update(info);
      } else {
        Resources.spritesheets().load(info);
      }
    });

    if (this.loading) {
      return;
    }

    Resources.images().clear();
    this.getMapComponent().reloadEnvironment();

    if (forceAssetTreeUpdate) {
      UI.getAssetController().refresh();
    }
  }

  public void loadTileset(ITileset tileset, boolean embedded) {
    if (tileset == null) {
      return;
    }
    Spritesheet sprite = Resources.spritesheets().get(tileset.getImage().getSource());
    if (sprite != null) {
      Resources.spritesheets().remove(sprite.getName());
      getGameFile().getSpriteSheets().removeIf(x -> x.getName().equals(sprite.getName()));
    }

    Spritesheet newSprite = Resources.spritesheets().load(tileset);
    SpritesheetResource info = new SpritesheetResource(newSprite);
    getGameFile().getSpriteSheets().removeIf(x -> x.getName().equals(info.getName()));
    getGameFile().getSpriteSheets().add(info);
    loadSpriteSheets(Arrays.asList(info), true);
    if (!embedded) {
      getGameFile().getTilesets().removeIf(x -> x.getName().equals(tileset.getName()));
      getGameFile().getTilesets().add((Tileset) tileset);
    }
  }

  public void save(boolean selectFile) {
    this.updateGameFileMaps();

    if (this.getGameFile() == null) {
      return;
    }

    if (this.currentResourceFile == null || selectFile) {
      JFileChooser chooser;
      try {
        final String source = this.getProjectPath();
        chooser = new JFileChooser(source != null ? source : new File(".").getCanonicalPath());
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        FileFilter filter = new FileNameExtensionFilter(GAME_FILE_NAME, ResourceBundle.FILE_EXTENSION);
        chooser.setFileFilter(filter);
        chooser.addChoosableFileFilter(filter);
        chooser.setSelectedFile(new File(DEFAULT_GAME_NAME + "." + ResourceBundle.FILE_EXTENSION));

        int result = chooser.showSaveDialog(Game.window().getHostControl());
        if (result == JFileChooser.APPROVE_OPTION) {
          this.saveGameFile(chooser.getSelectedFile().toString());
        }
      } catch (IOException e1) {
        log.log(Level.SEVERE, e1.getLocalizedMessage(), e1);
      }
    } else {
      this.saveGameFile(this.currentResourceFile);
    }
  }

  public void revert() {
    if (this.currentResourceFile == null || this.currentResourceFile.isEmpty()) {
      return;
    }

    boolean revert = UI.showRevertWarning();
    if (!revert) {
      return;
    }

    File currentFile = new File(this.currentResourceFile);
    TmxMap currentMapSelection = null;
    if (UI.getMapController().getCurrentMap() != null) {
      currentMapSelection = UI.getMapController().getCurrentMap();
    }

    this.close(true);
    this.load(currentFile, true);
    UI.getMapController().setSelection(currentMapSelection);
    log.log(Level.INFO, "Reverted all pending changes.");
  }

  public MapComponent getMapComponent() {
    return this.mapComponent;
  }

  public String getCurrentResourceFile() {
    return this.currentResourceFile;
  }

  public String getCurrentStatus() {
    return currentStatus;
  }

  public List<TmxMap> getChangedMaps() {
    return this.getMapComponent().getMaps().stream().filter(UndoManager::hasChanges).distinct().collect(Collectors.toList());
  }

  public void setCurrentStatus(String currentStatus) {
    this.currentStatus = currentStatus;
    this.statusTick = Game.time().now();
  }

  public void updateGameFileMaps() {
    this.getGameFile().getMaps().clear();
    for (TmxMap map : this.mapComponent.getMaps()) {
      this.getGameFile().getMaps().add(map);
    }

    UI.getAssetController().refresh();
  }

  public boolean isUnsavedProject() {
    return this.getCurrentResourceFile() == null && this.getProjectPath() != null;
  }

  private String saveGameFile(String target) {
    String saveFile = this.getGameFile().save(target, preferences().compressFile());
    this.currentResourceFile = saveFile;
    preferences().setLastGameFile(this.currentResourceFile);
    preferences().addOpenedFile(this.currentResourceFile);
    this.gamefileLoaded();
    log.log(Level.INFO, "saved {0} maps, {1} spritesheets, {2} tilesets, {3} emitters, {4} blueprints, {5} sounds to {6}",
        new Object[] { this.getGameFile().getMaps().size(), this.getGameFile().getSpriteSheets().size(), this.getGameFile().getTilesets().size(), this.getGameFile().getEmitters().size(), this.getGameFile().getBluePrints().size(), this.getGameFile().getSounds().size(), this.currentResourceFile });
    this.setCurrentStatus(Resources.strings().get("status_gamefile_saved"));

    this.saveMaps();
    return saveFile;
  }

  private void saveMaps() {
    for (TmxMap map : this.getChangedMaps()) {
      UndoManager.save(map);

      if (preferences().syncMaps()) {
        for (String file : FileUtilities.findFilesByExtension(new ArrayList<>(), Paths.get(FileUtilities.combine(this.getProjectPath(), "maps")), map.getName() + "." + TmxMap.FILE_EXTENSION)) {
          File newFile = XmlUtilities.save(map, file, TmxMap.FILE_EXTENSION);
          log.log(Level.INFO, "synchronized map {0}", new Object[] { newFile });
        }
      }
    }
  }

  private static void loadCustomEmitters(List<EmitterData> emitters) {
    for (EmitterData emitterData : emitters) {
      EmitterLoader.load(emitterData);
    }
  }

  private void loadSpriteSheets(TmxMap map) {
    List<SpritesheetResource> infos = new ArrayList<>();
    int cnt = 0;
    for (ITileset tileSet : map.getTilesets()) {
      if (tileSet.getImage() == null) {
        continue;
      }

      Spritesheet opt = Resources.spritesheets().get(tileSet.getImage().getSource());
      Spritesheet sprite = null;
      if (opt == null) {
        sprite = Resources.spritesheets().load(tileSet);
        if (sprite == null) {
          continue;
        }
      } else {
        sprite = opt;
      }

      infos.add(new SpritesheetResource(sprite));
      cnt++;
    }

    for (IImageLayer imageLayer : map.getImageLayers()) {
      Spritesheet opt = Resources.spritesheets().get(imageLayer.getImage().getSource());
      Spritesheet sprite = null;
      if (opt == null) {
        BufferedImage img = Resources.images().get(imageLayer.getImage().getAbsoluteSourcePath(), true);
        if (img == null) {
          continue;
        }

        sprite = Resources.spritesheets().load(img, imageLayer.getImage().getSource(), img.getWidth(), img.getHeight());
        if (sprite == null) {
          continue;
        }
      } else {
        sprite = opt;
      }

      SpritesheetResource info = new SpritesheetResource(sprite);
      infos.add(info);
      this.getGameFile().getSpriteSheets().removeIf(x -> x.getName().equals(info.getName()));
      this.getGameFile().getSpriteSheets().add(info);
      cnt++;
    }

    this.loadSpriteSheets(infos, false);
    for (SpritesheetResource info : infos) {
      if (this.getGameFile().getSpriteSheets().stream().noneMatch(x -> x.getName().equals(info.getName()))) {
        this.getGameFile().getSpriteSheets().add(info);
      }
    }

    if (cnt > 0) {
      log.log(Level.INFO, "{0} tilesets loaded from {1}", new Object[] { cnt, map.getName() });
    }
  }

  private void gamefileLoaded() {
    preferences().setLastGameFile(this.currentResourceFile);
    preferences().addOpenedFile(this.currentResourceFile);
    for (Runnable callback : this.loadedCallbacks) {
      callback.run();
    }
  }
}