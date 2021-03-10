package de.gurkenlabs.litiengine.graphics.animation;

import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import javax.imageio.ImageIO;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.animation.Animation;
import de.gurkenlabs.litiengine.graphics.animation.KeyFrame;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.resources.SpritesheetResource;

/**
 * Offers an interface to import Aseprite JSON export format.
 * Note: requires animation key frames to have same dimensions to support internal animation format.
 */
public class AsepriteHandler {
  
  /**
   * Thrown to indicate error when importing Aseprite JSON format.
   */
  public static class ImportAnimationException extends Error {
    public ImportAnimationException(String message) {
      super(message);
    }
  }
  
  /**
   * Imports an Aseprite animation (.json + sprite sheet).
   *
   * @param jsonPath path (including filename) to Aseprite JSON.
   * @return Animation object represented by each key frame in Aseprite sprite sheet.
   */
  public static Animation importAnimation(String jsonPath) throws IOException, FileNotFoundException, AsepriteHandler.ImportAnimationException {
    
    JsonElement rootElement = null;
    try {
      rootElement = getRootJsonElement(jsonPath);
    } catch (FileNotFoundException e) {
      throw new FileNotFoundException("FileNotFoundException: Could not find .json file " + jsonPath);
    }
    
    File spriteSheetFile = null;
    try {
      spriteSheetFile = getSpriteSheetFile(rootElement, jsonPath);
    } catch (FileNotFoundException e) {
      throw new FileNotFoundException("FileNotFoundException: Could not find sprite sheet file. " +
              "Expected location is 'image' in .json metadata, or same folder as .json file.");
    }
    
    Dimension keyFrameDimensions = getKeyFrameDimensions(rootElement);
    if (areKeyFramesSameDimensions(rootElement, keyFrameDimensions)) {
      
      BufferedImage image = null;
      try {
        image = ImageIO.read(spriteSheetFile);
      } catch (IOException e) {
        throw new IOException("IOException: Could not write sprite sheet data to BufferedImage object.");
      }
      
      Spritesheet spriteSheet = new Spritesheet(image,
              spriteSheetFile.getPath().toString(),
              (int) keyFrameDimensions.getWidth(),
              (int) keyFrameDimensions.getHeight());
      
      return new Animation(spriteSheet, false, getKeyFrameDurations(rootElement));
    }
    
    throw new AsepriteHandler.ImportAnimationException("AsepriteHandler.ImportAnimationException: animation key frames require same dimensions.");
  }
  
  /**
   * @param jsonPath path (including filename) to Aseprite .json file.
   * @return root element of JSON data.
   */
  private static JsonElement getRootJsonElement(String jsonPath) throws FileNotFoundException {
    
    File jsonFile = new File(jsonPath);
    
    try {
      JsonElement rootElement = JsonParser.parseReader(new FileReader(jsonFile));
      return rootElement;
    } catch (FileNotFoundException e) {
      throw e;
    }
  }
  
  /**
   * Searches for sprite sheet path through .json metadata and same folder as .json file.
   *
   * @param rootElement root element of JSON data.
   * @param jsonPath    path (including filename) to .json Aseprite file.
   * @return sprite sheet file if it can be found, else an exception is thrown.
   */
  private static File getSpriteSheetFile(JsonElement rootElement, String jsonPath) throws FileNotFoundException {
    
    //try searching path supplied in .json data
    JsonElement metaData = rootElement.getAsJsonObject().get("meta");
    String spriteSheetPath = metaData.getAsJsonObject().get("image").getAsString();
    
    File spriteSheetFile = new File(spriteSheetPath);
    
    if (spriteSheetFile.exists())
      return spriteSheetFile;

    //try searching local directory
    Path jsonFilePath = Paths.get(jsonPath);
    String dirPath = jsonFilePath.getParent().toString();

    //same name as filename in 'image' element
    String fileNameSheet = Paths.get(spriteSheetPath).getFileName().toString(); //name of spritesheet
    String alternativePath1 = dirPath + "/" + fileNameSheet;

    spriteSheetFile = new File(alternativePath1);
    if(spriteSheetFile.exists())
      return spriteSheetFile;

    //same name as .json file
    String fileNameJson = jsonFilePath.getFileName().toString();
    String trimmedJsonName = fileNameJson.substring(0, fileNameJson.indexOf("."));  //without suffix
    String sheetSuffix = fileNameSheet.substring(fileNameSheet.indexOf("."), fileNameSheet.length());
    String alternativePath2 = dirPath + "/" + trimmedJsonName + sheetSuffix;

    spriteSheetFile = new File(alternativePath2);
    if(spriteSheetFile.exists())
      return spriteSheetFile;

    throw new FileNotFoundException();
  }
  
  /**
   * @param rootElement root element of JSON data.
   * @return dimensions of first key frame.
   */
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
   * @param expected    expected dimensions of each key frame.
   * @return true if key frames have same duration.
   */
  private static boolean areKeyFramesSameDimensions(JsonElement rootElement, Dimension expected) {
    
    JsonElement frames = rootElement.getAsJsonObject().get("frames");
    
    for (Map.Entry<String, JsonElement> entry : frames.getAsJsonObject().entrySet()) {
      JsonObject frameObject = entry.getValue().getAsJsonObject();
      JsonObject frameDimensions = frameObject.get("sourceSize").getAsJsonObject();
      
      int frameWidth = frameDimensions.get("w").getAsInt();
      int frameHeight = frameDimensions.get("h").getAsInt();
      
      if (frameWidth != expected.getWidth() || frameHeight != expected.getHeight())
        return false;
    }
    
    return true;
  }
  
  /**
   * @param rootElement root element of JSON data.
   * @return integer array representing duration of each key frame.
   */
  public static int[] getKeyFrameDurations(JsonElement rootElement) {
    
    JsonElement frames = rootElement.getAsJsonObject().get("frames");
    
    Set<Map.Entry<String, JsonElement>> keyFrameSet = frames.getAsJsonObject().entrySet();
    
    int[] keyFrameDurations = new int[keyFrameSet.size()];
    
    int frameIndex = 0;
    for (Map.Entry<String, JsonElement> entry : keyFrameSet) {
      JsonObject frameObject = entry.getValue().getAsJsonObject();
      int frameDuration = frameObject.get("duration").getAsInt();
      keyFrameDurations[frameIndex++] = frameDuration;
    }
    
    return keyFrameDurations;
  }
  
  /**
   * Error that is thrown by the export class
   */
  public static class ExportAnimationException extends Error {
    public ExportAnimationException(String message) {
      super(message);
    }
  }
  
  /**
   * Creates the json representation of an animation object and returns it.
   * This is the public accesible function and can/should be changed to fit into the UI.
   *
   * @param spritesheetResource the animation object to export
   */
  public static String exportAnimation(SpritesheetResource spritesheetResource) {
    String json = createJson(spritesheetResource);
    return json;
  }
  
  /**
   * Creates the json representation of an animation object and returns it as a string.
   *
   * @param spritesheetResource spritesheetResource object to export as json.
   * @return the json as a string.
   */
  private static String createJson(SpritesheetResource spritesheetResource) {
    Spritesheet spritesheet = Resources.spritesheets().load(spritesheetResource);
    assert spritesheet != null;
    int[] keyframes = Resources.spritesheets().getCustomKeyFrameDurations(spritesheet);
    Frames[] frames = new Frames[keyframes.length];
    
    if (frames.length != spritesheet.getTotalNumberOfSprites()) {
      throw new ExportAnimationException("Different dimensions of keyframes and sprites in spritesheet");
    }
    
    // Build the frames object in the json
    int numCol = spritesheet.getColumns();
    int numRows = spritesheet.getRows();
    int frameWidth = spritesheet.getSpriteWidth();
    int frameHeight = spritesheet.getSpriteHeight();
    
    for (int i = 0; i < numRows; i++) {
      for (int j = 0; j < numCol; j++) {
        final int row = i;
        final int col = j;
        Map<String, Integer> frame = new HashMap<>() {{
          put("x", (0 + col * frameWidth));
          put("y", (0 + row * frameHeight));
          put("w", frameWidth);
          put("h", frameHeight);
        }};
        Map<String, Integer> spriteSourceSize = new HashMap<>() {{
          put("x", 0);
          put("y", 0);
          put("w", frameWidth);
          put("h", frameHeight);
        }};
        Map<String, Integer> sourceSize = new HashMap<>() {{
          put("w", frameWidth);
          put("h", frameHeight);
        }};
        int duration = keyframes[i + j];
        String index = String.valueOf(i + j);
        frames[i + j] = new Frames("frame " + index,
                frame,
                false,
                false,
                spriteSourceSize,
                sourceSize,
                duration);
      }
    }
    
    // Build the meta object in the json
    int spritesheetWidth = frameWidth * numCol;
    int spritesheetHeight = frameHeight * numRows;
    Map<String, Integer> size = new HashMap<>() {{
      put("w", spritesheetWidth);
      put("h", spritesheetHeight);
    }};
    String spritesheetName = spritesheet.getName();
    Layer[] layers = {new Layer("Layer", 255, "normal")};
    Meta meta = new Meta("http://www.aseprite.org/",
            "1.2.16.3-x64",
            spritesheetName,
            "RGBA8888", size, "1", layers);
    
    // Create the json as string
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    StringBuilder sb = new StringBuilder();
    
    sb.append("{ \"frames\": {\n");
    for (int i = 0; i < frames.length; i++) {
      String json = gson.toJson(frames[i]);
      sb.append(" \"" + frames[i].name + "\": ").append(json).append(",\n");
    }
    sb.append(" },\n");
    String json = gson.toJson(meta);
    sb.append("\"meta\":").append(json).append("\n}");
    
    return sb.toString();
  }
  
  /**
   * Frames class for Aseprite json structure.
   */
  private static class Frames {
    transient String name;
    Map<String, Integer> frame;
    boolean rotated;
    boolean trimmed;
    Map<String, Integer> spriteSourceSize;
    Map<String, Integer> sourceSize;
    int duration;
    
    /**
     * @param name             name of frame
     * @param frame            x, y, w, h on the substruction of the sprite in the spritesheet.
     * @param rotated          is the frame rotated?
     * @param trimmed          is the frame trimmed?
     * @param spriteSourceSize how the sprite is trimmed.
     * @param sourceSize       the original sprite size.
     * @param duration         the duration of the frame
     */
    public Frames(String name, Map<String, Integer> frame, boolean rotated, boolean trimmed, Map<String, Integer> spriteSourceSize, Map<String, Integer> sourceSize, int duration) {
      this.name = name;
      this.frame = frame;
      this.rotated = rotated;
      this.trimmed = trimmed;
      this.spriteSourceSize = spriteSourceSize;
      this.sourceSize = sourceSize;
      this.duration = duration;
    }
  }
  
  /**
   * Meta data class for Aseprite json structure.
   */
  private static class Meta {
    String app;
    String version;
    String image;
    String format;
    Map<String, Integer> size;
    String scale;
    Layer[] layers;
    
    /**
     * @param app     the application the json format comes from, in this case Aseprite.
     * @param version Version of application.
     * @param image   filename of spritesheet.
     * @param format  color format of spritesheet image.
     * @param size    Size of spritesheet.
     * @param scale   Scale of spritesheet.
     * @param layers  Layers of spritesheet.
     */
    public Meta(String app, String version, String image, String format, Map<String, Integer> size, String scale, Layer[] layers) {
      this.app = app;
      this.version = version;
      this.image = image;
      this.format = format;
      this.size = size;
      this.scale = scale;
      this.layers = layers;
    }
  }
  
  /**
   * Layer class for Aseprite json structure.
   */
  private static class Layer {
    String name;
    int opacity;
    String blendMode;
    
    /**
     * @param name      Name of layer.
     * @param opacity   Opacity level of layer.
     * @param blendMode Blendmode of layer.
     */
    public Layer(String name, int opacity, String blendMode) {
      this.name = name;
      this.opacity = opacity;
      this.blendMode = blendMode;
    }
    
  }
}
