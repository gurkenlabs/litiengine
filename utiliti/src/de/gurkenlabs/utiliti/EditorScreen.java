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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameFile;
import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.SpriteSheetInfo;
import de.gurkenlabs.litiengine.annotation.ScreenInfo;
import de.gurkenlabs.litiengine.environment.tilemap.IImageLayer;
import de.gurkenlabs.litiengine.environment.tilemap.ITileset;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Map;
import de.gurkenlabs.litiengine.graphics.ImageCache;
import de.gurkenlabs.litiengine.graphics.ImageFormat;
import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.particles.xml.CustomEmitter;
import de.gurkenlabs.litiengine.gui.screens.Screen;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.util.MathUtilities;
import de.gurkenlabs.util.io.FileUtilities;
import de.gurkenlabs.utiliti.components.EditorComponent;
import de.gurkenlabs.utiliti.components.EditorComponent.ComponentType;
import de.gurkenlabs.utiliti.components.MapComponent;
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

  private final List<Map> changedMaps;

  private EditorScreen() {
    this.comps = new ArrayList<>();
    this.changedMaps = new CopyOnWriteArrayList<>();
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
      Program.trayIcon.setToolTip(Game.getInfo().getName() + " " + Game.getInfo().getVersion() + "\n" + this.currentResourceFile + mapName);
    } else if (this.getProjectPath() != null) {
      Game.getScreenManager().setTitle(Game.getInfo().toString() + " - " + NEW_GAME_STRING);
      Program.trayIcon.setToolTip(Game.getInfo().toString() + "\n" + NEW_GAME_STRING);
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
      chooser.setDialogTitle("Select the java project folder");
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
      this.loadCustomEmitters(this.getProjectPath());

      // update new game file by the loaded information
      this.updateGameFile();

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
    final long current = System.nanoTime();
    Game.getScreenManager().getRenderComponent().setCursor(Program.CURSOR_LOAD, 0, 0);
    Game.getScreenManager().getRenderComponent().setCursorOffsetX(0);
    Game.getScreenManager().getRenderComponent().setCursorOffsetY(0);

    try {
      if (!FileUtilities.getExtension(gameFile).equals(GameFile.FILE_EXTENSION)) {
        log.log(Level.SEVERE, "unsupported file format '" + FileUtilities.getExtension(gameFile) + "'");
        return;
      }

      if (!gameFile.exists()) {
        log.log(Level.SEVERE, "gameFile '" + gameFile + "' doesn't exist");
        return;
      }

      // set up project settings
      this.currentResourceFile = gameFile.getPath();
      this.gameFile = GameFile.load(gameFile.getPath());
      Program.userPreferences.setLastGameFile(gameFile.getPath());
      Program.userPreferences.addOpenedFile(this.currentResourceFile);
      Program.loadRecentFiles();
      this.setProjectPath(FileUtilities.getParentDirPath(gameFile.getAbsolutePath()));

      // load maps from game file
      this.mapComponent.loadMaps(this.getGameFile().getMaps());

      // load sprite sheets from different sources:
      // 1. add sprite sheets from game file
      // 2. add sprite sheets by tile sets of all maps in the game file
      this.loadSpriteSheets(this.getGameFile().getSpriteSheets(), true);

      log.log(Level.INFO, this.getGameFile().getSpriteSheets().size() + " tilesheets loaded from '" + this.currentResourceFile + "'");

      for (Map map : this.mapComponent.getMaps()) {
        this.loadSpriteSheets(map);
      }

      // load custom emitter files
      this.loadCustomEmitters(this.getProjectPath());

      // display first available map after loading all stuff
      // also switch to map component
      if (!this.mapComponent.getMaps().isEmpty()) {
        this.mapComponent.loadEnvironment(this.mapComponent.getMaps().get(0));

        this.changeComponent(ComponentType.MAP);
      }
    } finally {
      Game.getScreenManager().getRenderComponent().setCursor(Program.CURSOR, 0, 0);
      log.log(Level.INFO, "Loading gamefile {0} took: {1} ms", new Object[] { gameFile, (System.nanoTime() - current) / 1000000.0 });
      this.setCurrentStatus("gamefile loaded");
    }
  }

  public void importSpriteFile() {
    JFileChooser chooser;

    try {
      chooser = new JFileChooser(new File(this.getProjectPath()).getCanonicalPath());

      FileFilter filter = new FileNameExtensionFilter(SPRITE_FILE_NAME, SpriteSheetInfo.FILE_EXTENSION);
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

  public void importSprites() {

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
        }

        this.loadSpriteSheets(sprites, true);
      }
    } catch (

    IOException e) {
      log.log(Level.SEVERE, e.getLocalizedMessage(), e);
    }
  }

  public void loadSpriteSheets(Collection<SpriteSheetInfo> infos, boolean forceAssetTreeUpdate) {
    for (SpriteSheetInfo info : infos) {
      Spritesheet.remove(info.getName());
      if (info.getHeight() == 0 && info.getWidth() == 0) {
        continue;
      }

      Spritesheet.load(info);
    }

    ImageCache.clearAll();
    this.getMapComponent().reloadEnvironment();

    if (forceAssetTreeUpdate) {
      Program.getAssetTree().forceUpdate();
    }
  }

  public void save(boolean selectFile) {
    this.updateGameFile();

    if (this.getGameFile() == null) {
      return;
    }

    if (this.currentResourceFile == null || selectFile) {
      JFileChooser chooser;
      try {
        chooser = new JFileChooser(new File(".").getCanonicalPath());
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

  public void mapChanged() {
    if (this.changedMaps.contains(Game.getEnvironment().getMap())) {
      return;
    }

    this.changedMaps.add((Map) Game.getEnvironment().getMap());
  }

  private String saveGameFile(String target) {
    String saveFile = this.getGameFile().save(target, Program.userPreferences.isCompressFile());
    Program.userPreferences.setLastGameFile(this.currentResourceFile);
    Program.userPreferences.addOpenedFile(this.currentResourceFile);
    Program.loadRecentFiles();
    log.log(Level.INFO, "saved " + this.getGameFile().getMaps().size() + " maps and " + this.getGameFile().getSpriteSheets().size() + " tilesets to '" + this.currentResourceFile + "'");
    this.setCurrentStatus("saved gamefile");

    if (Program.userPreferences.isSyncMaps()) {
      this.saveMaps();
    }

    this.changedMaps.clear();
    return saveFile;
  }

  private void saveMaps() {
    for (Map map : this.changedMaps.stream().distinct().collect(Collectors.toList())) {
      for (String file : FileUtilities.findFilesByExtension(new ArrayList<>(), Paths.get(this.getProjectPath(), "maps"), map.getName() + "." + Map.FILE_EXTENSION)) {
        map.save(file);
        log.log(Level.INFO, "synchronized map '" + file + "'");
      }
    }
  }

  /**
   * Gets the currenly configured spritefiles/spriteinfos and maps from the
   * different components and updates the game file object.
   */
  private void updateGameFile() {
    this.getGameFile().getMaps().clear();
    for (Map map : this.mapComponent.getMaps()) {
      this.getGameFile().getMaps().add(map);
    }
  }

  private void loadCustomEmitters(String projectPath) {
    for (String xmlFile : FileUtilities.findFilesByExtension(new ArrayList<>(), Paths.get(projectPath), "xml")) {
      boolean isEmitter = false;
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      try {
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();
        if (doc.getDocumentElement().getNodeName().equalsIgnoreCase("emitter")) {
          isEmitter = true;
        }
      } catch (SAXException | IOException | ParserConfigurationException e) {
        log.log(Level.SEVERE, e.getLocalizedMessage(), e);
      }

      if (isEmitter) {
        CustomEmitter.load(xmlFile);
      }
    }
  }

  private void loadSpriteSheets(Map map) {
    List<SpriteSheetInfo> infos = new ArrayList<>();
    int cnt = 0;
    for (ITileset tileSet : map.getTilesets()) {
      if (tileSet.getImage() == null) {
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

    log.log(Level.INFO, cnt + " tilesets loaded from '" + map.getFileName() + "'");
  }
}