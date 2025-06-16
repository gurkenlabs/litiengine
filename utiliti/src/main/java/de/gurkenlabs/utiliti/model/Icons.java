package de.gurkenlabs.utiliti.model;

import com.github.weisj.darklaf.properties.icons.IconLoader;
import de.gurkenlabs.litiengine.resources.ResourceLoadException;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.Editor;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public final class Icons {
  // 8px variants
  public static final Icon ADD_8 = IconLoader.get().getUIAwareIcon("add.svg", 8, 8);
  public static final Icon ASSET_8 = IconLoader.get().getUIAwareIcon("asset.svg", 8, 8);
  public static final Icon BLUEPRINT_8 = IconLoader.get().getUIAwareIcon("blueprint.svg", 8, 8);
  public static final Icon BULB_8 = IconLoader.get().getUIAwareIcon("bulb.svg", 8, 8);
  public static final Icon CLEAR_CONSOLE_8 = IconLoader.get().getUIAwareIcon("clear-console.svg", 8, 8);
  public static final Icon COLLAPSE_8 = IconLoader.get().getUIAwareIcon("collapse.svg", 8, 8);
  public static final Icon COLLISIONBOX_8 = IconLoader.get().getUIAwareIcon("collisionbox.svg", 8, 8);
  public static final Icon COLOR_8 = IconLoader.get().getUIAwareIcon("color.svg", 8, 8);
  public static final Icon CONSOLE_8 = IconLoader.get().getUIAwareIcon("console.svg", 8, 8);
  public static final Icon COPY_8 = IconLoader.get().getUIAwareIcon("copy.svg", 8, 8);
  public static final Icon CREATURE_8 = IconLoader.get().getUIAwareIcon("creature.svg", 8, 8);
  public static final Icon CUT_8 = IconLoader.get().getUIAwareIcon("cut.svg", 8, 8);
  public static final Icon DELETE_8 = IconLoader.get().getUIAwareIcon("delete.svg", 8, 8);
  public static final Icon EMITTER_8 = IconLoader.get().getUIAwareIcon("emitter.svg", 8, 8);

  // 16px variants
  public static final Icon ADD_16 = IconLoader.get().getUIAwareIcon("add.svg", 16, 16);
  public static final Icon ASSET_16 = IconLoader.get().getUIAwareIcon("asset.svg", 16, 16);
  public static final Icon BLUEPRINT_16 = IconLoader.get().getUIAwareIcon("blueprint.svg", 16, 16);
  public static final Icon BULB_16 = IconLoader.get().getUIAwareIcon("bulb.svg", 16, 16);
  public static final Icon CLEAR_CONSOLE_16 = IconLoader.get().getUIAwareIcon("clear-console.svg", 16, 16);
  public static final Icon COLLAPSE_16 = IconLoader.get().getUIAwareIcon("collapse.svg", 16, 16);
  public static final Icon COLLISIONBOX_16 = IconLoader.get().getUIAwareIcon("collisionbox.svg", 16, 16);
  public static final Icon COLOR_16 = IconLoader.get().getUIAwareIcon("color.svg", 16, 16);
  public static final Icon CONSOLE_16 = IconLoader.get().getUIAwareIcon("console.svg", 16, 16);
  public static final Icon COPY_16 = IconLoader.get().getUIAwareIcon("copy.svg", 16, 16);
  public static final Icon CREATURE_16 = IconLoader.get().getUIAwareIcon("creature.svg", 16, 16);
  public static final Icon CUT_16 = IconLoader.get().getUIAwareIcon("cut.svg", 16, 16);
  public static final Icon DELETE_16 = IconLoader.get().getUIAwareIcon("delete.svg", 16, 16);
  public static final Icon EMITTER_16 = IconLoader.get().getUIAwareIcon("emitter.svg", 16, 16);

  // 24px variants
  public static final Icon ADD_24 = IconLoader.get().getUIAwareIcon("add.svg", 24, 24);
  public static final Icon ASSET_24 = IconLoader.get().getUIAwareIcon("asset.svg", 24, 24);
  public static final Icon BLUEPRINT_24 = IconLoader.get().getUIAwareIcon("blueprint.svg", 24, 24);
  public static final Icon BULB_24 = IconLoader.get().getUIAwareIcon("bulb.svg", 24, 24);
  public static final Icon CLEAR_CONSOLE_24 = IconLoader.get().getUIAwareIcon("clear-console.svg", 24, 24);
  public static final Icon COLLAPSE_24 = IconLoader.get().getUIAwareIcon("collapse.svg", 24, 24);
  public static final Icon COLLISIONBOX_24 = IconLoader.get().getUIAwareIcon("collisionbox.svg", 24, 24);
  public static final Icon COLOR_24 = IconLoader.get().getUIAwareIcon("color.svg", 24, 24);
  public static final Icon CONSOLE_24 = IconLoader.get().getUIAwareIcon("console.svg", 24, 24);
  public static final Icon COPY_24 = IconLoader.get().getUIAwareIcon("copy.svg", 24, 24);
  public static final Icon CREATURE_24 = IconLoader.get().getUIAwareIcon("creature.svg", 24, 24);
  public static final Icon CUT_24 = IconLoader.get().getUIAwareIcon("cut.svg", 24, 24);
  public static final Icon DELETE_24 = IconLoader.get().getUIAwareIcon("delete.svg", 24, 24);
  public static final Icon EMITTER_24 = IconLoader.get().getUIAwareIcon("emitter.svg", 24, 24);

  // 32px variants
  public static final Icon ADD_32 = IconLoader.get().getUIAwareIcon("add.svg", 32, 32);
  public static final Icon ASSET_32 = IconLoader.get().getUIAwareIcon("asset.svg", 32, 32);
  public static final Icon BLUEPRINT_32 = IconLoader.get().getUIAwareIcon("blueprint.svg", 32, 32);
  public static final Icon BULB_32 = IconLoader.get().getUIAwareIcon("bulb.svg", 32, 32);
  public static final Icon CLEAR_CONSOLE_32 = IconLoader.get().getUIAwareIcon("clear-console.svg", 32, 32);
  public static final Icon COLLAPSE_32 = IconLoader.get().getUIAwareIcon("collapse.svg", 32, 32);
  public static final Icon COLLISIONBOX_32 = IconLoader.get().getUIAwareIcon("collisionbox.svg", 32, 32);
  public static final Icon COLOR_32 = IconLoader.get().getUIAwareIcon("color.svg", 32, 32);
  public static final Icon CONSOLE_32 = IconLoader.get().getUIAwareIcon("console.svg", 32, 32);
  public static final Icon COPY_32 = IconLoader.get().getUIAwareIcon("copy.svg", 32, 32);
  public static final Icon CREATURE_32 = IconLoader.get().getUIAwareIcon("creature.svg", 32, 32);
  public static final Icon CUT_32 = IconLoader.get().getUIAwareIcon("cut.svg", 32, 32);
  public static final Icon DELETE_32 = IconLoader.get().getUIAwareIcon("delete.svg", 32, 32);
  public static final Icon EMITTER_32 = IconLoader.get().getUIAwareIcon("emitter.svg", 32, 32);

  public static final Icon DEFAULT_NODE = get("bullet");
  public static final Icon DOC_BLUEPRINT = get("document_blueprint");
  public static final Icon DOC_EMITTER = get("document_emitter");
  public static final Icon DOC_SOUND = get("document_sound");
  public static final Icon DOC_TILESET = get("document_tsx");
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
  public static final Icon SCROLL_DOWN = get("scroll_down");
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
      ic =
          new ImageIcon(
              Resources.images()
                  .get(
                      String.format(
                          "%s_%s.png",
                          identifier, Editor.preferences().getTheme().name().toLowerCase())));
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
