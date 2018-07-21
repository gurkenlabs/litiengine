package de.gurkenlabs.utiliti;

import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import de.gurkenlabs.litiengine.Resources;

public final class Icons {
  public static final Icon ASSET = new ImageIcon(Resources.getImage("asset.png"));
  public static final Icon SPRITESHEET = new ImageIcon(Resources.getImage("spritesheet.png"));
  public static final Icon PROP = new ImageIcon(Resources.getImage("entity.png"));
  public static final Icon CREATURE = new ImageIcon(Resources.getImage("creature.png"));
  public static final Icon FOLDER = new ImageIcon(Resources.getImage("object_cube-10x10.png"));
  public static final Icon LIGHT = new ImageIcon(Resources.getImage("bulb.png"));
  public static final Icon TRIGGER = new ImageIcon(Resources.getImage("trigger.png"));
  public static final Icon SPAWNPOINT = new ImageIcon(Resources.getImage("spawnpoint.png"));
  public static final Icon COLLISIONBOX = new ImageIcon(Resources.getImage("collisionbox.png"));
  public static final Icon MAPAREA = new ImageIcon(Resources.getImage("maparea.png"));
  public static final Icon SHADOWBOX = new ImageIcon(Resources.getImage("shadowbox.png"));
  public static final Icon EMITTER = new ImageIcon(Resources.getImage("emitter.png"));
  public static final Icon MISC = new ImageIcon(Resources.getImage("misc.png"));
  public static final Icon TILESET = new ImageIcon(Resources.getImage("tileset.png"));
  public static final Icon BLUEPRINT = new ImageIcon(Resources.getImage("blueprint.png"));
  public static final Icon DOC_TILESET = new ImageIcon(Resources.getImage("document-tsx.png"));
  public static final Icon DOC_EMITTER = new ImageIcon(Resources.getImage("document-emitter.png"));
  public static final Icon DOC_BLUEPRINT = new ImageIcon(Resources.getImage("document-blueprint.png"));
  public static final Icon PLAY = new ImageIcon(Resources.getImage("button-play.png"));
  public static final Icon PAUSE = new ImageIcon(Resources.getImage("button-pause.png"));
  public static final Icon PENCIL = new ImageIcon(Resources.getImage("pencil.png"));
  public static final Icon DELETE = new ImageIcon(Resources.getImage("button-deletex12.png"));
  public static final Icon EXPORT = new ImageIcon(Resources.getImage("export.png"));
  public static final Icon ADD = new ImageIcon(Resources.getImage("addx12.png"));
  public static final Icon DEFAULT_NODE = new ImageIcon(Resources.getImage("bullet.png"));
  public static final BufferedImage DELETE_X7_DISABLED = Resources.getImage("button-delete-disabledx7.png");
  public static final BufferedImage DELETE_X7 = Resources.getImage("button-deletex7.png");
  public static final Icon MAP_EXPORT = new ImageIcon(Resources.getImage("button-map-exportx16.png"));
  public static final Icon MAP_DELETE = new ImageIcon(Resources.getImage("button-deletex16.png"));
  public static final Icon SEARCH = new ImageIcon(Resources.getImage("search.png"));
  public static final Icon COLLAPSE = new ImageIcon(Resources.getImage("collapse.png"));
  public static final Icon CUBE = new ImageIcon(Resources.getImage("object_cube-10x10.png"));
  public static final Icon LAYER = new ImageIcon(Resources.getImage("layer.png"));

  private Icons() {
  }
}
