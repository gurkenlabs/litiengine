package de.gurkenlabs.litiengine.gui;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.input.IKeyObserver;
import de.gurkenlabs.litiengine.input.Input;

public class TextFieldComponent extends ImageComponent implements IKeyObserver {
  public static final String INTEGER_FORMAT = "[0-9]{1,10}";
  public static final String DOUBLE_FORMAT = "[-+]?[0-9]*\\.?[0-9]*([eE][-+]?[0-9]*)?";
  private static final Logger log = Logger.getLogger(TextFieldComponent.class.getName());
  private final List<Consumer<String>> changeConfirmedConsumers;
  private boolean cursorVisible;
  private long lastToggled;
  private final int flickerDelay;

  private String fullText;
  private String lastText;
  private int maxLength = 0;
  private String format;

  public TextFieldComponent(final double x, final double y, final double width, final double height, final Spritesheet spritesheet, final String text) {
    super(x, y, width, height, spritesheet, text, null);
    this.changeConfirmedConsumers = new CopyOnWriteArrayList<>();
    this.setText(text);
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
    if (this.isSuspended() || !this.isSelected() || !this.isVisible()) {
      return;
    }

    switch (event.getKeyCode()) {
    case KeyEvent.VK_BACK_SPACE:
      if (Input.KEYBOARD.isPressed(KeyEvent.VK_SHIFT)) {
        while (this.getText().length() >= 1 && this.getText().charAt(this.getText().length() - 1) == ' ') {
          this.setText(this.getText().substring(0, this.getText().length() - 1));
        }

        while (this.getText().length() >= 1 && this.getText().charAt(this.getText().length() - 1) != ' ') {
          this.setText(this.getText().substring(0, this.getText().length() - 1));
        }
      } else if (this.getText().length() >= 1) {
        this.setText(this.getText().substring(0, this.getText().length() - 1));
      }

      if (this.getFormat() != null && (this.getFormat() == INTEGER_FORMAT || this.getFormat() == DOUBLE_FORMAT)) {
        if (this.getText() == null || this.getText().isEmpty()) {
          this.setText("0");
        }
      }

      break;
    case KeyEvent.VK_SPACE:
      if (this.getText() != "") {
        this.setText(getText() + " ");
      }
      break;
    case KeyEvent.VK_ENTER:
      this.toggleSelection();
      for (Consumer<String> cons : this.changeConfirmedConsumers) {
        cons.accept(this.getText());
      }
      log.log(Level.INFO, "\"" + this.getText() + "\"" + " typed into TextField with ComponentID " + this.getComponentId());
      break;
    default:
      if (this.getMaxLength() > 0 && this.getText().length() >= this.getMaxLength()) {
        break;
      }

      String text = Input.KEYBOARD.getText(event);
      if (text == null || text.isEmpty()) {
        break;
      }

      // regex check to ensure certain formats
      if (this.getFormat() != null && !this.getFormat().isEmpty()) {
        Pattern pat = Pattern.compile(this.getFormat());
        Matcher mat = pat.matcher(this.getText() + text);
        if (!mat.matches()) {
          break;
        }
      }

      if (this.getFormat() != null && (this.getFormat() == INTEGER_FORMAT || this.getFormat() == DOUBLE_FORMAT) && this.getText().equals("0")) {
        this.setText("");
      }

      this.setText(this.getText() + text);

      break;
    }
  }

  @Override
  public String getText() {
    return this.fullText;
  }

  @Override
  public void setText(String text) {
    this.fullText = text;
  }

  @Override
  public void render(final Graphics2D g) {
    g.setFont(this.getFont());
    final FontMetrics fm = g.getFontMetrics();
    if (this.lastText == null || !this.lastText.equals(this.getText())) {
      String newText = this.getText();
      while (newText.length() > 1 && fm.stringWidth(this.getText()) > this.getWidth() - this.getTextX() * 2) {
        newText = newText.substring(1);
      }

      this.setText(newText);
      this.lastText = this.getText();
    }

    super.render(g);

    if (this.isSelected() && Game.getLoop().getDeltaTime(this.lastToggled) > this.flickerDelay) {
      this.cursorVisible = !this.cursorVisible;
      this.lastToggled = Game.getLoop().getTicks();
    }
    if (this.isSelected() && this.cursorVisible) {
      final Rectangle2D cursor = new Rectangle2D.Double(this.getX() + this.getTextX() + fm.stringWidth(this.getText()), this.getY() + this.getTextY(), this.getFont().getSize2D() * 3 / 5, this.getFont().getSize2D() * 1 / 5);
      g.setColor(this.getTextColor());
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

  public void onChangeConfirmed(Consumer<String> cons) {
    this.changeConfirmedConsumers.add(cons);
  }

}
