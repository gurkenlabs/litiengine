package de.gurkenlabs.litiengine.gui;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Paint;
import java.awt.GradientPaint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class AppearanceTests {

    @Test
    public void testEquals(){
        Appearance appearance = new Appearance();
        Color color = Color.BLUE;
        assertFalse(appearance.equals(color));
    }

    @Test
    public void testGetBackgroundPaintBackground(){
        Appearance appearance = new Appearance(Color.BLUE, Color.RED);
        assertEquals(Color.RED,appearance.getBackgroundPaint(0,0));
    }

    @Test
    public void testGetBackgroundPaintBackgroundNull(){
        Appearance appearance = new Appearance(Color.BLUE, null);
        assertNull(appearance.getBackgroundPaint(0,0));
    }

    @Test
    public void testBackgroundTransparent(){
        Appearance appearance = new Appearance(null);
        assertNull(appearance.getBackgroundPaint(0,0));
    }

    @Test
    public void testGetBackgroundGradientTrue(){
        //arrange
        Appearance appearance = new Appearance(Color.RED, Color.BLUE);
        appearance.setHorizontalBackgroundGradient(true);
        appearance.setBackgroundColor2(Color.RED);

        //act
        Paint paint = appearance.getBackgroundPaint(100, 100);
        GradientPaint gp = (GradientPaint)paint;

        //assert
        assertEquals(Color.RED, gp.getColor2());
    }

    @Test
    public void testGetBackgroundGradientFalse(){
        //arrange
        Appearance appearance = new Appearance(Color.RED, Color.BLUE);
        appearance.setHorizontalBackgroundGradient(false);
        appearance.setBackgroundColor2(Color.RED);

        //act
        Paint paint = appearance.getBackgroundPaint(100, 100);
        GradientPaint gp = (GradientPaint)paint;

        //assert
        assertEquals(Color.RED, gp.getColor2());
    }

}

