package de.gurkenlabs.litiengine.graphics.animation;

import java.awt.Image;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import java.util.HashMap;
import java.util.Map;

import de.gurkenlabs.litiengine.graphics.animation.Animation;
import de.gurkenlabs.litiengine.graphics.animation.KeyFrame;
import de.gurkenlabs.litiengine.graphics.Spritesheet;

public class AsepriteHandler {

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


