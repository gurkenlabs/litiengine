package de.gurkenlabs.utiliti.controller;

import static de.gurkenlabs.utiliti.model.constants.EditorConstants.AUDIO_FILE_NAME;
import static de.gurkenlabs.utiliti.model.constants.EditorConstants.ANIMATION_FILE_NAME;
import static de.gurkenlabs.utiliti.model.constants.EditorConstants.GAME_FILE_NAME;
import static de.gurkenlabs.utiliti.model.constants.EditorConstants.IMPORT_DIALOGUE;
import static de.gurkenlabs.utiliti.model.constants.EditorConstants.NEW_GAME_STRING;
import static de.gurkenlabs.utiliti.model.constants.EditorConstants.SPRITESHEET_FILE_NAME;
import static de.gurkenlabs.utiliti.model.constants.EditorConstants.SPRITE_FILE_NAME;
import static de.gurkenlabs.utiliti.model.constants.EditorConstants.STATUS_DURATION;
import static de.gurkenlabs.utiliti.model.constants.EditorConstants.TEXTUREATLAS_FILE_NAME;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IImageLayer;
import de.gurkenlabs.litiengine.environment.tilemap.ITileset;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Blueprint;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.emitters.xml.EmitterAttributes;
import de.gurkenlabs.litiengine.graphics.emitters.xml.EmitterLoader;
import de.gurkenlabs.litiengine.gui.screens.Screen;
import de.gurkenlabs.litiengine.resources.ImageFormat;
import de.gurkenlabs.litiengine.resources.ResourceBundle;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.resources.SoundFormat;
import de.gurkenlabs.litiengine.resources.SoundResource;
import de.gurkenlabs.litiengine.resources.SpritesheetResource;
import de.gurkenlabs.litiengine.resources.TextureAtlas;
import de.gurkenlabs.litiengine.util.io.FileUtilities;
import de.gurkenlabs.litiengine.util.io.XmlUtilities;
import de.gurkenlabs.utiliti.model.Cursors;
import de.gurkenlabs.utiliti.model.Style;
import de.gurkenlabs.utiliti.model.UserPreferences;
import de.gurkenlabs.utiliti.controller.tool.ToolManager;
import de.gurkenlabs.utiliti.view.components.SpritesheetImportPanel;
import de.gurkenlabs.utiliti.view.renderers.WorkspaceRenderer;
import de.gurkenlabs.utiliti.view.components.Tray;
import de.gurkenlabs.utiliti.view.components.UI;
import de.gurkenlabs.utiliti.view.dialogs.ConfirmDialog;
import de.gurkenlabs.utiliti.view.dialogs.EditorFileChooser;
import de.gurkenlabs.utiliti.view.dialogs.EditorFileSaver;
import de.gurkenlabs.utiliti.view.dialogs.XmlImportDialog;
import jakarta.xml.bind.JAXBException;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Editor extends Screen {
  private static final Logger log = Logger.getLogger(Editor.class.getName());


  private static Editor instance;
  private static UserPreferences preferences;

  private final List<Runnable> loadedCallbacks;

  private final MapComponent mapComponent;
  private ResourceBundle gameFile = new ResourceBundle();
  private Path projectPath;
  private final ProjectCodeIntegration projectCodeIntegration = new ProjectCodeIntegration();
  private Path currentResourceFile;

  private long statusTick;
  private String currentStatus;
  private String displayedWindowTitle;
  private String displayedTrayTooltip;
  private String displayedMapName;
  private final AtomicBoolean windowMetadataDirty = new AtomicBoolean(true);
  private boolean loading;

  private Editor() {
    super("Editor");
    this.loadedCallbacks = new CopyOnWriteArrayList<>();
    this.mapComponent = new MapComponent();
    this.mapComponent.onMapLoaded(map -> this.windowMetadataDirty.set(true));
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

  @Override public void prepare() {
    this.getComponents().add(this.mapComponent);
    super.prepare();
  }

  @Override public void render(final Graphics2D g) {
    renderWorkspace(g);

    String mapName = Game.world().environment() != null && Game.world().environment().getMap() != null
      ? Game.world().environment().getMap().getName()
      : null;
    if (!Objects.equals(this.displayedMapName, mapName)) {
      this.windowMetadataDirty.set(true);
    }
    if (this.windowMetadataDirty.getAndSet(false)) {
      updateWindowMetadata(mapName);
    }

    super.render(g);
    WorkspaceRenderer.renderMapBounds(g);
  }

  /**
   * Renders the editor canvas without the surrounding Swing UI.
   *
   * <p>This mirrors the visible canvas pipeline: workspace background, map layers and entities,
   * editor overlays, and map bounds.
   */
  public void renderCanvas(Graphics2D g) {
    renderWorkspace(g);
    this.mapComponent.render(g);
    WorkspaceRenderer.renderMapBounds(g);
  }

  private static void renderWorkspace(Graphics2D g) {
    WorkspaceRenderer.renderBackground(g);
    if (Game.world().environment() != null) {
      Game.world().environment().render(g);
    }
  }

  private void updateWindowMetadata(String mapName) {
    String title;
    String tooltip;
    if (this.currentResourceFile != null) {
      title = Game.info().getName() + " " + Game.info().getVersion() + " - " + this.currentResourceFile;
      String mapDescription = mapName != null ? "\n" + Resources.strings().get("tray_map", mapName) : "";
      tooltip = Game.info().getName() + " " + Game.info().getVersion()
        + "\n" + this.currentResourceFile + mapDescription;
    } else if (this.getProjectPath() != null) {
      title = Game.info().getTitle() + " - " + NEW_GAME_STRING;
      tooltip = Game.info().getTitle() + "\n" + NEW_GAME_STRING;
    } else {
      title = Game.info().getTitle();
      tooltip = title;
    }

    if (!Objects.equals(this.displayedWindowTitle, title)) {
      Game.window().setTitle(title);
      this.displayedWindowTitle = title;
    }
    if (!Objects.equals(this.displayedTrayTooltip, tooltip)) {
      Tray.setToolTip(tooltip);
      this.displayedTrayTooltip = tooltip;
    }
    this.displayedMapName = mapName;
  }

  public ResourceBundle getGameFile() {
    return this.gameFile;
  }

  public Path getProjectPath() {
    return projectPath;
  }

  public ProjectCodeIntegration getProjectCodeIntegration() {
    return projectCodeIntegration;
  }

  public void setProjectPath(Path projectPath) {
    this.projectPath = projectPath;
    this.windowMetadataDirty.set(true);
  }

  public void create() {
    JFileChooser chooser;
    try {
      chooser = new JFileChooser(new File(".").getCanonicalPath());
      chooser.setDialogTitle(Resources.strings().get("input_create_new_project"));
      chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      if (chooser.showOpenDialog(Game.window().getHostControl()) != JFileChooser.APPROVE_OPTION) {
        return;
      }

      if (Game.world().environment() != null) {
        Game.world().unloadEnvironment();
      }

      // set up project settings
      this.setProjectPath(chooser.getSelectedFile().toPath());

      // load all maps in the directory
      this.mapComponent.loadMaps(getProjectPath());
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
        this.mapComponent.loadEnvironment(this.mapComponent.getMaps().getFirst());
      }

      this.gamefileLoaded();
      this.save(true);
    } catch (IOException e) {
      log.log(Level.SEVERE, e.getLocalizedMessage(), e);
    }

    this.setCurrentStatus(Resources.strings().get("status_project_created"));
  }

  public void load() {
    if (EditorFileChooser.showFileDialog(ResourceBundle.FILE_EXTENSION, GAME_FILE_NAME, false, ResourceBundle.FILE_EXTENSION)
      == JFileChooser.APPROVE_OPTION) {
      this.load(EditorFileChooser.instance().getSelectedFile().toPath(), false);
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
    ToolManager.instance().clearSelections();
    UndoManager.clearAll();
    getMapComponent().clearAll();
    UI.clearInspector();
    this.currentResourceFile = null;
    this.projectCodeIntegration.close();
    this.setProjectPath(null);
    this.mapComponent.loadMaps(List.of(), true);
    Resources.clearAll();
    this.gameFile = null;
    UI.getAssetController().refresh();
    this.setCurrentStatus(Resources.strings().get("status_gamefile_closed"));
  }

  public void load(Path gameFile, boolean force) {
    if (gameFile == null) {
      log.warning("Cannot load a project without a file path");
      return;
    }
    if (!force) {
      boolean proceedLoading = UI.notifyPendingChanges();
      if (!proceedLoading) {
        return;
      }
    }

    AutoSaveManager.checkForRecovery(gameFile);

    if (!Files.exists(gameFile)) {
      log.log(Level.SEVERE, "gameFile {0} does not exist", gameFile);
      return;
    }

    if (!FileUtilities.getExtension(gameFile).equals(ResourceBundle.FILE_EXTENSION)) {
      log.log(Level.SEVERE, "[{0}] unsupported file format [{1}]", new Object[] {gameFile, FileUtilities.getExtension(gameFile)});
      return;
    }

    final long currentTime = System.nanoTime();
    Cursors.apply(Cursors.LOAD);

    this.loading = true;
    try {
      UndoManager.clearAll();
      ToolManager.instance().clearSelections();

      ResourceBundle loadedGameFile = ResourceBundle.load(gameFile.toString());
      if (loadedGameFile == null) {
        throw new IllegalArgumentException("The game file " + gameFile + " could not be loaded!");
      }

      // Replace the current project only after the new resource bundle was parsed successfully.
      this.currentResourceFile = gameFile;
      this.gameFile = loadedGameFile;
      this.setProjectPath(gameFile);
      this.loadProjectTilesetTerrains(gameFile.getParent());
      this.projectCodeIntegration.reload(gameFile);

      // load maps from game file
      this.mapComponent.loadMaps(this.getGameFile().getMaps(), true);

      Resources.images().clear();
      Resources.spritesheets().clear();

      // load sprite sheets from different sources:
      // 1. add sprite sheets from game file
      // 2. add sprite sheets by tile sets of all maps in the game file
      this.loadSpriteSheets(this.getGameFile().getSpriteSheets(), true);

      this.getGameFile().getSounds().parallelStream().forEach(Resources.sounds()::load);

      log.log(Level.INFO, "{0} maps loaded from {1}", new Object[] {this.getGameFile().getMaps().size(), this.currentResourceFile});
      log.log(Level.INFO, "{0} spritesheets loaded from {1}", new Object[] {this.getGameFile().getSpriteSheets().size(), this.currentResourceFile});
      log.log(Level.INFO, "{0} tilesets loaded from {1}", new Object[] {this.getGameFile().getTilesets().size(), this.currentResourceFile});
      log.log(Level.INFO, "{0} emitters loaded from {1}", new Object[] {this.getGameFile().getEmitters().size(), this.currentResourceFile});
      log.log(Level.INFO, "{0} blueprints loaded from {1}", new Object[] {this.getGameFile().getBluePrints().size(), this.currentResourceFile});
      log.log(Level.INFO, "{0} sounds loaded from {1}", new Object[] {this.getGameFile().getSounds().size(), this.currentResourceFile});

      for (TmxMap map : this.mapComponent.getMaps()) {
        this.loadSpriteSheets(map);
      }

      // load custom emitter files
      loadCustomEmitters(this.getGameFile().getEmitters());
      UI.getAssetController().refresh();

      // display first available map after loading all stuff
      // also switch to map component
      if (!this.mapComponent.getMaps().isEmpty()) {
        this.mapComponent.loadEnvironment(this.mapComponent.getMaps().getFirst());
      } else {
        Game.world().unloadEnvironment();
      }

      this.gamefileLoaded();
      this.setCurrentStatus(Resources.strings().get("status_gamefile_loaded"));
    } finally {
      Cursors.apply(Cursors.DEFAULT);
      log.log(Level.INFO, "Loading gamefile {0} took: {1} ms", new Object[] {gameFile, (System.nanoTime() - currentTime) / 1000000.0});
      this.loading = false;
    }
  }

  public void loadAsync(Path gameFile, boolean force) {
    this.loadAsync(gameFile, force, null);
  }

  public void loadAsync(Path gameFile, boolean force, Runnable onComplete) {
    if (!force) {
      boolean proceedLoading = UI.notifyPendingChanges();
      if (!proceedLoading) {
        return;
      }
    }

    if (!Files.exists(gameFile)) {
      log.log(Level.SEVERE, "gameFile {0} does not exist", gameFile);
      return;
    }

    if (!FileUtilities.getExtension(gameFile).equals(ResourceBundle.FILE_EXTENSION)) {
      log.log(Level.SEVERE, "[{0}] unsupported file format [{1}]", new Object[] {gameFile, FileUtilities.getExtension(gameFile)});
      return;
    }

    final long currentTime = System.nanoTime();
    Cursors.apply(Cursors.LOAD);
    this.loading = true;

    CompletableFuture.supplyAsync(() -> {
      ResourceBundle loadedBundle = ResourceBundle.load(gameFile.toString());
      if (loadedBundle == null) {
        throw new IllegalArgumentException("The game file " + gameFile + " could not be loaded!");
      }
      return loadedBundle;
    }).thenAcceptAsync(loadedBundle -> {
      try {
        UndoManager.clearAll();
        ToolManager.instance().clearSelections();

        this.currentResourceFile = gameFile;
        this.gameFile = loadedBundle;

        this.setProjectPath(gameFile);
        this.loadProjectTilesetTerrains(gameFile.getParent());
        this.projectCodeIntegration.reload(gameFile);

        this.mapComponent.loadMaps(this.getGameFile().getMaps(), true);

        Resources.images().clear();
        Resources.spritesheets().clear();

        this.loadSpriteSheets(this.getGameFile().getSpriteSheets(), true);
        this.getGameFile().getSounds().parallelStream().forEach(Resources.sounds()::load);

        log.log(Level.INFO, "{0} maps loaded from {1}", new Object[] {this.getGameFile().getMaps().size(), this.currentResourceFile});
        log.log(Level.INFO, "{0} spritesheets loaded from {1}", new Object[] {this.getGameFile().getSpriteSheets().size(), this.currentResourceFile});
        log.log(Level.INFO, "{0} tilesets loaded from {1}", new Object[] {this.getGameFile().getTilesets().size(), this.currentResourceFile});
        log.log(Level.INFO, "{0} emitters loaded from {1}", new Object[] {this.getGameFile().getEmitters().size(), this.currentResourceFile});
        log.log(Level.INFO, "{0} blueprints loaded from {1}", new Object[] {this.getGameFile().getBluePrints().size(), this.currentResourceFile});
        log.log(Level.INFO, "{0} sounds loaded from {1}", new Object[] {this.getGameFile().getSounds().size(), this.currentResourceFile});

        for (TmxMap map : this.mapComponent.getMaps()) {
          this.loadSpriteSheets(map);
        }

        loadCustomEmitters(this.getGameFile().getEmitters());
        UI.getAssetController().refresh();

        if (!this.mapComponent.getMaps().isEmpty()) {
          this.mapComponent.loadEnvironment(this.mapComponent.getMaps().getFirst());
        } else {
          Game.world().unloadEnvironment();
        }

        this.gamefileLoaded();
        this.setCurrentStatus(Resources.strings().get("status_gamefile_loaded"));
        if (onComplete != null) {
          onComplete.run();
        }
      } finally {
        Cursors.apply(Cursors.DEFAULT);
        log.log(Level.INFO, "Loading gamefile {0} took: {1} ms", new Object[] {gameFile, (System.nanoTime() - currentTime) / 1000000.0});
        this.loading = false;
      }
    }, javax.swing.SwingUtilities::invokeLater)
    .exceptionally(throwable -> {
      Cursors.apply(Cursors.DEFAULT);
      this.loading = false;
      log.log(Level.SEVERE, "Failed to load game file " + gameFile, throwable);
      return null;
    });
  }

  public void onLoaded(Runnable callback) {
    this.loadedCallbacks.add(callback);
  }

  public void importSpriteFile() {
    if (EditorFileChooser.showFileDialog(SPRITE_FILE_NAME, Resources.strings().get(IMPORT_DIALOGUE, SPRITE_FILE_NAME), false,
      SpritesheetResource.PLAIN_TEXT_FILE_EXTENSION) == JFileChooser.APPROVE_OPTION) {
      File spriteFile = EditorFileChooser.instance().getSelectedFile();
      if (spriteFile == null) {
        return;
      }

      this.importSpriteFile(spriteFile.toPath());
    }
  }

  public void importSpriteFile(Path... files) {
    List<SpritesheetResource> infos = new ArrayList<>();
    for (Path file : files) {
      List<Spritesheet> loaded = Resources.spritesheets().loadFrom(file.toString());
      for (Spritesheet sprite : loaded) {
        SpritesheetResource info = new SpritesheetResource(sprite);
        infos.add(info);
        this.getGameFile().getSpriteSheets().removeIf(x -> x.getName().equals(info.getName()));
        this.getGameFile().getSpriteSheets().add(info);
      }

    }
    this.loadSpriteSheets(infos, true);
  }

  public void importSpriteSheets() {
    if (EditorFileChooser.showFileDialog(SPRITESHEET_FILE_NAME, Resources.strings().get(IMPORT_DIALOGUE, SPRITESHEET_FILE_NAME), true,
      ImageFormat.getAllExtensions()) == JFileChooser.APPROVE_OPTION) {
      this.importSpriteSheets(Stream.of(EditorFileChooser.instance().getSelectedFiles()).map(File::toPath).toArray(Path[]::new));
    }
  }

  public void importSounds() {
    if (EditorFileChooser.showFileDialog(AUDIO_FILE_NAME, Resources.strings().get(IMPORT_DIALOGUE, AUDIO_FILE_NAME), true,
      SoundFormat.getAllExtensions()) == JFileChooser.APPROVE_OPTION) {
      this.importSounds(Stream.of(EditorFileChooser.instance().getSelectedFiles()).map(File::toPath).toArray(Path[]::new));
    }
  }

  /**
   * Opens a file chooser dialog so the user can pick one or more Aseprite-exported JSON files and
   * imports the animations they describe into the editor.
   */
  public void importAnimations() {
    if (EditorFileChooser.showFileDialog(ANIMATION_FILE_NAME, Resources.strings().get(IMPORT_DIALOGUE, ANIMATION_FILE_NAME), true,
      "json") == JFileChooser.APPROVE_OPTION) {
      this.importAnimations(Stream.of(EditorFileChooser.instance().getSelectedFiles()).map(File::toPath).toArray(Path[]::new));
    }
  }

  /**
   * Imports the given Aseprite JSON animation files into the engine's {@link
   * de.gurkenlabs.litiengine.resources.Animations} resource container and refreshes the asset
   * panel.
   *
   * @param files One or more paths pointing to Aseprite JSON sidecar files.
   */
  public void importAnimations(Path... files) {
    if (files == null || files.length == 0) {
      return;
    }
    for (Path file : files) {
      try {
        var animation = Resources.animations().importAseprite(file);
        log.log(Level.INFO, "imported animation {0} from {1}", new Object[] {animation.getName(), file});
      } catch (IOException | RuntimeException e) {
        log.log(Level.SEVERE, "could not import animation from " + file + ": " + e.getMessage(), e);
      }
    }
    UI.getAssetController().refresh();
  }

  public void exportSpriteFile() {
    if (Resources.spritesheets().getAll().isEmpty()) {
      return;
    }

    final Path source = getProjectPath();
    JFileChooser chooser = new JFileChooser(source.toFile());
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setDialogType(JFileChooser.SAVE_DIALOG);
    FileFilter filter = new FileNameExtensionFilter(SPRITE_FILE_NAME, "info");
    chooser.setFileFilter(filter);
    chooser.addChoosableFileFilter(filter);
    chooser.setSelectedFile(new File("sprites.info"));

    int result = chooser.showSaveDialog(Game.window().getHostControl());

    if (result != JFileChooser.APPROVE_OPTION) {
      return;
    }

    int res = JOptionPane.showConfirmDialog(Game.window().getRenderComponent(), Resources.strings().get("menu_export_spriteSheets_withResources"),
      Resources.strings().get("menu_export_spriteSheets_withResources"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

    Resources.spritesheets().saveTo(chooser.getSelectedFile().getAbsolutePath(), res == JOptionPane.NO_OPTION);
  }

  public void importTextureAtlas() {
    if (EditorFileChooser.showFileDialog(TEXTUREATLAS_FILE_NAME, Resources.strings().get(IMPORT_DIALOGUE, TEXTUREATLAS_FILE_NAME), false, "xml")
      == JFileChooser.APPROVE_OPTION) {
      TextureAtlas atlas = TextureAtlas.read(EditorFileChooser.instance().getSelectedFile().getAbsolutePath());
      if (atlas == null) {
        return;
      }

      Resources.images().load(atlas);
      importSpriteSheets(atlas);
    }
  }

  public void importSpriteSheets(Path... files) {
    SpritesheetImportPanel spritePanel = new SpritesheetImportPanel(files);
    this.processSpritesheets(spritePanel);
  }

  public void importSpriteSheets(SpritesheetResource... resources) {
    SpritesheetImportPanel spritePanel = new SpritesheetImportPanel(resources);
    this.processSpritesheets(spritePanel);
  }

  public void importSounds(Path... selectedFiles) {
    for (Path file : selectedFiles) {
      try (InputStream stream = Files.newInputStream(file)) {
        SoundFormat format = SoundFormat.get(FileUtilities.getExtension(file));
        SoundResource resource = new SoundResource(new BufferedInputStream(stream), FileUtilities.getFileName(file.getFileName().toString()), format);
        this.getGameFile().getSounds().removeIf(x -> x.getName().equals(resource.getName()));
        this.getGameFile().getSounds().add(resource);
        Resources.sounds().load(resource);
        log.log(Level.INFO, "imported sound {0}", new Object[] {resource.getName()});
      } catch (IOException | UnsupportedAudioFileException e) {
        log.log(Level.SEVERE, e.getMessage(), e);
      }
    }

    UI.getAssetController().refresh();
  }

  public void importSpriteSheets(TextureAtlas atlas) {
    SpritesheetImportPanel spritePanel = new SpritesheetImportPanel(atlas);
    this.processSpritesheets(spritePanel);
  }

  private void processSpritesheets(SpritesheetImportPanel spritePanel) {
    try {
      int option = JOptionPane.showConfirmDialog(Game.window().getRenderComponent(), spritePanel, Resources.strings().get("menu_assets_editSprite"),
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
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
        log.log(Level.INFO, "imported spritesheet {0}", new Object[] {info.getName()});
      }

      this.loadSpriteSheets(sprites, true);
    } finally {
      spritePanel.dispose();
    }
  }

  private void loadProjectTilesetTerrains(Path projectRoot) {
    if (projectRoot == null || this.gameFile == null) {
      return;
    }
    loadProjectTilesetTerrains(this.gameFile, projectRoot);
  }

  static void loadProjectTilesetTerrains(ResourceBundle gameFile, Path projectRoot) {
    List<Tileset> sources;
    try (Stream<Path> paths = Files.walk(projectRoot)) {
      sources = paths.filter(Files::isRegularFile)
        .filter(path -> {
          String extension = FileUtilities.getExtension(path);
          return extension.equalsIgnoreCase(Tileset.FILE_EXTENSION)
            || extension.equalsIgnoreCase(TmxMap.FILE_EXTENSION);
        })
        .sorted()
        .flatMap(path -> loadProjectTilesets(path).stream())
        .filter(source -> source.getTerrainSets() != null && !source.getTerrainSets().isEmpty())
        .toList();
    } catch (IOException e) {
      log.log(Level.WARNING, "Could not search project for tilesets: {0}", e.getMessage());
      return;
    }

    List<Tileset> targets = Stream.concat(
        gameFile.getTilesets().stream(),
        gameFile.getMaps().stream().flatMap(map -> map.getTilesets().stream()).filter(Tileset.class::isInstance).map(Tileset.class::cast))
      .distinct()
      .toList();
    for (Tileset target : targets) {
      boolean hasTerrains = target.getTerrainSets() != null && !target.getTerrainSets().isEmpty();
      if (hasTerrains && !needsTerrainNameEnrichment(target)) {
        continue;
      }
      List<Tileset> matches = sources.stream().filter(source -> sameTileset(target, source)).toList();
      if (matches.isEmpty()) {
        List<Tileset> caseInsensitiveMatches = sources.stream()
          .filter(source -> sameTilesetIgnoringCase(target, source))
          .toList();
        if (caseInsensitiveMatches.size() > 1) {
          log.log(Level.WARNING, "Skipped ambiguous case-insensitive terrain definitions for tileset {0}", target.getName());
          continue;
        }
        matches = caseInsensitiveMatches;
      }
      if (matches.isEmpty()) {
        continue;
      }
      if (hasTerrains) {
        Tileset firstMatch = matches.getFirst();
        if (matches.stream().allMatch(source -> sameTerrainNames(firstMatch, source))) {
          target.enrichTerrainMetadataFrom(firstMatch);
        } else {
          log.log(Level.WARNING, "Skipped ambiguous terrain definitions for tileset {0}", target.getName());
        }
      } else {
        target.copyTerrainSetsFrom(matches.getFirst());
      }
    }
  }

  private static List<Tileset> loadProjectTilesets(Path path) {
    try {
      if (FileUtilities.getExtension(path).equalsIgnoreCase(Tileset.FILE_EXTENSION)) {
        return List.of(XmlUtilities.read(Tileset.class, path.toUri().toURL()));
      }
      TmxMap map = XmlUtilities.read(TmxMap.class, path.toUri().toURL());
      return map.getTilesets().stream().filter(Tileset.class::isInstance).map(Tileset.class::cast).toList();
    } catch (IOException | JAXBException | RuntimeException e) {
      log.log(Level.WARNING, "Could not load terrain definitions from " + path, e);
      return List.of();
    }
  }

  private static boolean needsTerrainNameEnrichment(Tileset tileset) {
    return tileset.getTerrainSets() != null && !tileset.getTerrainSets().isEmpty()
      && tileset.getTerrainSets().stream().flatMap(set -> set.getTerrains().stream())
      .anyMatch(terrain -> terrain.getName() == null || terrain.getName().matches("Terrain \\d+"));
  }

  private static boolean sameTileset(Tileset first, Tileset second) {
    if (tilesetsReferToSameFile(first, second)) {
      return true;
    }
    return Objects.equals(first.getName(), second.getName())
      && first.getTileCount() == second.getTileCount()
      && first.getTileWidth() == second.getTileWidth()
      && first.getTileHeight() == second.getTileHeight();
  }

  private static boolean sameTilesetIgnoringCase(Tileset first, Tileset second) {
    if (!Collections.disjoint(normalizedCandidateNames(first), normalizedCandidateNames(second))) {
      return true;
    }
    return first.getName() != null && second.getName() != null
      && first.getName().equalsIgnoreCase(second.getName())
      && first.getTileCount() == second.getTileCount()
      && first.getTileWidth() == second.getTileWidth()
      && first.getTileHeight() == second.getTileHeight();
  }

  static boolean tilesetsReferToSameFile(Tileset first, Tileset second) {
    return !Collections.disjoint(candidateNames(first), candidateNames(second));
  }

  private static Set<String> candidateNames(Tileset tileset) {
    Set<String> names = new HashSet<>();
    if (tileset.getName() != null) {
      names.add(tileset.getName());
    }
    if (tileset.getSource() != null) {
      names.add(FileUtilities.getFileName(tileset.getSource()));
      names.add(FileUtilities.getFileName(tileset.getSource(), false));
    }
    return names;
  }

  private static Set<String> normalizedCandidateNames(Tileset tileset) {
    return candidateNames(tileset).stream()
      .map(name -> name.toLowerCase(Locale.ROOT))
      .collect(java.util.stream.Collectors.toSet());
  }

  private static boolean sameTerrainNames(Tileset first, Tileset second) {
    List<String> firstNames = first.getTerrainSets().stream().flatMap(set -> set.getTerrains().stream()).map(terrain -> terrain.getName()).toList();
    List<String> secondNames = second.getTerrainSets().stream().flatMap(set -> set.getTerrains().stream()).map(terrain -> terrain.getName()).toList();
    return firstNames.equals(secondNames);
  }

  public void importEmitters() {
    XmlImportDialog.importXml(Resources.strings().get("resource_emitter"), this::importEmitter);
  }

  public void importEmitters(Path... files) {
    Stream.of(files).forEach(this::importEmitter);
    UI.getAssetController().refresh();
  }

  private void importEmitter(Path file) {
    EmitterAttributes emitter;
    try {
      emitter = XmlUtilities.read(EmitterAttributes.class, file.toUri().toURL());
    } catch (IOException | JAXBException e) {
      log.log(Level.SEVERE, String.format("could not load emitter data from %s", file), e);
      return;
    }

    if (this.gameFile.getEmitters().stream().anyMatch(x -> x.getName().equals(Objects.requireNonNull(emitter).getName()))) {
      if (!ConfirmDialog.show(Resources.strings().get("import_emitter_title"),
        Resources.strings().get("import_emitter_question", emitter.getName()))) {
        return;
      }

      this.gameFile.getEmitters().removeIf(x -> x.getName().equals(emitter.getName()));
    }

    this.gameFile.getEmitters().add(emitter);
    log.log(Level.INFO, "imported emitter {0} from {1}", new Object[] {Objects.requireNonNull(emitter).getName(), file});
  }

  public void importBlueprints() {
    XmlImportDialog.importXml(Resources.strings().get("resource_blueprint"), this::importBlueprint, Blueprint.BLUEPRINT_FILE_EXTENSION,
      Blueprint.TEMPLATE_FILE_EXTENSION);
  }

  public void importBlueprints(Path... files) {
    Stream.of(files).forEach(this::importBlueprint);
    UI.getAssetController().refresh();
  }

  private void importBlueprint(Path file) {
    Blueprint blueprint;
    try {
      blueprint = XmlUtilities.read(Blueprint.class, file.toUri().toURL());
    } catch (IOException | JAXBException e) {
      log.log(Level.SEVERE, String.format("could not load blueprint from %s", file), e);
      return;
    }
    if (blueprint == null) {
      return;
    }

    if (blueprint.getName() == null || blueprint.getName().isEmpty()) {
      blueprint.setName(FileUtilities.getFileName(file.getFileName().toString()));
    }

    if (this.gameFile.getBluePrints().stream().anyMatch(x -> x.getName().equals(blueprint.getName())) && !ConfirmDialog.show(
      Resources.strings().get("import_blueprint_title"), Resources.strings().get("import_blueprint_question", blueprint.getName()))) {
      return;
    }

    this.gameFile.getBluePrints().add(blueprint);
    Resources.blueprints().add(blueprint.getName(), blueprint);
    log.log(Level.INFO, "imported blueprint {0} from {1}", new Object[] {blueprint.getName(), file});
  }

  public void importTilesets() {
    XmlImportDialog.importXml(Resources.strings().get("resource_tilesets"), this::importTileset, Tileset.FILE_EXTENSION);
  }

  public void importTilesets(Path... files) {
    Stream.of(files).forEach(this::importTileset);
    UI.getAssetController().refresh();
  }

  private void importTileset(Path file) {
    Tileset tileset;
    try {
      URL path = file.toUri().toURL();
      tileset = XmlUtilities.read(Tileset.class, path);
      Objects.requireNonNull(tileset).finish(path);
    } catch (IOException | JAXBException e) {
      log.log(Level.SEVERE, String.format("could not load tileset from %s", file), e);
      return;
    }

    if (this.gameFile.getTilesets().stream().anyMatch(x -> x.getName().equals(tileset.getName())) && !ConfirmDialog.show(
      Resources.strings().get("import_tileset_title"), Resources.strings().get("import_tileset_title", tileset.getName()))) {
      return;
    }

    loadTileset(tileset, false);
    log.log(Level.INFO, "imported tileset {0} from {1}", new Object[] {tileset.getName(), file});
  }

  public void importResources(Path... files) {
    List<Path> spritesheets = new ArrayList<>();
    List<Path> sounds = new ArrayList<>();
    List<Path> animations = new ArrayList<>();
    List<Path> spriteDefinitions = new ArrayList<>();
    List<Path> emitters = new ArrayList<>();
    List<Path> blueprints = new ArrayList<>();
    List<Path> tilesets = new ArrayList<>();

    for (Path file : files) {
      if (file == null || !Files.isRegularFile(file)) {
        continue;
      }
      String extension = FileUtilities.getExtension(file).toLowerCase(Locale.ROOT);
      if (ImageFormat.isSupported(file)) {
        spritesheets.add(file);
      } else if (SoundFormat.isSupported(file)) {
        sounds.add(file);
      } else if (extension.equals("json")) {
        animations.add(file);
      } else if (extension.equals(SpritesheetResource.PLAIN_TEXT_FILE_EXTENSION)) {
        spriteDefinitions.add(file);
      } else if (extension.equals(Blueprint.BLUEPRINT_FILE_EXTENSION) || extension.equals(Blueprint.TEMPLATE_FILE_EXTENSION)) {
        blueprints.add(file);
      } else if (extension.equals(Tileset.FILE_EXTENSION)) {
        tilesets.add(file);
      } else if (extension.equals("xml")) {
        emitters.add(file);
      }
    }

    if (!spritesheets.isEmpty()) {
      importSpriteSheets(spritesheets.toArray(Path[]::new));
    }
    if (!sounds.isEmpty()) {
      importSounds(sounds.toArray(Path[]::new));
    }
    if (!animations.isEmpty()) {
      importAnimations(animations.toArray(Path[]::new));
    }
    if (!spriteDefinitions.isEmpty()) {
      importSpriteFile(spriteDefinitions.toArray(Path[]::new));
    }
    if (!emitters.isEmpty()) {
      importEmitters(emitters.toArray(Path[]::new));
    }
    if (!blueprints.isEmpty()) {
      importBlueprints(blueprints.toArray(Path[]::new));
    }
    if (!tilesets.isEmpty()) {
      importTilesets(tilesets.toArray(Path[]::new));
    }
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
    if (UI.getMapController() != null) {
      this.getMapComponent().reloadEnvironment();
    }

    if (forceAssetTreeUpdate && UI.getAssetController() != null) {
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
    loadSpriteSheets(List.of(info), true);
    if (!embedded) {
      getGameFile().getTilesets().removeIf(x -> x.getName().equals(tileset.getName()));
      getGameFile().getTilesets().add((Tileset) tileset);
    }
  }

  public void save(boolean selectFile) {
    updateGameFileMaps();

    if (getGameFile() == null || getProjectPath() == null) {
      return;
    }

    if (this.currentResourceFile == null || selectFile) {
      JFileChooser chooser = new EditorFileSaver(getProjectPath());
      int result = chooser.showSaveDialog(Game.window().getHostControl());
      if (result == JFileChooser.APPROVE_OPTION) {
        saveGameFile(chooser.getSelectedFile().toPath());
      }
    } else {
      saveGameFile(this.currentResourceFile);
    }
    Game.config().save();
  }

  public void revert() {
    if (this.currentResourceFile == null) {
      return;
    }

    // close(true) clears currentResourceFile, so retain the saved project path before closing.
    Path resourceFile = this.currentResourceFile;

    boolean revert = UI.showRevertWarning();
    if (!revert) {
      return;
    }

    TmxMap currentMapSelection = null;
    if (UI.getMapController().getCurrentMap() != null) {
      currentMapSelection = UI.getMapController().getCurrentMap();
    }

    this.close(true);
    this.load(resourceFile, true);
    UI.getMapController().setSelection(currentMapSelection);
    log.log(Level.INFO, "Reverted all pending changes.");
  }

  public MapComponent getMapComponent() {
    return mapComponent;
  }

  public Path getCurrentResourceFile() {
    return currentResourceFile;
  }

  public synchronized String getCurrentStatus() {
    if (this.currentStatus != null && Game.time().since(this.statusTick) > STATUS_DURATION) {
      this.currentStatus = null;
    }
    return currentStatus;
  }

  public List<TmxMap> getChangedMaps() {
    return this.getMapComponent().getMaps().stream().filter(UndoManager::hasChanges).distinct().toList();
  }

  public synchronized void setCurrentStatus(String currentStatus) {
    this.currentStatus = currentStatus;
    this.statusTick = Game.time().now();
  }

  public void updateGameFileMaps() {
    this.getGameFile().getMaps().clear();
    for (TmxMap map : this.mapComponent.getMaps()) {
      this.getGameFile().getMaps().add(map);
    }

    if (UI.getAssetController() != null) {
      UI.getAssetController().refresh();
    }
  }

  public boolean isUnsavedProject() {
    return this.getCurrentResourceFile() == null && this.getProjectPath() != null;
  }

  private void saveGameFile(Path target) {
    try {
      Files.deleteIfExists(target);

      getGameFile().save(target.toString(), preferences().compressFile());
      AutoSaveManager.deleteBackup(target);
      this.currentResourceFile = target;
      this.windowMetadataDirty.set(true);
      preferences().addOpenedFile(target);
      gamefileLoaded();
      log.log(Level.INFO, "saved {0} maps, {1} spritesheets, {2} tilesets, {3} emitters, {4} blueprints, {5} sounds to {6}",
        new Object[] {
          getGameFile().getMaps().size(),
          getGameFile().getSpriteSheets().size(),
          getGameFile().getTilesets().size(),
          getGameFile().getEmitters().size(),
          getGameFile().getBluePrints().size(),
          getGameFile().getSounds().size(),
          getCurrentResourceFile()
        });
      this.setCurrentStatus(Resources.strings().get("status_gamefile_saved"));

      this.saveMaps();
    } catch (IOException e) {
      log.log(Level.SEVERE, "Failed to save game file: " + e.getMessage(), e);
      this.setCurrentStatus(Resources.strings().get("status_gamefile_save_error", e.getMessage()));
    }
  }

  private void saveMaps() {
    getChangedMaps().forEach(m -> {
      UndoManager.save(m);
      String fileName = String.format("%s.%s", m.getName(), TmxMap.FILE_EXTENSION);
      if (preferences().syncMaps()) {
        Path searchRoot = getProjectPath().getParent();
        try (Stream<Path> paths = Files.walk(searchRoot)) {
          paths.filter(p -> Files.isRegularFile(p) && p.getFileName().toString().equals(fileName)).forEach(p -> {
            Path newFile = XmlUtilities.save(m, p);
            log.log(Level.INFO, "synchronized map {0}", new Object[] {newFile});
          });
        } catch (IOException e) {
          log.log(Level.SEVERE, "Error walking file tree for map sync", e);
        }
      }
    });
  }

  private static void loadCustomEmitters(List<EmitterAttributes> emitters) {
    for (EmitterAttributes emitterData : emitters) {
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
      Spritesheet sprite;
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
      File imageFile = new File(imageLayer.getImage().getAbsoluteSourcePath().toString());
      if (!imageFile.exists()) {
        log.warning(
          () -> String.format("Source image for image layer '%s' in map '%s' could not be loaded: '%s'", map.getName(), imageLayer.getName(),
            imageLayer.getImage().getAbsoluteSourcePath().toString()));
        continue;
      }
      Spritesheet opt = Resources.spritesheets().get(imageLayer.getImage().getSource());
      Spritesheet sprite;
      if (opt == null) {
        BufferedImage img = Resources.images().get(imageLayer.getImage().getAbsoluteSourcePath(), true);
        if (img == null) {
          continue;
        }
        sprite = Resources.spritesheets().load(img, imageLayer.getImage().getSource(), img.getWidth(), img.getHeight());
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
      log.log(Level.INFO, "{0} tilesets loaded from {1}", new Object[] {cnt, map.getName()});
    }
  }

  private void gamefileLoaded() {
    preferences().addOpenedFile(this.currentResourceFile);
    for (Runnable callback : this.loadedCallbacks) {
      callback.run();
    }
  }
}
