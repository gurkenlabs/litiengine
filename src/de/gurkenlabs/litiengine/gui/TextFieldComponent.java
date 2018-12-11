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

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.input.Input;

public class TextFieldComponent extends ImageComponent {
  public static final String DOUBLE_FORMAT = "[-+]?[0-9]*\\.?[0-9]*([eE][-+]?[0-9]*)?";
  public static final String INTEGER_FORMAT = "[0-9]{1,10}";
  private static final Logger log = Logger.getLogger(TextFieldComponent.class.getName());
  private final List<Consumer<String>> changeConfirmedConsumers;
  private boolean cursorVisible;
  private final int flickerDelay;
  private String format;

  private String fullText;
  private long lastToggled;
  private int maxLength = 0;

  public TextFieldComponent(final double x, final double y, final double width, final double height, final Spritesheet spritesheet, final String text) {
    super(x, y, width, height, spritesheet, text, null);
    this.changeConfirmedConsumers = new CopyOnWriteArrayList<>();
    this.setText(text);
    this.flickerDelay = 100;
    Input.keyboard().onKeyTyped(this::handleTypedKey);
    this.onClicked(e -> {
      if (!this.isSelected()) {
        this.toggleSelection();
      }
    });

    Input.mouse().onClicked(e -> {
      if (!this.getBoundingBox().contains(Input.mouse().getLocation())) {
        this.setSelected(false);
      }
    });

    this.setTextAlignment(Align.LEFT);
  }

  public String getFormat() {
    return this.format;
  }

  public int getMaxLength() {
    return this.maxLength;
  }

  @Override
  public String getText() {
    return this.fullText;
  }

  public void handleTypedKey(final KeyEvent event) {
    if (this.isSuspended() || !this.isSelected() || !this.isVisible() || !this.isEnabled()) {
      return;
    }

    switch (event.getKeyCode()) {
    case KeyEvent.VK_BACK_SPACE:
      this.handleBackSpace();
      break;
    case KeyEvent.VK_SPACE:
      if (!this.getText().equals("")) {
        this.setText(this.getText() + " ");
      }
      break;
    case KeyEvent.VK_ENTER:
      this.toggleSelection();
      this.changeConfirmedConsumers.forEach(c -> c.accept(this.getText()));

      log.log(Level.INFO, "{0} typed into TextField with ComponentID {1}", new Object[] { this.getText(), this.getComponentId() });
      break;
    default:
      this.handleNormalTyping(event);
      break;
    }
  }

  public void onChangeConfirmed(final Consumer<String> cons) {
    this.changeConfirmedConsumers.add(cons);
  }

  @Override
  public void render(final Graphics2D g) {
    super.render(g);
    g.setFont(this.getFont());
    final FontMetrics fm = g.getFontMetrics();

    if (this.isSelected() && Game.loop().getDeltaTime(this.lastToggled) > this.flickerDelay) {
      this.cursorVisible = !this.cursorVisible;
      this.lastToggled = Game.loop().getTicks();
    }
    if (this.isSelected() && this.cursorVisible) {
      final Rectangle2D cursor = new Rectangle2D.Double(this.getX() + this.getTextX() + fm.stringWidth(this.getTextToRender(g)), this.getY() + this.getTextY(), this.getFont().getSize2D() * 3 / 5, this.getFont().getSize2D() * 1 / 5);
      g.setColor(this.getAppearance().getForeColor());
      g.fill(cursor);
    }
  }

  public void setFormat(final String format) {
    this.format = format;
  }

  public void setMaxLength(final int maxLength) {
    this.maxLength = maxLength;
  }

  @Override
  public void setText(final String text) {
    this.fullText = text;
  }

  private void handleBackSpace() {
    if (Input.keyboard().isPressed(KeyEvent.VK_SHIFT)) {
      while (this.getText().length() >= 1 && this.getText().charAt(this.getText().length() - 1) == ' ') {
        this.setText(this.getText().substring(0, this.getText().length() - 1));
      }

      while (this.getText().length() >= 1 && this.getText().charAt(this.getText().length() - 1) != ' ') {
        this.setText(this.getText().substring(0, this.getText().length() - 1));
      }
    } else if (this.getText().length() >= 1) {
      this.setText(this.getText().substring(0, this.getText().length() - 1));
    }

    if (this.getFormat() != null && (this.getFormat() == INTEGER_FORMAT || this.getFormat() == DOUBLE_FORMAT) && (this.getText() == null || this.getText().isEmpty())) {
      this.setText("0");
    }
  }

  private void handleNormalTyping(KeyEvent event) {
    if (this.getMaxLength() > 0 && this.getText().length() >= this.getMaxLength()) {
      return;
    }

    final String text = Input.keyboard().getText(event);
    if (text == null || text.isEmpty()) {
      return;
    }

    // regex check to ensure certain formats
    if (this.getFormat() != null && !this.getFormat().isEmpty()) {
      final Pattern pat = Pattern.compile(this.getFormat());
      final Matcher mat = pat.matcher(this.getText() + text);
      if (!mat.matches()) {
        return;
      }
    }

    if (this.getFormat() != null && (this.getFormat() == INTEGER_FORMAT || this.getFormat() == DOUBLE_FORMAT) && this.getText().equals("0")) {
      this.setText("");
    }

    this.setText(this.getText() + text);
  }
}
