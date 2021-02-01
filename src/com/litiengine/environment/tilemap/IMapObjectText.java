package com.litiengine.environment.tilemap;

import java.awt.Color;
import java.awt.Font;

import com.litiengine.Align;
import com.litiengine.Valign;

public interface IMapObjectText {
  public String getText();

  public Font getFont();

  public boolean wrap();

  public Color getColor();

  public Align getAlign();

  public Valign getValign();

  public boolean isBold();

  public boolean isItalic();

  public boolean isUnderlined();

  public boolean isStrikeout();

  public boolean useKerning();
}
