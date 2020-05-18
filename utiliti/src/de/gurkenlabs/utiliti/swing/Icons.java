package de.gurkenlabs.utiliti.swing;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import de.gurkenlabs.litiengine.resources.ResourceLoadException;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.Style.Theme;
import de.gurkenlabs.utiliti.components.Editor;

public final class Icons {
  public static Icon ADD;
  public static Icon ASSET;
  public static Icon BLUEPRINT;
  public static Icon COLLAPSE;
  public static Icon COLLISIONBOX;
  public static Icon COLOR;
  public static Icon CONSOLE;
  public static Icon COPY;
  public static Icon CREATURE;
  public static Icon CUBE;
  public static Icon CUT;
  public static Icon DEFAULT_NODE;
  public static Icon DELETE;
  public static Icon DELETE_X16;
  public static Icon DELETE_X7;
  public static Icon DELETE_X7_DISABLED;
  public static Icon DOC_BLUEPRINT;
  public static Icon DOC_EMITTER;
  public static Icon DOC_SOUND;
  public static Icon DOC_TILESET;
  public static Icon EMITTER;
  public static Icon EXPORT;
  public static Icon FOLDER;
  public static Icon HIDEOTHER;
  public static Icon LAYER;
  public static Icon LIFT;
  public static Icon LIGHT;
  public static Icon LOWER;
  public static Icon MAP_DELETE;
  public static Icon MAPAREA;
  public static Icon MISC;
  public static Icon PAUSE;
  public static Icon PASTE;
  public static Icon PENCIL;
  public static Icon PLAY;
  public static Icon PROP;
  public static Icon RENAME;
  public static Icon SEARCH;
  public static Icon SHADOWBOX;
  public static Icon SPAWNPOINT;
  public static Icon SPRITESHEET;
  public static Icon SOUND;
  public static Icon TILESET;
  public static Icon TRIGGER;

  public static void initialize(Theme theme) {
    ADD = get("add");
    ASSET = get("asset");
    BLUEPRINT = get("blueprint");
    COLLAPSE = get("collapse");
    COLLISIONBOX = get("collisionbox");
    COLOR = get("color");
    CONSOLE = get("console");
    COPY = get("copy");
    CREATURE = get("creature");
    CUBE = get("cube");
    CUT = get("cut");
    DEFAULT_NODE = get("bullet");
    DELETE = get("delete_x12");
    DELETE_X16 = get("delete_x16");
    DELETE_X7 = get("delete_x7");
    DELETE_X7_DISABLED = get("delete_x7_disabled");
    DOC_BLUEPRINT = get("document_blueprint");
    DOC_EMITTER = get("document_emitter");
    DOC_SOUND = get("document_sound");
    DOC_TILESET = get("document_tsx");
    EMITTER = get("emitter");
    EXPORT = get("export");
    FOLDER = get("cube");
    HIDEOTHER = get("hideother");
    LAYER = get("layer");
    LIFT = get("lift");
    LIGHT = get("bulb");
    LOWER = get("lower");
    MAP_DELETE = get("delete_x16");
    MAPAREA = get("maparea");
    MISC = get("misc");
    PAUSE = get("pause");
    PASTE = get("paste");
    PENCIL = get("pencil");
    PLAY = get("play");
    PROP = get("entity");
    RENAME = get("rename");
    SEARCH = get("search");
    SHADOWBOX = get("shadowbox");
    SPAWNPOINT = get("spawnpoint");
    SPRITESHEET = get("spritesheet");
    SOUND = get("sound");
    TILESET = get("tileset");
    TRIGGER = get("trigger");
  }

  private static ImageIcon get(String identifier) {
    ImageIcon ic;

    try {
      ic = new ImageIcon(Resources.images().get(String.format("%s_%s.png", identifier, Editor.preferences().getTheme().name().toLowerCase())));
    } catch (ResourceLoadException rle1) {
      try {
        ic = new ImageIcon(Resources.images().get(String.format("%s.png", identifier)));
      } catch (ResourceLoadException rle2) {
        return null;
      }
    }
    return ic;
  }

  private Icons() {
  }
}
