package de.gurkenlabs.utiliti;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameFile;
import de.gurkenlabs.litiengine.SpriteSheetInfo;
import de.gurkenlabs.litiengine.annotation.ScreenInfo;
import de.gurkenlabs.litiengine.environment.tilemap.IImageLayer;
import de.gurkenlabs.litiengine.environment.tilemap.ITileset;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Map;
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
import de.gurkenlabs.utiliti.components.ProjectSettingsDialog;

@ScreenInfo(name = "Editor")
public class EditorScreen extends Screen {
  private static final int STATUS_DURATION = 5000;
  private static final String DEFAULT_GAME_NAME = "game";
  private static final String[] DEFAULT_SPRITESHEET_NAMES = { "sprites.info", "game.sprites" };
  private static final String NEW_GAME_STRING = "NEW GAME *";

  private static final String GAME_FILE_NAME = "Game Resource File";

  public static double padding, propertyWidth, valueWidth, rightValueX, rightPropertyX, leftPropertyX, leftValueX;

  public static final Color COLLISION_COLOR = new Color(255, 0, 0, 125);
  public static final Color BOUNDINGBOX_COLOR = new Color(0, 0, 255, 125);
  public static final Color COMPONENTBACKGROUND_COLOR = new Color(100, 100, 100, 125);

  private static EditorScreen instance;
  private final List<EditorComponent> comps;

  private MapComponent mapComponent;
  private GameFile gameFile = new GameFile();
  private EditorComponent current;
  private String projectPath;
  private String[] spriteFiles;
  private String currentResourceFile;

  private MapObjectPanel mapEditorPanel;

  private MapSelectionPanel mapSelectionPanel;

  private long statusTick;
  private String currentStatus;

  private EditorScreen() {
    this.comps = new ArrayList<>();
    this.spriteFiles = DEFAULT_SPRITESHEET_NAMES;
  }

  public static EditorScreen instance() {
    return instance != null ? instance : (instance = new EditorScreen());
  }

  public boolean fileLoaded() {
    return this.currentResourceFile != null;
  }

  public void setProjectSettings() {
    ProjectSettingsDialog dialog = new ProjectSettingsDialog();
    dialog.set(Arrays.asList(this.spriteFiles != null ? this.spriteFiles : DEFAULT_SPRITESHEET_NAMES));
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    dialog.setModal(true);
    dialog.setVisible(true);

    this.spriteFiles = dialog.getSpritefileNames();
  }

  @Override
  public void prepare() {
    padding = this.getWidth() / 50;
    propertyWidth = this.getWidth() * 3 / 20;
    valueWidth = this.getWidth() / 20;
    rightValueX = this.getWidth() - padding * 2 - valueWidth;
    rightPropertyX = this.getWidth() - padding * 3 - valueWidth - propertyWidth;

    leftPropertyX = padding;
    leftValueX = leftPropertyX + padding;

    // init components
    this.mapComponent = new MapComponent(this);
    this.comps.add(this.mapComponent);
    super.prepare();
  }

  @Override
  public void render(final Graphics2D g) {
    Game.getScreenManager().getCamera().updateFocus();
    if (Game.getEnvironment() != null) {
      Game.getEnvironment().render(g);
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
    g.setFont(Program.TEXT_FONT.deriveFont(Font.BOLD));
    g.setFont(g.getFont().deriveFont(11f));
    g.setColor(Color.WHITE);
    RenderEngine.drawText(g, "x: " + (int) Input.mouse().getMapLocation().getX() + " y: " + (int) Input.mouse().getMapLocation().getY() + " zoom: " + (int) (Game.getInfo().getRenderScale() * 100) + " %", 10, Game.getScreenManager().getResolution().getHeight() - 40);
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
      if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
        return;
      }

      // set up project settings
      this.setProjectPath(chooser.getSelectedFile().getCanonicalPath());
      this.setProjectSettings();

      // load all maps in the directory
      this.mapComponent.loadMaps(this.getProjectPath());
      this.currentResourceFile = null;
      this.gameFile = new GameFile();

      // load sprite sheets from different sources:
      // 1. add sprite sheets from sprite files
      // 2. add sprite sheets by tile sets of all maps in the project director
      this.loadSpriteFiles(this.getProjectPath(), this.spriteFiles);
      for (Map map : this.mapComponent.getMaps()) {
        this.loadSpriteSheets(map);
      }

      // load custom emitter files
      this.loadCustomEmitters(this.getProjectPath());

      // update new game file by the loaded information
      this.updateGameFile();

      // display first available map after loading all stuff
      if (!this.mapComponent.getMaps().isEmpty()) {
        this.mapComponent.loadEnvironment(this.mapComponent.getMaps().get(0));
        this.changeComponent(ComponentType.MAP);
      }
    } catch (IOException e1) {
      e1.printStackTrace();
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
      if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        this.load(chooser.getSelectedFile());
      }
    } catch (IOException e1) {
      e1.printStackTrace();
    }
  }

  public void load(File gameFile) {
    Game.getScreenManager().getRenderComponent().setCursor(Program.CURSOR_LOAD, 0, 0);
    Game.getScreenManager().getRenderComponent().setCursorOffsetX(0);
    Game.getScreenManager().getRenderComponent().setCursorOffsetY(0);

    try {
      if (!FileUtilities.getExtension(gameFile).equals(GameFile.FILE_EXTENSION)) {
        System.out.println("unsupported file format '" + FileUtilities.getExtension(gameFile) + "'");
        return;
      }

      if (!gameFile.exists()) {
        System.out.println("gameFile '" + gameFile + "' doesn't exist");
        return;
      }

      // set up project settings
      this.currentResourceFile = gameFile.getPath();
      this.gameFile = GameFile.load(gameFile.getPath());
      Program.USER_PREFERNCES.setLastGameFile(gameFile.getPath());
      Program.USER_PREFERNCES.addOpenedFile(this.currentResourceFile);
      Program.loadRecentFiles();
      this.setProjectPath(FileUtilities.getParentDirPath(gameFile.getAbsolutePath()));

      this.spriteFiles = this.gameFile.getSpriteFiles();

      // load maps from game file
      this.mapComponent.loadMaps(this.getGameFile().getMaps());

      // load sprite sheets from different sources:
      // 1. add sprite sheets from game file
      // 2. add sprite sheets from sprite files
      // 3. add sprite sheets by tile sets of all maps in the game file
      this.loadSpriteSheets(this.getGameFile().getTileSets());
      System.out.println(this.getGameFile().getTileSets().size() + " tilesheets loaded from '" + this.currentResourceFile + "'");

      this.loadSpriteFiles(this.getProjectPath(), this.getGameFile().getSpriteFiles());
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
      this.setCurrentStatus("gamefile loaded");
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

        int result = chooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
          String newFile = this.saveGameFile(chooser.getSelectedFile().toString());
          this.currentResourceFile = newFile;
        }
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    } else {
      this.saveGameFile(this.currentResourceFile);
    }
  }

  private String saveGameFile(String target) {
    String saveFile = this.getGameFile().save(target, Program.USER_PREFERNCES.isCompressFile());
    Program.USER_PREFERNCES.setLastGameFile(this.currentResourceFile);
    Program.USER_PREFERNCES.addOpenedFile(this.currentResourceFile);
    Program.loadRecentFiles();
    System.out.println("saved " + this.getGameFile().getMaps().size() + " maps and " + this.getGameFile().getTileSets().size()
        + " tilesets to '" + this.currentResourceFile + "'");
    this.setCurrentStatus("saved gamefile");

    if (Program.USER_PREFERNCES.isSyncMaps()) {
      this.saveMaps();
    }

    return saveFile;
  }

  private void saveMaps() {
    for (Map map : this.getGameFile().getMaps()) {
      for (String file : FileUtilities.findFiles(new ArrayList<>(), Paths.get(this.getProjectPath(), "maps"), map.getName() + "." + Map.FILE_EXTENSION)) {
        map.save(file);
        System.out.println("synchronized map '" + file + "'");
      }
    }
  }

  /**
   * Gets the currenly configured spritefiles/spriteinfos and maps from the
   * different components and updates the game file object.
   */
  private void updateGameFile() {
    this.getGameFile().setSpriteFiles(this.spriteFiles);

    this.getGameFile().getMaps().clear();
    for (Map map : this.mapComponent.getMaps()) {
      this.getGameFile().getMaps().add(map);
    }
  }

  private void loadCustomEmitters(String projectPath) {
    for (String xmlFile : FileUtilities.findFiles(new ArrayList<>(), Paths.get(projectPath), "xml")) {
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
        e.printStackTrace();
      }

      if (isEmitter) {
        CustomEmitter.load(xmlFile);
      }
    }
  }

  private void loadSpriteFiles(String projectPath, String[] spriteInfoFiles) {
    for (String spriteFile : FileUtilities.findFiles(new ArrayList<>(), Paths.get(projectPath), spriteInfoFiles)) {
      List<Spritesheet> loaded = Spritesheet.load(spriteFile, FileUtilities.getParentDirPath(this.currentResourceFile) + "\\resources\\");
      List<SpriteSheetInfo> infos = new ArrayList<>();
      for (Spritesheet sprite : loaded) {
        infos.add(new SpriteSheetInfo(sprite));
      }

      this.loadSpriteSheets(infos);
    }
  }

  private void loadSpriteSheets(Map map) {
    List<SpriteSheetInfo> infos = new ArrayList<>();
    int cnt = 0;
    for (ITileset tileSet : map.getTilesets()) {
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
        BufferedImage img = RenderEngine.getImage(imageLayer.getImage().getAbsoluteSourcePath(), true);

        sprite = Spritesheet.load(img, imageLayer.getImage().getSource(), img.getWidth(), img.getHeight());
        if (sprite == null) {
          continue;
        }
      }

      infos.add(new SpriteSheetInfo(sprite));
      cnt++;
    }

    this.loadSpriteSheets(infos);
    for (SpriteSheetInfo info : infos) {
      if (!this.getGameFile().getTileSets().stream().anyMatch(x -> x.getName().equals(info.getName()))) {
        this.getGameFile().getTileSets().add(info);
      }
    }

    System.out.println(cnt + " tilesets loaded from '" + map.getFileName() + "'");
  }

  private void loadSpriteSheets(List<SpriteSheetInfo> infos) {
    for (SpriteSheetInfo info : infos) {
      if (info.getHeight() == 0 && info.getWidth() == 0) {
        continue;
      }

      Spritesheet.load(info);
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
}