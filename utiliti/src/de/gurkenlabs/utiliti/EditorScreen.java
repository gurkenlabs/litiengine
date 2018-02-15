package de.gurkenlabs.utiliti;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameFile;
import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.SpriteSheetInfo;
import de.gurkenlabs.litiengine.annotation.ScreenInfo;
import de.gurkenlabs.litiengine.environment.tilemap.IImageLayer;
import de.gurkenlabs.litiengine.environment.tilemap.ITileset;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Blueprint;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Map;
import de.gurkenlabs.litiengine.graphics.ImageCache;
import de.gurkenlabs.litiengine.graphics.ImageFormat;
import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.particles.xml.CustomEmitter;
import de.gurkenlabs.litiengine.graphics.particles.xml.EmitterData;
import de.gurkenlabs.litiengine.gui.screens.Screen;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.util.MathUtilities;
import de.gurkenlabs.util.io.FileUtilities;
import de.gurkenlabs.util.io.XmlUtilities;
import de.gurkenlabs.utiliti.components.EditorComponent;
import de.gurkenlabs.utiliti.components.EditorComponent.ComponentType;
import de.gurkenlabs.utiliti.components.MapComponent;
import de.gurkenlabs.utiliti.swing.XmlImportDialog;
import de.gurkenlabs.utiliti.swing.dialogs.SpritesheetImportPanel;
import de.gurkenlabs.utiliti.swing.panels.MapObjectPanel;

@ScreenInfo(name = "Editor")
public class EditorScreen extends Screen {
  private static final Logger log = Logger.getLogger(EditorScreen.class.getName());
  private static final int STATUS_DURATION = 5000;
  private static final String DEFAULT_GAME_NAME = "game";
  private static final String NEW_GAME_STRING = "NEW GAME *";

  private static final String GAME_FILE_NAME = "Game Resource File";
  private static final String SPRITE_FILE_NAME = "Sprite Info File";
  private static final String SPRITESHEET_FILE_NAME = "Spritesheet Image";

  public static final Color COLLISION_COLOR = new Color(255, 0, 0, 125);
  public static final Color BOUNDINGBOX_COLOR = new Color(0, 0, 255, 125);
  public static final Color COMPONENTBACKGROUND_COLOR = new Color(100, 100, 100, 125);

  private static EditorScreen instance;

  private final List<EditorComponent> comps;

  private double padding;
  private MapComponent mapComponent;
  private GameFile gameFile = new GameFile();
  private EditorComponent current;
  private String projectPath;
  private String currentResourceFile;

  private MapObjectPanel mapEditorPanel;

  private MapSelectionPanel mapSelectionPanel;

  private long statusTick;
  private String currentStatus;

  private EditorScreen() {
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
    Game.getCamera().updateFocus();
    if (Game.getEnvironment() != null) {
      Game.getEnvironment().render(g);
    }

    if (ImageCache.IMAGES.size() > 200) {
      ImageCache.IMAGES.clear();
      log.log(Level.INFO, "cache cleared!");
    }

    if (this.currentResourceFile != null) {
      Game.getScreenManager().setTitle(Game.getInfo().getName() + " " + Game.getInfo().getVersion() + " - " + this.currentResourceFile);
      String mapName = Game.getEnvironment() != null && Game.getEnvironment().getMap() != null ? "\nMap: " + Game.getEnvironment().getMap().getName() : "";
      Program.getTrayIcon().setToolTip(Game.getInfo().getName() + " " + Game.getInfo().getVersion() + "\n" + this.currentResourceFile + mapName);
    } else if (this.getProjectPath() != null) {
      Game.getScreenManager().setTitle(Game.getInfo().toString() + " - " + NEW_GAME_STRING);
      Program.getTrayIcon().setToolTip(Game.getInfo().toString() + "\n" + NEW_GAME_STRING);
    } else {
      Game.getScreenManager().setTitle(Game.getInfo().toString());
    }

    super.render(g);

    // render mouse/zoom and fps

    g.setFont(g.getFont().deriveFont(11f));
    g.setColor(Color.WHITE);
    Point tile = Input.mouse().getTile();
    RenderEngine.drawText(g, "x: " + (int) Input.mouse().getMapLocation().getX() + " y: " + (int) Input.mouse().getMapLocation().getY() + " tile: [" + tile.x + ", " + tile.y + "]" + " zoom: " + (int) (Game.getCamera().getRenderScale() * 100) + " %", 10,
        Game.getScreenManager().getResolution().getHeight() - 40);
    RenderEngine.drawText(g, Game.getMetrics().getFramesPerSecond() + " FPS", 10, Game.getScreenManager().getResolution().getHeight() - 20);

    // render status
    if (this.currentStatus != null && !this.currentStatus.isEmpty()) {
      long deltaTime = Game.getLoop().getDeltaTime(this.statusTick);
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
      RenderEngine.drawText(g, this.currentStatus, 10, Game.getScreenManager().getResolution().getHeight() - 60);
      g.setFont(old);
    }
  }

  public GameFile getGameFile() {
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
      chooser.setDialogTitle(Resources.get("input_select_project_folder"));
      chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      if (chooser.showOpenDialog(Game.getScreenManager().getRenderComponent()) != JFileChooser.APPROVE_OPTION) {
        return;
      }

      if (Game.getEnvironment() != null) {
        Game.loadEnvironment(null);
      }

      // set up project settings
      this.setProjectPath(chooser.getSelectedFile().getCanonicalPath());

      // load all maps in the directory
      this.mapComponent.loadMaps(this.getProjectPath());
      this.currentResourceFile = null;
      this.gameFile = new GameFile();

      // add sprite sheets by tile sets of all maps in the project director
      for (Map map : this.mapComponent.getMaps()) {
        this.loadSpriteSheets(map);
      }

      Program.getAssetTree().forceUpdate();

      // load custom emitter files
      this.loadCustomEmitters(this.getGameFile().getEmitters());

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
    JFileChooser chooser;

    try {
      chooser = new JFileChooser(new File(".").getCanonicalPath());

      FileFilter filter = new FileNameExtensionFilter(GAME_FILE_NAME, GameFile.FILE_EXTENSION);
      chooser.setFileFilter(filter);
      chooser.addChoosableFileFilter(filter);
      if (chooser.showOpenDialog(Game.getScreenManager().getRenderComponent()) == JFileChooser.APPROVE_OPTION) {
        this.load(chooser.getSelectedFile());
      }
    } catch (IOException e) {
      log.log(Level.SEVERE, e.getLocalizedMessage(), e);
    }
  }

  public void load(File gameFile) {
    final long currentTime = System.nanoTime();
    Game.getScreenManager().getRenderComponent().setCursor(Program.CURSOR_LOAD, 0, 0);
    Game.getScreenManager().getRenderComponent().setCursorOffsetX(0);
    Game.getScreenManager().getRenderComponent().setCursorOffsetY(0);

    try {
      if (!FileUtilities.getExtension(gameFile).equals(GameFile.FILE_EXTENSION)) {
        log.log(Level.SEVERE, "unsupported file format {0}", FileUtilities.getExtension(gameFile));
        return;
      }

      if (!gameFile.exists()) {
        log.log(Level.SEVERE, "gameFile {0} doesn't exist", gameFile);
        return;
      }

      // set up project settings
      this.currentResourceFile = gameFile.getPath();
      this.gameFile = GameFile.load(gameFile.getPath());

      Program.getUserPreferences().setLastGameFile(gameFile.getPath());
      Program.getUserPreferences().addOpenedFile(this.currentResourceFile);
      Program.loadRecentFiles();
      this.setProjectPath(FileUtilities.getParentDirPath(gameFile.getAbsolutePath()));

      // load maps from game file
      this.mapComponent.loadMaps(this.getGameFile().getMaps());

      // load sprite sheets from different sources:
      // 1. add sprite sheets from game file
      // 2. add sprite sheets by tile sets of all maps in the game file
      this.loadSpriteSheets(this.getGameFile().getSpriteSheets(), true);

      log.log(Level.INFO, "{0} spritesheets loaded from {1}", new Object[] { this.getGameFile().getSpriteSheets().size(), this.currentResourceFile });

      for (Map map : this.mapComponent.getMaps()) {
        this.loadSpriteSheets(map);
      }

      // load custom emitter files
      this.loadCustomEmitters(this.getGameFile().getEmitters());

      // display first available map after loading all stuff
      // also switch to map component
      if (!this.mapComponent.getMaps().isEmpty()) {
        this.mapComponent.loadEnvironment(this.mapComponent.getMaps().get(0));
      } else {
        Game.loadEnvironment(null);
      }

      this.changeComponent(ComponentType.MAP);
      this.setCurrentStatus(Resources.get("status_gamefile_loaded"));
    } finally {
      Game.getScreenManager().getRenderComponent().setCursor(Program.CURSOR, 0, 0);
      log.log(Level.INFO, "Loading gamefile {0} took: {1} ms", new Object[] { gameFile, (System.nanoTime() - currentTime) / 1000000.0 });
    }
  }

  public void importSpriteFile() {
    JFileChooser chooser;

    try {
      chooser = new JFileChooser(new File(this.getProjectPath()).getCanonicalPath());

      FileFilter filter = new FileNameExtensionFilter(SPRITE_FILE_NAME, SpriteSheetInfo.PLAIN_TEXT_FILE_EXTENSION);
      chooser.setFileFilter(filter);
      chooser.addChoosableFileFilter(filter);
      if (chooser.showOpenDialog(Game.getScreenManager().getRenderComponent()) == JFileChooser.APPROVE_OPTION) {
        File spriteFile = chooser.getSelectedFile();
        if (spriteFile == null) {
          return;
        }

        List<Spritesheet> loaded = Spritesheet.load(spriteFile.toString());
        List<SpriteSheetInfo> infos = new ArrayList<>();
        for (Spritesheet sprite : loaded) {
          SpriteSheetInfo info = new SpriteSheetInfo(sprite);
          infos.add(info);
          this.gameFile.getSpriteSheets().add(info);
        }

        this.loadSpriteSheets(infos, true);
      }

    } catch (IOException e) {
      log.log(Level.SEVERE, e.getLocalizedMessage(), e);
    }
  }

  public void importSpritesheets() {

    JFileChooser chooser;

    try {
      chooser = new JFileChooser(new File(this.getProjectPath()).getCanonicalPath());

      FileFilter filter = new FileNameExtensionFilter(SPRITESHEET_FILE_NAME, ImageFormat.getAllExtensions());
      chooser.setFileFilter(filter);
      chooser.addChoosableFileFilter(filter);
      chooser.setMultiSelectionEnabled(true);
      if (chooser.showOpenDialog(Game.getScreenManager().getRenderComponent()) == JFileChooser.APPROVE_OPTION) {
        SpritesheetImportPanel spritePanel = new SpritesheetImportPanel(chooser.getSelectedFiles());
        int option = JOptionPane.showConfirmDialog(Game.getScreenManager().getRenderComponent(), spritePanel, Resources.get("menu_assets_editSprite"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option != JOptionPane.OK_OPTION) {
          return;
        }

        final Collection<SpriteSheetInfo> sprites = spritePanel.getSpriteSheets();
        for (SpriteSheetInfo spriteFile : sprites) {
          this.gameFile.getSpriteSheets().add(spriteFile);
          log.log(Level.INFO, "imported spritesheet {0}", new Object[] { spriteFile.getName() });
        }

        this.loadSpriteSheets(sprites, true);
      }
    } catch (

    IOException e) {
      log.log(Level.SEVERE, e.getLocalizedMessage(), e);
    }
  }

  public void importEmitters() {
    XmlImportDialog.importXml("Emitter", files -> {
      for (File file : files) {
        EmitterData emitter = XmlUtilities.readFromFile(EmitterData.class, file.toString());
        if (emitter == null) {
          continue;
        }

        if (this.gameFile.getEmitters().stream().anyMatch(x -> x.getName().equals(emitter.getName()))) {
          int result = JOptionPane.showConfirmDialog(Game.getScreenManager().getRenderComponent(), Resources.get("import_emitter_question", emitter.getName()), Resources.get("import_emitter_title"), JOptionPane.YES_NO_OPTION);
          if (result == JOptionPane.NO_OPTION) {
            continue;
          }

          this.gameFile.getEmitters().removeIf(x -> x.getName().equals(emitter.getName()));
        }

        this.gameFile.getEmitters().add(emitter);
        log.log(Level.INFO, "imported emitter {0} from {1}", new Object[] { emitter.getName(), file.toString() });
      }
    });
  }

  public void importBlueprints() {
    XmlImportDialog.importXml("Blueprint", files -> {
      for (File file : files) {
        Blueprint blueprint = XmlUtilities.readFromFile(Blueprint.class, file.toString());
        if (blueprint == null) {
          continue;
        }

        if (this.gameFile.getBluePrints().stream().anyMatch(x -> x.getName().equals(blueprint.getName()))) {
          int result = JOptionPane.showConfirmDialog(Game.getScreenManager().getRenderComponent(), Resources.get("import_blueprint_question", blueprint.getName()), Resources.get("import_blueprint_title"), JOptionPane.YES_NO_OPTION);
          if (result == JOptionPane.NO_OPTION) {
            continue;
          }

          this.gameFile.getBluePrints().removeIf(x -> x.getName().equals(blueprint.getName()));
        }

        this.gameFile.getBluePrints().add(blueprint);
        log.log(Level.INFO, "imported blueprint {0} from {1}", new Object[] { blueprint.getName(), file.toString() });
      }
    });
  }

  public void loadSpriteSheets(Collection<SpriteSheetInfo> infos, boolean forceAssetTreeUpdate) {
    infos.parallelStream().forEach(info -> {
      Spritesheet.remove(info.getName());
      if (info.getHeight() == 0 && info.getWidth() == 0) {
        return;
      }

      Spritesheet.load(info);
    });

    ImageCache.clearAll();
    this.getMapComponent().reloadEnvironment();

    if (forceAssetTreeUpdate) {
      Program.getAssetTree().forceUpdate();
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
        FileFilter filter = new FileNameExtensionFilter(GAME_FILE_NAME, GameFile.FILE_EXTENSION);
        chooser.setFileFilter(filter);
        chooser.addChoosableFileFilter(filter);
        chooser.setSelectedFile(new File(DEFAULT_GAME_NAME + "." + GameFile.FILE_EXTENSION));

        int result = chooser.showSaveDialog(Game.getScreenManager().getRenderComponent());
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

  public void setCurrentStatus(String currentStatus) {
    this.currentStatus = currentStatus;
    this.statusTick = Game.getLoop().getTicks();
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
    log.log(Level.INFO, "saved {0} maps and {1} tilesets to {2}", new Object[] { this.getGameFile().getMaps().size(), this.getGameFile().getSpriteSheets().size(), this.currentResourceFile });
    this.setCurrentStatus(Resources.get("status_gamefile_saved"));

    if (Program.getUserPreferences().isSyncMaps()) {
      this.saveMaps();
    }

    this.getMapSelectionPanel().bind(this.getMapComponent().getMaps());
    return saveFile;
  }

  private void saveMaps() {
    for (Map map : this.getMapComponent().getMaps().stream().filter(UndoManager::hasChanges).distinct().collect(Collectors.toList())) {
      UndoManager.save(map);
      for (String file : FileUtilities.findFilesByExtension(new ArrayList<>(), Paths.get(this.getProjectPath(), "maps"), map.getName() + "." + Map.FILE_EXTENSION)) {
        String newFile = XmlUtilities.save(map, file, Map.FILE_EXTENSION);
        log.log(Level.INFO, "synchronized map {0}", new Object[] { newFile });
      }
    }
  }

  private void loadCustomEmitters(List<EmitterData> emitters) {
    for (EmitterData emitter : emitters) {
      CustomEmitter.load(emitter);
    }
  }

  private void loadSpriteSheets(Map map) {
    List<SpriteSheetInfo> infos = new ArrayList<>();
    int cnt = 0;
    for (ITileset tileSet : map.getTilesets()) {
      if (tileSet.getImage() == null || Spritesheet.find(tileSet.getName()) != null) {
        continue;
      }

      Spritesheet sprite = Spritesheet.find(tileSet.getImage().getSource());
      if (sprite == null) {
        sprite = Spritesheet.load(tileSet);
        if (sprite == null) {
          continue;
        }
      }

      infos.add(new SpriteSheetInfo(sprite));
      cnt++;
    }

    for (IImageLayer imageLayer : map.getImageLayers()) {
      Spritesheet sprite = Spritesheet.find(imageLayer.getImage().getSource());
      if (sprite == null) {
        BufferedImage img = Resources.getImage(imageLayer.getImage().getAbsoluteSourcePath(), true);

        sprite = Spritesheet.load(img, imageLayer.getImage().getSource(), img.getWidth(), img.getHeight());
        if (sprite == null) {
          continue;
        }
      }

      infos.add(new SpriteSheetInfo(sprite));
      cnt++;
    }

    this.loadSpriteSheets(infos, false);
    for (SpriteSheetInfo info : infos) {
      if (!this.getGameFile().getSpriteSheets().stream().anyMatch(x -> x.getName().equals(info.getName()))) {
        this.getGameFile().getSpriteSheets().add(info);
      }
    }

    if (cnt > 0) {
      log.log(Level.INFO, "{0} tilesets loaded from {1}", new Object[] { cnt, map.getFileName() });
    }
  }
}