package de.gurkenlabs.utiliti.swing;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import de.gurkenlabs.litiengine.resources.ResourceLoadException;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.components.Editor;

public final class Icons {
  public static final Icon ADD = get("add");
  public static final Icon ASSET = get("asset");
  public static final Icon BLUEPRINT = get("blueprint");
  public static final Icon COLLAPSE = get("collapse");
  public static final Icon COLLISIONBOX = get("collisionbox");
  public static final Icon COLOR = get("color");
  public static final Icon CONSOLE = get("console");
  public static final Icon COPY = get("copy");
  public static final Icon CREATURE = get("creature");
  public static final Icon CUBE = get("cube");
  public static final Icon CUT = get("cut");
  public static final Icon DEFAULT_NODE = get("bullet");
  public static final Icon DELETE = get("delete_x12");
  public static final Icon DELETE_X16 = get("delete_x16");
  public static final Icon DELETE_X7 = get("delete_x7");
  public static final Icon DELETE_X7_DISABLED = get("delete_x7_disabled");
  public static final Icon DOC_BLUEPRINT = get("document_blueprint");
  public static final Icon DOC_EMITTER = get("document_emitter");
  public static final Icon DOC_SOUND = get("document_sound");
  public static final Icon DOC_TILESET = get("document_tsx");
  public static final Icon EMITTER = get("emitter");
  public static final Icon EXPORT = get("export");
  public static final Icon FOLDER = get("cube");
  public static final Icon HIDEOTHER = get("hideother");
  public static final Icon LAYER = get("layer");
  public static final Icon LIFT = get("lift");
  public static final Icon LIGHT = get("bulb");
  public static final Icon LOWER = get("lower");
  public static final Icon MAP_DELETE = get("delete_x16");
  public static final Icon MAPAREA = get("maparea");
  public static final Icon MISC = get("misc");
  public static final Icon PAUSE = get("pause");
  public static final Icon PASTE = get("paste");
  public static final Icon PENCIL = get("pencil");
  public static final Icon PLAY = get("play");
  public static final Icon PROP = get("entity");
  public static final Icon RENAME = get("rename");
  public static final Icon SEARCH = get("search");
  public static final Icon SHADOWBOX = get("shadowbox");
  public static final Icon SPAWNPOINT = get("spawnpoint");
  public static final Icon SPRITESHEET = get("spritesheet");
  public static final Icon SOUND = get("sound");
  public static final Icon TILESET = get("tileset");
  public static final Icon TRIGGER = get("trigger");
  public static final Icon VISIBLE = get("visible");
  public static final Icon INVISIBLE = get("invisible");

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
