package de.gurkenlabs.litiengine.graphics.animation;

import java.awt.Image;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import de.gurkenlabs.litiengine.graphics.animation.Animation;
import de.gurkenlabs.litiengine.graphics.animation.KeyFrame;
import de.gurkenlabs.litiengine.graphics.Spritesheet;

public class AsepriteHandler {

    /**
     * Error that is thrown by the export class
     */
    public static class ExportAnimationException extends Error {
        public ExportAnimationException(String message) {
            super(message);
        }
    }

    /** 
     * Creates the json representation of an animation object and prints it.
     * This is the public accesible function and can/should be changed to fit into the UI.
     * 
     * @param animation the animation object to export
     */
    public void exportAnimation(Animation animation){

        String json = createJson(animation);
        System.out.println("JSON: " + json);
    }

    /**
     * Creates the json representation of an animation object and returns it as a string.
     * 
     * @param animation animation object to export as json.
     * @return the json as a string.
     */
    private String createJson(Animation animation){
        Spritesheet spritesheet = animation.getSpritesheet();
        List<KeyFrame> keyframes = animation.getKeyframes();
        Frames[] frames = new Frames[keyframes.size()];

        if(frames.length != spritesheet.getTotalNumberOfSprites()){
            //ERROR
            System.out.println("ERROR");
            throw new ExportAnimationException("Different dimensions of keyframes and sprites in spritesheet"); 
        }

        // Build the frames object in the json
        int numCol = spritesheet.getColumns();
        int numRows = spritesheet.getRows();
        int frameWidth = spritesheet.getSpriteWidth();
        int frameHeight = spritesheet.getSpriteHeight();

        for(int i = 0; i < numRows; i++){
            for(int j = 0; j < numCol; j++){
                final int row = i;
                final int col = j;
                Map<String, Integer> frame = new HashMap<>(){{
                    put("x", (0 + col*frameWidth) );
                    put("y", (0 + row*frameHeight) );
                    put("w", frameWidth);
                    put("h", frameHeight);
                }};
                Map<String, Integer> spriteSourceSize = new HashMap<>(){{
                    put("x", 0);
                    put("y", 0);
                    put("w", frameWidth);
                    put("h", frameHeight);
                }};
                Map<String, Integer> sourceSize = new HashMap<>(){{
                    put("w", frameWidth);
                    put("h", frameHeight);
                }};
                int duration = keyframes.get(i+j).getDuration();
                String index = String.valueOf(i+j);
                frames[i+j] = new Frames("frame " + index, 
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
        Map<String, Integer> size= new HashMap<>(){{
            put("w", spritesheetWidth);
            put("h", spritesheetHeight);
        }};
        String spritesheetName = spritesheet.getName();
        Layer[] layers = {new Layer("Layer",255,"normal")};
        Meta meta = new Meta("http://www.aseprite.org/",
        "1.2.16.3-x64",
        spritesheetName,
        "RGBA8888", size, "1", layers);
    
        // Create the json as string
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        StringBuilder sb = new StringBuilder();

        sb.append("{ \"frames\": {\n");
        for(int i = 0; i < frames.length; i++){
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
    private class Frames {
        transient String name; 
        Map<String, Integer> frame;
        boolean rotated;
        boolean trimmed;
        Map<String, Integer> spriteSourceSize;
        Map<String, Integer> sourceSize;
        int duration;

        /**
         * 
         * @param name name of frame
         * @param frame x, y, w, h on the substruction of the sprite in the spritesheet.
         * @param rotated is the frame rotated?
         * @param trimmed  is the frame trimmed?
         * @param spriteSourceSize how the sprite is trimmed.
         * @param sourceSize the original sprite size.
         * @param duration the duration of the frame
         */
        public Frames(String name, Map<String, Integer> frame, boolean rotated, boolean trimmed, Map<String, Integer> spriteSourceSize, Map<String, Integer> sourceSize, int duration){
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
    private class Meta {
        String app;
        String version;
        String image;
        String format;
        Map<String, Integer> size;
        String scale;
        Layer[] layers;

        /**
         * 
         * @param app the application the json format comes from, in this case Aseprite.
         * @param version Version of application.
         * @param image filename of spritesheet.
         * @param format color format of spritesheet image.
         * @param size Size of spritesheet.
         * @param scale Scale of spritesheet.
         * @param layers Layers of spritesheet.
         */
        public Meta(String app, String version, String image, String format, Map<String, Integer> size, String scale, Layer[] layers){
            this.app = app;
            this.version = version;
            this.image = image;
            this.format = format;
            this.size = size;
            this. scale = scale;
            this.layers = layers;
        }  
    }
    
    /**
     * Layer class for Aseprite json structure.
     */
    private class Layer {
        String name;
        int opacity;
        String blendMode;

        /**
         * 
         * @param name Name of layer.
         * @param opacity Opacity level of layer.
         * @param blendMode Blendmode of layer.
         */
        public Layer(String name, int opacity, String blendMode){
            this.name = name;
            this.opacity = opacity;
            this.blendMode = blendMode;
        }

    }

    
    
    
    
}


