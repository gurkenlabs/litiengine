package de.gurkenlabs.utiliti.swing;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import de.gurkenlabs.litiengine.resources.Resources;

public final class Icons {
  public static final Icon ADD = new ImageIcon(Resources.images().get("addx12.png"));
  public static final Icon ASSET = new ImageIcon(Resources.images().get("asset.png"));
  public static final Icon BLUEPRINT = new ImageIcon(Resources.images().get("blueprint.png"));
  public static final Icon COLLAPSE = new ImageIcon(Resources.images().get("collapse.png"));
  public static final Icon COLLISIONBOX = new ImageIcon(Resources.images().get("collisionbox.png"));
  public static final Icon COLORX16 = new ImageIcon(Resources.images().get("button-colorx16.png"));
  public static final Icon CONSOLE = new ImageIcon(Resources.images().get("console.png"));
  public static final Icon COPYX16 = new ImageIcon(Resources.images().get("button-copyx16.png"));
  public static final Icon CREATURE = new ImageIcon(Resources.images().get("creature.png"));
  public static final Icon CUBE = new ImageIcon(Resources.images().get("object_cubex10.png"));
  public static final Icon CUTX16 = new ImageIcon(Resources.images().get("button-cutx16.png"));
  public static final Icon DEFAULT_NODE = new ImageIcon(Resources.images().get("bullet.png"));
  public static final Icon DELETE = new ImageIcon(Resources.images().get("button-deletex12.png"));
  public static final Icon DELETEX16 = new ImageIcon(Resources.images().get("button-deletex16.png"));
  public static final Icon DELETE_X7 = new ImageIcon(Resources.images().get("button-deletex7.png"));
  public static final Icon DELETE_X7_DISABLED = new ImageIcon(Resources.images().get("button-delete-disabledx7.png"));
  public static final Icon DOC_BLUEPRINT = new ImageIcon(Resources.images().get("document-blueprint.png"));
  public static final Icon DOC_EMITTER = new ImageIcon(Resources.images().get("document-emitter.png"));
  public static final Icon DOC_SOUND = new ImageIcon(Resources.images().get("document-sound.png"));
  public static final Icon DOC_TILESET = new ImageIcon(Resources.images().get("document-tsx.png"));
  public static final Icon EMITTER = new ImageIcon(Resources.images().get("emitter.png"));
  public static final Icon EXPORT = new ImageIcon(Resources.images().get("export.png"));
  public static final Icon FOLDER = new ImageIcon(Resources.images().get("object_cube-10x10.png"));
  public static final Icon HIDEOTHER = new ImageIcon(Resources.images().get("button-hideother.png"));
  public static final Icon LAYER = new ImageIcon(Resources.images().get("layer.png"));
  public static final Icon LIFT = new ImageIcon(Resources.images().get("button-lift.png"));
  public static final Icon LIGHT = new ImageIcon(Resources.images().get("bulb.png"));
  public static final Icon LOWER = new ImageIcon(Resources.images().get("button-lower.png"));
  public static final Icon MAP_DELETE = new ImageIcon(Resources.images().get("button-deletex16.png"));
  public static final Icon MAPAREA = new ImageIcon(Resources.images().get("maparea.png"));
  public static final Icon MISC = new ImageIcon(Resources.images().get("misc.png"));
  public static final Icon PAUSE = new ImageIcon(Resources.images().get("button-pause.png"));
  public static final Icon PASTEX16 = new ImageIcon(Resources.images().get("button-pastex16.png"));
  public static final Icon PENCIL = new ImageIcon(Resources.images().get("pencil.png"));
  public static final Icon PLAY = new ImageIcon(Resources.images().get("button-play.png"));
  public static final Icon PROP = new ImageIcon(Resources.images().get("entity.png"));
  public static final Icon RENAMEX16 = new ImageIcon(Resources.images().get("button-renamex16.png"));
  public static final Icon SEARCH = new ImageIcon(Resources.images().get("search.png"));
  public static final Icon SHADOWBOX = new ImageIcon(Resources.images().get("shadowbox.png"));
  public static final Icon SPAWNPOINT = new ImageIcon(Resources.images().get("spawnpoint.png"));
  public static final Icon SPRITESHEET = new ImageIcon(Resources.images().get("spritesheet.png"));
  public static final Icon SOUND = new ImageIcon(Resources.images().get("sound.png"));
  public static final Icon TILESET = new ImageIcon(Resources.images().get("tileset.png"));
  public static final Icon TRIGGER = new ImageIcon(Resources.images().get("trigger.png"));

  public static class ToolBar {
    public static final int SIZE_ICON = 24;
    public static final Icon COLOR = get("button-color.png");
    public static final Icon COPY = get("button-copy.png");
    public static final Icon CREATE = get("button-create.png");
    public static final Icon CUT = get("button-cut.png");
    public static final Icon EDIT = get("button-edit.png");
    public static final Icon LOAD = get("button-load.png");
    public static final Icon MOVE = get("button-move.png");
    public static final Icon PASTE = get("button-paste.png");
    public static final Icon ADD = get("button-placeobject.png");
    public static final Icon REDO = get("button-redo.png");
    public static final Icon SAVE = get("button-save.png");
    public static final Icon UNDO = get("button-undo.png");

    private ToolBar() {
    }

    private static Icon get(String name) {
      return new ImageIcon(Resources.images().get(name));
    }
  }

  private Icons() {
  }
}
