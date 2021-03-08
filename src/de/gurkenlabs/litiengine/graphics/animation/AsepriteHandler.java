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

    
    public void exportAnimation(Animation animation){

        String json = createJson(animation);
        System.out.println("JSON: " + json);
    }

    private String createJson(Animation animation){
        Spritesheet spritesheet = animation.getSpritesheet();
        List<KeyFrame> keyframes = animation.getKeyframes();
        Frames[] frames = new Frames[keyframes.size()];

        if(frames.length != spritesheet.getTotalNumberOfSprites()){
            //ERROR
            System.out.println("ERROR"); 
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

    private class Frames {
        transient String name; 
        Map<String, Integer> frame;
        boolean rotated;
        boolean trimmed;
        Map<String, Integer> spriteSourceSize;
        Map<String, Integer> sourceSize;
        int duration;

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

    private class Meta {
        String app;
        String version;
        String image;
        String format;
        Map<String, Integer> size;
        String scale;
        Layer[] layers;



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

    private class Layer {
        String name;
        int opacity;
        String blendMode;

        public Layer(String name, int opacity, String blendMode){
            this.name = name;
            this.opacity = opacity;
            this.blendMode = blendMode;
        }

    }

    
    
    
    
}


