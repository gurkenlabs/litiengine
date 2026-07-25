package de.gurkenlabs.utiliti.model.constants;

import de.gurkenlabs.litiengine.resources.Resources;

public final class EditorConstants {
  private EditorConstants() {
    // prevent instantiation
  }

  public static final int STATUS_DURATION = 5000;
  public static final String DEFAULT_GAME_NAME = "game";
  public static final String NEW_GAME_STRING = Resources.strings().get("editor_new_game");

  public static final String GAME_FILE_NAME = Resources.strings().get("file_type_game_resource");
  public static final String SPRITE_FILE_NAME = Resources.strings().get("file_type_sprite_info");
  public static final String AUDIO_FILE_NAME = Resources.strings().get("file_type_audio");
  public static final String SPRITESHEET_FILE_NAME = Resources.strings().get("file_type_spritesheet_image");
  public static final String TEXTUREATLAS_FILE_NAME = Resources.strings().get("file_type_texture_atlas");
  public static final String ANIMATION_FILE_NAME = Resources.strings().get("file_type_aseprite_animation");

  public static final String IMPORT_DIALOGUE = "import_something";

}
