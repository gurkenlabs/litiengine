package de.gurkenlabs.litiengine.graphics.animation;

import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.Set;
import java.util.Map;
import javax.imageio.ImageIO;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.animation.Animation;

/**
 * Offers an interface to import Aseprite JSON export format.
 * Note: requires animation key frames to have same dimensions to support internal animation format.
 * */
public class AsepriteHandler {

  /** 
   * Thrown to indicate error when importing Aseprite JSON format.
   * */
  public static class ImportAnimationException extends Error {
    public ImportAnimationException(String message) {
      super(message);
    }
  }

  /** 
   * Imports an Aseprite animation (.json + sprite sheet).
   * Note: searches for sprite sheet path through .json metadata, specifically 'image' element. This should be an absolute path in system.
   *
   * @param jsonPath path (including filename) to Aseprite JSON.
   * 
   * @return Animation object represented by each key frame in Aseprite sprite sheet.
   * */
  public static Animation importAnimation(String jsonPath) throws IOException, FileNotFoundException, AsepriteHandler.ImportAnimationException {

    JsonElement rootElement = null;
    try { rootElement = getRootJsonElement(jsonPath); }
    catch(FileNotFoundException e) {
      throw new FileNotFoundException("FileNotFoundException: Could not find .json file " + jsonPath);
    }

    String spriteSheetPath = getSpriteSheetPath(rootElement);
    File spriteSheetFile = new File(spriteSheetPath);
    if(!spriteSheetFile.exists()) {
      throw new FileNotFoundException("FileNotFoundException: Could not find sprite sheet file. " +
                                      "Expected location is 'image' in .json metadata, which evaluates to: " + spriteSheetPath);
    }

    Dimension keyFrameDimensions = getKeyFrameDimensions(rootElement);
    Dimension spriteSheetDimensions = getSpriteSheetDimensions(rootElement);
    if(areKeyFramesSameDimensions(rootElement, keyFrameDimensions)) {

      BufferedImage image = new BufferedImage((int)spriteSheetDimensions.getWidth(),
                                              (int)spriteSheetDimensions.getHeight(),
                                              BufferedImage.TYPE_4BYTE_ABGR);

      try { image = ImageIO.read(spriteSheetFile); }
      catch(IOException e) {
        throw new IOException("IOException: Could not write sprite sheet data to BufferedImage object.");
      }

      Spritesheet spriteSheet = new Spritesheet(image,
                                                spriteSheetPath,
                                                (int)keyFrameDimensions.getWidth(),
                                                (int)keyFrameDimensions.getHeight());

      return new Animation(spriteSheet, false, getKeyFrameDurations(rootElement));
    }

    throw new AsepriteHandler.ImportAnimationException("AsepriteHandler.ImportAnimationException: animation key frames require same dimensions.");
  }

  /**
   * @param jsonPath path (including filename) to Aseprite .json file.
   *
   * @return root element of JSON data.
   * */
  private static JsonElement getRootJsonElement(String jsonPath) throws FileNotFoundException {

    File jsonFile = new File(jsonPath);

    try { 
      JsonElement rootElement = JsonParser.parseReader(new FileReader(jsonFile)); 
      return rootElement;
    }
    catch(FileNotFoundException e) { throw e; }
  }

  /**
   * @param rootElement root element of JSON data.
   *
   * @return path (including filename) to animation sprite sheet.
   * */
  private static String getSpriteSheetPath(JsonElement rootElement) {

    JsonElement metaData = rootElement.getAsJsonObject().get("meta");
    String spriteSheetPath = metaData.getAsJsonObject().get("image").getAsString();

    return spriteSheetPath;
  }

  /**
   * @param rootElement root element of JSON data.
   *
   * @return dimensions of animation sprite sheet. 
   * */
  private static Dimension getSpriteSheetDimensions(JsonElement rootElement) {

    JsonElement metadata = rootElement.getAsJsonObject().get("meta");
    JsonObject spriteSheetSize = metadata.getAsJsonObject().get("size").getAsJsonObject();

    int spriteSheetWidth = spriteSheetSize.get("w").getAsInt();
    int spriteSheetHeight = spriteSheetSize.get("h").getAsInt();

    return new Dimension(spriteSheetWidth, spriteSheetHeight);
  }

  /**
   * @param rootElement root element of JSON data.
   *
   * @return dimensions of first key frame.
   * */
  private static Dimension getKeyFrameDimensions(JsonElement rootElement) {

    JsonElement frames = rootElement.getAsJsonObject().get("frames");

    JsonObject firstFrameObject = frames.getAsJsonObject().entrySet().iterator().next().getValue().getAsJsonObject();
    JsonObject frameDimensions = firstFrameObject.get("sourceSize").getAsJsonObject();

    int frameWidth = frameDimensions.get("w").getAsInt();
    int frameHeight = frameDimensions.get("h").getAsInt();

    return new Dimension(frameWidth, frameHeight);
  }

  /**
   * @param rootElement root element of JSON data.
   * @param expected expected dimensions of each key frame.
   *
   * @return true if key frames have same duration.
   * */
  private static boolean areKeyFramesSameDimensions(JsonElement rootElement, Dimension expected) {

    JsonElement frames = rootElement.getAsJsonObject().get("frames");

    for(Map.Entry<String, JsonElement> entry : frames.getAsJsonObject().entrySet()) {
      JsonObject frameObject = entry.getValue().getAsJsonObject();
      JsonObject frameDimensions = frameObject.get("sourceSize").getAsJsonObject();

      int frameWidth = frameDimensions.get("w").getAsInt();
      int frameHeight = frameDimensions.get("h").getAsInt();

      if(frameWidth != expected.getWidth() || frameHeight != expected.getHeight())
        return false;
    }

    return true;
  }

  /**
   * @param rootElement root element of JSON data.
   *
   * @return integer array representing duration of each key frame.
   * */
  public static int[] getKeyFrameDurations(JsonElement rootElement) {

    JsonElement frames = rootElement.getAsJsonObject().get("frames");

    Set<Map.Entry<String, JsonElement>> keyFrameSet = frames.getAsJsonObject().entrySet();

    int[] keyFrameDurations = new int[keyFrameSet.size()];

    int frameIndex = 0;
    for(Map.Entry<String, JsonElement> entry : keyFrameSet) {
      JsonObject frameObject = entry.getValue().getAsJsonObject();
      int frameDuration = frameObject.get("duration").getAsInt();
      keyFrameDurations[frameIndex++] = frameDuration;
    }

    return keyFrameDurations;
  }
}
