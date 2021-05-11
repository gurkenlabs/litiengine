package de.gurkenlabs.litiengine.gui;

import java.awt.Color;
import java.util.Objects;

public class SpeechBubbleAppearance extends Appearance {
  private Color borderColor;
  private float padding;
  private boolean renderIndicator = true;

  public SpeechBubbleAppearance() {
    super();
  }

  public SpeechBubbleAppearance(Color foreColor) {
    super(foreColor);
  }

  public SpeechBubbleAppearance(Color foreColor, Color backColor) {
    super(foreColor, backColor);
  }

  public SpeechBubbleAppearance(Color foreColor, Color backColor, Color borderColor) {
    super(foreColor, backColor);
    this.borderColor = borderColor;
  }

  public SpeechBubbleAppearance(
      Color foreColor, Color backColor, Color borderColor, float padding) {
    this(foreColor, backColor, borderColor);
    this.padding = padding;
  }

  @Override
  public Color getBorderColor() {
    return this.borderColor;
  }

  public float getPadding() {
    return this.padding;
  }

  public boolean isRenderIndicator() {
    return this.renderIndicator;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof SpeechBubbleAppearance) {
      return this.hashCode() == obj.hashCode();
    }

    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        this.getBorderColor(),
        this.getPadding(),
        this.isRenderIndicator(),
        this.getForeColor(),
        this.getBackgroundColor1(),
        this.getBackgroundColor2(),
        this.isHorizontalBackgroundGradient(),
        this.isTransparentBackground());
  }

  @Override
  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
    this.fireOnChangeEvent();
  }

  public void setPadding(float padding) {
    this.padding = padding;
    this.fireOnChangeEvent();
  }

  public void setRenderIndicator(boolean renderIndicator) {
    this.renderIndicator = renderIndicator;
    this.fireOnChangeEvent();
  }
}
