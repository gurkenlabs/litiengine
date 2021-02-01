package com.litiengine.environment.tilemap.xml;

import java.awt.Color;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.litiengine.Align;
import com.litiengine.Valign;
import com.litiengine.environment.tilemap.IMapObjectText;

public class Text implements IMapObjectText {
  @XmlAttribute
  private String fontfamily;

  @XmlAttribute
  private Integer pixelsize;

  @XmlAttribute
  private Integer wrap;

  @XmlAttribute
  @XmlJavaTypeAdapter(ColorAdapter.class)
  private Color color;

  @XmlAttribute
  private Integer bold;

  @XmlAttribute
  private Integer italic;

  @XmlAttribute
  private Integer underline;

  @XmlAttribute
  private Integer strikeout;

  @XmlAttribute
  private Integer kerning;

  @XmlAttribute
  private Align halign;

  @XmlAttribute
  private Valign valign;

  @XmlValue
  private String text;

  @Override
  public String getText() {
    return this.text;
  }

  @Override
  public Font getFont() {
    Map<TextAttribute, Object> properties = new HashMap<>();
    properties.put(TextAttribute.FAMILY, this.getFontName());
    properties.put(TextAttribute.SIZE, this.getPixelSize() * 0.75f); // pixels to points
    properties.put(TextAttribute.WEIGHT, this.isBold() ? TextAttribute.WEIGHT_BOLD : TextAttribute.WEIGHT_REGULAR);
    properties.put(TextAttribute.POSTURE, this.isItalic() ? TextAttribute.POSTURE_OBLIQUE : TextAttribute.POSTURE_REGULAR);
    properties.put(TextAttribute.UNDERLINE, this.isUnderlined() ? TextAttribute.UNDERLINE_ON : -1);
    properties.put(TextAttribute.STRIKETHROUGH, this.isStrikeout());
    properties.put(TextAttribute.KERNING, this.useKerning() ? TextAttribute.KERNING_ON : 0);
    return new Font(properties);
  }

  public String getFontName() {
    return this.fontfamily != null ? this.fontfamily : Font.SANS_SERIF;
  }

  public int getPixelSize() {
    return this.pixelsize != null ? this.pixelsize : 16;
  }

  @Override
  public boolean wrap() {
    return this.wrap != null && this.wrap != 0;
  }

  @Override
  public Color getColor() {
    return this.color != null ? this.color : Color.BLACK;
  }

  @Override
  public boolean isBold() {
    return this.bold != null && this.bold != 0;
  }

  @Override
  public boolean isItalic() {
    return this.italic != null && this.italic != 0;
  }

  @Override
  public boolean isUnderlined() {
    return this.underline != null && this.underline != 0;
  }

  @Override
  public boolean isStrikeout() {
    return this.strikeout != null && this.strikeout != 0;
  }

  @Override
  public boolean useKerning() {
    return this.kerning == null || this.kerning != 0;
  }

  @Override
  public Align getAlign() {
    return this.halign;
  }

  @Override
  public Valign getValign() {
    return this.valign;
  }
}
