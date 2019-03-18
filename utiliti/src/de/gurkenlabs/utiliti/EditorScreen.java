package de.gurkenlabs.utiliti;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.bind.JAXBException;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IImageLayer;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.ITileset;
import de.gurkenlabs.litiengine.environment.tilemap.MapRenderer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Blueprint;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Map;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MissingTmxResourceException;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.graphics.ImageFormat;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.TextRenderer;
import de.gurkenlabs.litiengine.graphics.emitters.xml.CustomEmitter;
import de.gurkenlabs.litiengine.graphics.emitters.xml.EmitterData;
import de.gurkenlabs.litiengine.gui.screens.Screen;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.resources.ResourceBundle;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.resources.SoundResource;
import de.gurkenlabs.litiengine.resources.SpritesheetResource;
import de.gurkenlabs.litiengine.resources.TextureAtlas;
import de.gurkenlabs.litiengine.sound.SoundFormat;
import de.gurkenlabs.litiengine.util.MathUtilities;
import de.gurkenlabs.litiengine.util.io.FileUtilities;
import de.gurkenlabs.litiengine.util.io.ImageSerializer;
import de.gurkenlabs.litiengine.util.io.XmlUtilities;
import de.gurkenlabs.utiliti.components.EditorComponent;
import de.gurkenlabs.utiliti.components.EditorComponent.ComponentType;
import de.gurkenlabs.utiliti.components.MapComponent;
import de.gurkenlabs.utiliti.swing.EditorFileChooser;
import de.gurkenlabs.utiliti.swing.XmlImportDialog;
import de.gurkenlabs.utiliti.swing.dialogs.SpritesheetImportPanel;
import de.gurkenlabs.utiliti.swing.panels.MapObjectPanel;

public class EditorScreen extends Screen {
  private static final Logger log = Logger.getLogger(EditorScreen.class.getName());
  private static final int STATUS_DURATION = 5000;
  private static final String DEFAULT_GAME_NAME = "game";
  private static final String NEW_GAME_STRING = "NEW GAME *";

  private static final String GAME_FILE_NAME = "Game Resource File";
  private static final String SPRITE_FILE_NAME = "Sprite Info File";
  private static final String AUDIO_FILE_NAME = "Audio File";
  private static final String SPRITESHEET_FILE_NAME = "Spritesheet Image";
  private static final String TEXTUREATLAS_FILE_NAME = "Texture Atlas XML (generic)";

  public static final Color COLLISION_COLOR = new Color(255, 0, 0, 125);
  public static final Color BOUNDINGBOX_COLOR = new Color(0, 0, 255, 125);
  public static final Color COMPONENTBACKGROUND_COLOR = new Color(100, 100, 100, 125);

  private static EditorScreen instance;

  private final List<EditorComponent> comps;

  private double padding;
  private MapComponent mapComponent;
  private ResourceBundle gameFile = new ResourceBundle();
  private EditorComponent current;
  private String projectPath;
  private String currentResourceFile;

  private MapObjectPanel mapEditorPanel;

  private MapSelectionPanel mapSelectionPanel;

  private long statusTick;
  private String currentStatus;
  private boolean loading;

  private EditorScreen() {
    super("Editor");
    this.comps = new ArrayList<>();
  }

  public static EditorScreen instance() {
    if (instance != null) {
      return instance;
    }

    instance = new EditorScreen();
    return instance;
  }

  public boolean fileLoaded() {
    return this.currentResourceFile != null;
  }

  @Override
  public void prepare() {
    padding = this.getWidth() / 50;

    // init components
    this.mapComponent = new MapComponent(this);
    this.comps.add(this.mapComponent);
    super.prepare();
  }

  @Override
  public void render(final Graphics2D g) {

    Game.world().camera().updateFocus();
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
      Program.setTrayToolTip(Game.info().getName() + " " + Game.info().getVersion() + "\n" + this.currentResourceFile + mapName);
    } else if (this.getProjectPath() != null) {
      Game.window().setTitle(Game.info().getTitle() + " - " + NEW_GAME_STRING);
      Program.setTrayToolTip(Game.info().getTitle() + "\n" + NEW_GAME_STRING);
    } else {
      Game.window().setTitle(Game.info().getTitle());
    }

    super.render(g);

    // render mouse/zoom and fps

    g.setFont(Program.TEXT_FONT);
    g.setColor(Color.WHITE);
    Point tile = Input.mouse().getTile();

    TextRenderer.render(g, "x: " + (int) Input.mouse().getMapLocation().getX() + " y: " + (int) Input.mouse().getMapLocation().getY() + " tile: [" + tile.x + ", " + tile.y + "]" + " zoom: " + (int) (Game.world().camera().getRenderScale() * 100) + " %", 10,
        Game.window().getResolution().getHeight() - 40);
    TextRenderer.render(g, Game.metrics().getFramesPerSecond() + " FPS", 10, Game.window().getResolution().getHeight() - 20);

    // render status
    if (this.currentStatus != null && !this.currentStatus.isEmpty()) {
      long deltaTime = Game.loop().getDeltaTime(this.statusTick);
      if (deltaTime > STATUS_DURATION) {
        this.currentStatus = null;
      }

      // fade out status color
      final double fadeOutTime = 0.75 * STATUS_DURATION;
      if (deltaTime > fadeOutTime) {
        double fade = deltaTime - fadeOutTime;
        int alpha = (int) (255 - (fade / (STATUS_DURATION - fadeOutTime)) * 255);
        g.setColor(new Color(255, 255, 255, MathUtilities.clamp(alpha, 0, 255)));
      }

      Font old = g.getFont();
      g.setFont(g.getFont().deriveFont(20.0f));
      TextRenderer.render(g, this.currentStatus, 10, Game.window().getResolution().getHeight() - 60);
      g.setFont(old);
    }
  }

  public ResourceBundle getGameFile() {
    return this.gameFile;
  }

  public String getProjectPath() {
    return projectPath;
  }

  public double getPadding() {
    return this.padding;
  }

  public void setProjectPath(String projectPath) {
    this.projectPath = projectPath;
  }

  public void changeComponent(EditorComponent.ComponentType type) {
    if (this.current != null) {
      this.current.suspend();
      this.getComponents().remove(this.current);
    }

    for (EditorComponent comp : this.comps) {
      if (comp.getComponentType() == type) {
        this.current = comp;
        this.current.prepare();
        this.getComponents().add(this.current);
        break;
      }
    }
  }

  public void create() {
    JFileChooser chooser;
    try {
      chooser = new JFileChooser(new File(".").getCanonicalPath());
      chooser.setDialogTitle(Resources.strings().get("input_select_project_folder"));
      chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      if (chooser.showOpenDialog((JFrame) Game.window().getHostControl()) != JFileChooser.APPROVE_OPTION) {
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
      for (Map map : this.mapComponent.getMaps()) {
        this.loadSpriteSheets(map);
      }

      Program.getAssetTree().forceUpdate();

      // load custom emitter files
      loadCustomEmitters(this.getGameFile().getEmitters());

      // update new game file by the loaded information
      this.updateGameFileMaps();

      // display first available map after loading all stuff
      if (!this.mapComponent.getMaps().isEmpty()) {
        this.mapComponent.loadEnvironment(this.mapComponent.getMaps().get(0));
        this.changeComponent(ComponentType.MAP);
      }
    } catch (IOException e) {
      log.log(Level.SEVERE, e.getLocalizedMessage(), e);
    }

    this.setCurrentStatus("created new project");
  }

  public void load() {
    if (EditorFileChooser.showFileDialog(ResourceBundle.FILE_EXTENSION, GAME_FILE_NAME, false, ResourceBundle.FILE_EXTENSION) == JFileChooser.APPROVE_OPTION) {
      this.load(EditorFileChooser.instance().getSelectedFile());
    }
  }

  public void load(File gameFile) {
    boolean proceedLoading = Program.notifyPendingChanges();
    if (!proceedLoading) {
      return;
    }

    final long currentTime = System.nanoTime();
    Game.window().getRenderComponent().setCursor(Program.CURSOR_LOAD, 0, 0);
    Game.window().getRenderComponent().setCursorOffsetX(0);
    Game.window().getRenderComponent().setCursorOffsetY(0);

    this.loading = true;
    try {
      if (!FileUtilities.getExtension(gameFile).equals(ResourceBundle.FILE_EXTENSION)) {
        log.log(Level.SEVERE, "unsupported file format {0}", FileUtilities.getExtension(gameFile));
        return;
      }

      if (!gameFile.exists()) {
        log.log(Level.SEVERE, "gameFile {0} does not exist", gameFile);
        return;
      }

      UndoManager.clearAll();

      // set up project settings
      this.currentResourceFile = gameFile.getPath();
      this.gameFile = ResourceBundle.load(gameFile.getPath());

      Program.getUserPreferences().setLastGameFile(gameFile.getPath());
      Program.getUserPreferences().addOpenedFile(this.currentResourceFile);
      Program.loadRecentFiles();
      this.setProjectPath(gameFile.getPath());

      // load maps from game file
      this.mapComponent.loadMaps(this.getGameFile().getMaps());

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
      
      for (Map map : this.mapComponent.getMaps()) {
        this.loadSpriteSheets(map);
      }

      // load custom emitter files
      loadCustomEmitters(this.getGameFile().getEmitters());
      Program.getAssetTree().forceUpdate();

      // display first available map after loading all stuff
      // also switch to map component
      if (!this.mapComponent.getMaps().isEmpty()) {
        this.mapComponent.loadEnvironment(this.mapComponent.getMaps().get(0));
      } else {
        Game.world().unloadEnvironment();
      }

      this.changeComponent(ComponentType.MAP);
      this.setCurrentStatus(Resources.strings().get("status_gamefile_loaded"));
    } finally {
      Game.window().getRenderComponent().setCursor(Program.CURSOR, 0, 0);
      log.log(Level.INFO, "Loading gamefile {0} took: {1} ms", new Object[] { gameFile, (System.nanoTime() - currentTime) / 1000000.0 });
      this.loading = false;
    }
  }

  public void importSpriteFile() {

    if (EditorFileChooser.showFileDialog(SPRITE_FILE_NAME, Resources.strings().get("import_something", SPRITE_FILE_NAME), false, SpritesheetResource.PLAIN_TEXT_FILE_EXTENSION) == JFileChooser.APPROVE_OPTION) {
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
    if (EditorFileChooser.showFileDialog(SPRITESHEET_FILE_NAME, Resources.strings().get("import_something", SPRITE_FILE_NAME), true, ImageFormat.getAllExtensions()) == JFileChooser.APPROVE_OPTION) {
      this.importSpriteSheets(EditorFileChooser.instance().getSelectedFiles());
    }
  }

  public void importSounds() {
    if (EditorFileChooser.showFileDialog(AUDIO_FILE_NAME, Resources.strings().get("import_something", AUDIO_FILE_NAME), true, SoundFormat.getAllExtensions()) == JFileChooser.APPROVE_OPTION) {
      this.importSounds(EditorFileChooser.instance().getSelectedFiles());
    }
  }

  public void importTextureAtlas() {
    if (EditorFileChooser.showFileDialog(TEXTUREATLAS_FILE_NAME, Resources.strings().get("import_something", TEXTUREATLAS_FILE_NAME), false, "xml") == JFileChooser.APPROVE_OPTION) {
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
        emitter = XmlUtilities.readFromFile(EmitterData.class, file.toURI().toURL());
      } catch (JAXBException | MalformedURLException e) {
        log.log(Level.SEVERE, "could not load emitter data from " + file, e);
        return;
      }

      if (this.gameFile.getEmitters().stream().anyMatch(x -> x.getName().equals(emitter.getName()))) {
        int result = JOptionPane.showConfirmDialog(Game.window().getRenderComponent(), Resources.strings().get("import_emitter_question", emitter.getName()), Resources.strings().get("import_emitter_title"), JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.NO_OPTION) {
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
        blueprint = XmlUtilities.readFromFile(Blueprint.class, file.toURI().toURL());
      } catch (JAXBException | MalformedURLException e) {
        log.log(Level.SEVERE, "could not load blueprint from " + file, e);
        return;
      }
      if (blueprint == null) {
        return;
      }

      if (blueprint.getName() == null || blueprint.getName().isEmpty()) {
        blueprint.setName(FileUtilities.getFileName(file.getPath()));
      }

      if (this.gameFile.getBluePrints().stream().anyMatch(x -> x.getName().equals(blueprint.getName()))) {
        int result = JOptionPane.showConfirmDialog(Game.window().getRenderComponent(), Resources.strings().get("import_blueprint_question", blueprint.getName()), Resources.strings().get("import_blueprint_title"), JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.NO_OPTION) {
          return;
        }

        this.gameFile.getBluePrints().removeIf(x -> x.getName().equals(blueprint.getName()));
      }

      this.gameFile.getBluePrints().add(blueprint);
      log.log(Level.INFO, "imported blueprint {0} from {1}", new Object[] { blueprint.getName(), file });

    }, Blueprint.BLUEPRINT_FILE_EXTENSION, Blueprint.TEMPLATE_FILE_EXTENSION);
  }

  public void importTilesets() {
    XmlImportDialog.importXml("Tilesets", file -> {
      URL location;
      Tileset tileset;
      try {
        location = file.toURI().toURL();
        tileset = XmlUtilities.readFromFile(Tileset.class, location);
        tileset.finish(location);
      } catch (JAXBException | MalformedURLException | MissingTmxResourceException e) {
        log.log(Level.SEVERE, "could not load tileset from " + file, e);
        return;
      }

      if (this.gameFile.getTilesets().stream().anyMatch(x -> x.getName().equals(tileset.getName()))) {
        int result = JOptionPane.showConfirmDialog(Game.window().getRenderComponent(), Resources.strings().get("import_tileset_title", tileset.getName()), Resources.strings().get("import_tileset_title"), JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.NO_OPTION) {
          return;
        }

        this.getMapComponent().loadTileset(tileset, false);
      }

      log.log(Level.INFO, "imported tileset {0} from {1}", new Object[] { tileset.getName(), file });
    }, Tileset.FILE_EXTENSION);
  }

  public boolean isLoading() {
    return this.loading;
  }

  public void loadSpriteSheets(Collection<SpritesheetResource> infos, boolean forceAssetTreeUpdate) {
    infos.parallelStream().forEach(info -> {
      Optional<Spritesheet> opt = Resources.spritesheets().tryGet(info.getName());
      if (opt.isPresent()) {
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
      Program.getAssetTree().forceUpdate();
    }
  }

  public void saveMapSnapshot() {

    if (Game.world().environment() == null || Game.world().environment().getMap() == null) {
      return;
    }

    IMap currentMap = Game.world().environment().getMap();
    Dimension size = currentMap.getOrientation().getSize(currentMap);
    BufferedImage img = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
    MapRenderer.render(img.createGraphics(), currentMap, new Rectangle());

    try {
      final String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
      final File folder = new File("./screenshots/");
      if (!folder.exists()) {
        folder.mkdirs();
      }

      ImageSerializer.saveImage(new File("./screenshots/" + timeStamp + ImageFormat.PNG.toExtension()).toString(), img);
    } catch (Exception e) {
      log.log(Level.SEVERE, e.getLocalizedMessage(), e);
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

        int result = chooser.showSaveDialog((JFrame) Game.window().getHostControl());
        if (result == JFileChooser.APPROVE_OPTION) {
          String newFile = this.saveGameFile(chooser.getSelectedFile().toString());
          this.currentResourceFile = newFile;
        }
      } catch (IOException e1) {
        log.log(Level.SEVERE, e1.getLocalizedMessage(), e1);
      }
    } else {
      this.saveGameFile(this.currentResourceFile);
    }
  }

  public MapObjectPanel getMapObjectPanel() {
    return mapEditorPanel;
  }

  public MapComponent getMapComponent() {
    return this.mapComponent;
  }

  public String getCurrentResourceFile() {
    return this.currentResourceFile;
  }

  public void setMapEditorPanel(MapObjectPanel mapEditorPanel) {
    this.mapEditorPanel = mapEditorPanel;
  }

  public MapSelectionPanel getMapSelectionPanel() {
    return mapSelectionPanel;
  }

  public void setMapSelectionPanel(MapSelectionPanel mapSelectionPanel) {
    this.mapSelectionPanel = mapSelectionPanel;
  }

  public String getCurrentStatus() {
    return currentStatus;
  }

  public List<Map> getChangedMaps() {
    return this.getMapComponent().getMaps().stream().filter(UndoManager::hasChanges).distinct().collect(Collectors.toList());
  }

  public void setCurrentStatus(String currentStatus) {
    this.currentStatus = currentStatus;
    this.statusTick = Game.loop().getTicks();
  }

  public void updateGameFileMaps() {
    this.getGameFile().getMaps().clear();
    for (Map map : this.mapComponent.getMaps()) {
      this.getGameFile().getMaps().add(map);
    }

    Program.getAssetTree().forceUpdate();
  }

  private String saveGameFile(String target) {
    String saveFile = this.getGameFile().save(target, Program.getUserPreferences().isCompressFile());
    Program.getUserPreferences().setLastGameFile(this.currentResourceFile);
    Program.getUserPreferences().addOpenedFile(this.currentResourceFile);
    Program.loadRecentFiles();
    log.log(Level.INFO, "saved {0} maps, {1} spritesheets, {2} tilesets, {3} emitters, {4} blueprints, {5} sounds to {6}",
        new Object[] { this.getGameFile().getMaps().size(), this.getGameFile().getSpriteSheets().size(), this.getGameFile().getTilesets().size(), this.getGameFile().getEmitters().size(), this.getGameFile().getBluePrints().size(), this.getGameFile().getSounds().size(), this.currentResourceFile });
    this.setCurrentStatus(Resources.strings().get("status_gamefile_saved"));

    if (Program.getUserPreferences().isSyncMaps()) {
      this.saveMaps();
    }

    this.getMapSelectionPanel().bind(this.getMapComponent().getMaps());
    return saveFile;
  }

  private void saveMaps() {
    for (Map map : this.getChangedMaps()) {
      UndoManager.save(map);
      for (String file : FileUtilities.findFilesByExtension(new ArrayList<>(), Paths.get(FileUtilities.combine(this.getProjectPath(), "maps")), map.getName() + "." + Map.FILE_EXTENSION)) {
        File newFile = XmlUtilities.save(map, file, Map.FILE_EXTENSION);
        log.log(Level.INFO, "synchronized map {0}", new Object[] { newFile });
      }
    }
  }

  private static void loadCustomEmitters(List<EmitterData> emitters) {
    for (EmitterData emitter : emitters) {
      CustomEmitter.load(emitter);
    }
  }

  private void loadSpriteSheets(Map map) {
    List<SpritesheetResource> infos = new ArrayList<>();
    int cnt = 0;
    for (ITileset tileSet : map.getTilesets()) {
      if (tileSet.getImage() == null) {
        continue;
      }

      Optional<Spritesheet> opt = Resources.spritesheets().tryGet(tileSet.getImage().getSource());
      Spritesheet sprite = null;
      if (!opt.isPresent()) {
        sprite = Resources.spritesheets().load(tileSet);
        if (sprite == null) {
          continue;
        }
      } else {
        sprite = opt.get();
      }

      infos.add(new SpritesheetResource(sprite));
      cnt++;
    }

    for (IImageLayer imageLayer : map.getImageLayers()) {
      Optional<Spritesheet> opt = Resources.spritesheets().tryGet(imageLayer.getImage().getSource());
      Spritesheet sprite = null;
      if (!opt.isPresent()) {
        BufferedImage img = Resources.images().get(imageLayer.getImage().getAbsoluteSourcePath(), true);
        if (img == null) {
          continue;
        }

        sprite = Resources.spritesheets().load(img, imageLayer.getImage().getSource(), img.getWidth(), img.getHeight());
        if (sprite == null) {
          continue;
        }
      } else {
        sprite = opt.get();
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
}