package de.gurkenlabs.litiengine.gui;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.input.IKeyObserver;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.sound.Sound;

public class TextFieldComponent extends ImageComponent implements IKeyObserver {
  public static final String INTEGER_FORMAT = "[0-9]+";
  public static final String DOUBLE_FORMAT = "[-+]?[0-9]*\\.?[0-9]*([eE][-+]?[0-9]*)?";
  private static final Logger log = Logger.getLogger(TextFieldComponent.class.getName());

  private boolean cursorVisible;
  private long lastToggled;
  private final int textXOffset, flickerDelay;

  private String fullText;
  private int maxLength = 0;
  private String format;

  public TextFieldComponent(final int x, final int y, final int width, final int height, final Spritesheet spritesheet, final String text, final Sound hoverSound) {
    super(x, y, width, height, spritesheet, text, null, hoverSound);
    this.fullText = this.getText();
    this.textXOffset = (int) (this.getWidth() / 10);
    this.flickerDelay = 100;
    Input.KEYBOARD.registerForKeyDownEvents(this);

    this.onClicked(e -> {
      if (!this.isSelected()) {
        this.toggleSelection();
      }
    });

    Input.MOUSE.onClicked(e -> {
      if (!this.getBoundingBox().contains(Input.MOUSE.getLocation())) {
        this.setSelected(false);
      }
    });

    this.setTextAlignment(GuiComponent.TEXT_ALIGN_LEFT);
  }

  @Override
  public void handlePressedKey(final KeyEvent keyCode) {
    // TODO Auto-generated method stub

  }

  @Override
  public void handleReleasedKey(final KeyEvent keyCode) {
    // TODO Auto-generated method stub

  }

  @Override
  public void handleTypedKey(final KeyEvent event) {
    if (!this.isSelected()) {
      return;
    }

    switch (event.getKeyCode()) {
    case KeyEvent.VK_BACK_SPACE:
      if (Input.KEYBOARD.isPressed(KeyEvent.VK_SHIFT)) {
        while (this.fullText.length() >= 1 && this.fullText.charAt(this.fullText.length() - 1) == ' ') {
          this.fullText = this.fullText.substring(0, this.fullText.length() - 1);
        }

        while (this.fullText.length() >= 1 && this.fullText.charAt(this.fullText.length() - 1) != ' ') {
          this.fullText = this.fullText.substring(0, this.fullText.length() - 1);
        }
      } else if (this.fullText.length() >= 1) {
        this.fullText = this.fullText.substring(0, this.fullText.length() - 1);
      }

      break;
    case KeyEvent.VK_SPACE:
      if (this.fullText != "") {
        this.fullText += " ";
      }
      break;
    case KeyEvent.VK_ENTER:
      this.toggleSelection();
      log.info("Typed \"" + this.getText() + "\" into TextField with ComponentID " + this.getComponentId());
      break;
    default:
      if (this.getMaxLength() > 0 && this.fullText.length() >= this.getMaxLength()) {
        break;
      }

      String text = Input.KEYBOARD.getText(event);
      if(text == null || text.isEmpty()){
        break;
      }
      
      // regex check to ensure certain formats
      if (this.getFormat() != null && !this.getFormat().isEmpty()) {
        Pattern pat = Pattern.compile(this.getFormat());
        Matcher mat = pat.matcher(this.fullText + text);
        if (!mat.matches()) {
          break;
        }
      }

      this.fullText += text;

      break;
    }
  }

  @Override
  public void render(final Graphics2D g) {
    g.setFont(this.getFont());
    final FontMetrics fm = g.getFontMetrics();
    this.setText(this.fullText);
    while (fm.stringWidth(this.getText()) > this.getWidth() - this.textXOffset) {
      this.setText(this.getText().substring(1));
    }

    super.render(g);

    if (this.isSelected() && Game.getLoop().getDeltaTime(this.lastToggled) > this.flickerDelay) {
      this.cursorVisible = !this.cursorVisible;
      this.lastToggled = Game.getLoop().getTicks();
    }

    if (this.isSelected() && this.cursorVisible) {
      final Rectangle2D cursor = new Rectangle2D.Double(this.getX() + this.textXOffset / 2 + fm.stringWidth(this.getText()), this.getY() + fm.getAscent() + (this.getHeight() - (fm.getAscent() + fm.getDescent())) / 2, this.getWidth() / 20,
          this.getHeight() - 2 * (fm.getAscent() + (this.getHeight() - (fm.getAscent() + fm.getDescent())) / 2));
      g.fill(cursor);
    }
  }

  public int getMaxLength() {
    return maxLength;
  }

  public void setMaxLength(int maxLength) {
    this.maxLength = maxLength;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

}
