package de.gurkenlabs.litiengine.gui;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.input.Input;
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

public class TextFieldComponent extends ImageComponent {

  public static final String DOUBLE_FORMAT = "[-+]?[0-9]*\\.?[0-9]*([eE][-+]?[0-9]*)?";
  public static final String INTEGER_FORMAT = "[0-9]{1,10}";
  private static final Logger log = Logger.getLogger(TextFieldComponent.class.getName());
  private final List<Consumer<String>> changeConfirmedConsumers;
  private boolean cursorVisible;
  private int flickerDelay = 100;
  private String format;

  private String fullText;
  private long lastToggled;
  private int maxLength = 0;

  public TextFieldComponent(
    final double x, final double y, final double width, final double height, final String text) {
    super(x, y, width, height, text);
    this.changeConfirmedConsumers = new CopyOnWriteArrayList<>();
    setText(text);
    Input.keyboard().onKeyTyped(this::handleTypedKey);
    onClicked(
      e -> {
        if (!isSelected()) {
          toggleSelection();
        }
      });

    Input.mouse()
      .onClicked(
        e -> {
          if (!getBoundingBox().contains(Input.mouse().getLocation())) {
            setSelected(false);
          }
        });

    this.setTextAlign(Align.LEFT);
  }

  public String getFormat() {
    return format;
  }

  public int getMaxLength() {
    return maxLength;
  }

  @Override
  public String getText() {
    return fullText;
  }

  public void handleTypedKey(final KeyEvent event) {
    if (isSuspended() || !isSelected() || !isVisible() || !isEnabled()) {
      return;
    }

    switch (event.getKeyCode()) {
      case KeyEvent.VK_BACK_SPACE -> handleBackSpace();
      case KeyEvent.VK_ESCAPE, KeyEvent.VK_ENTER -> acceptInput();
      default -> handleNormalTyping(event);
    }
  }

  public void onChangeConfirmed(final Consumer<String> cons) {
    changeConfirmedConsumers.add(cons);
  }

  @Override
  public void render(final Graphics2D g) {
    super.render(g);
    g.setFont(getFont());
    final FontMetrics fm = g.getFontMetrics();
    if (this.isSelected() && Game.time().since(lastToggled) > flickerDelay) {
      this.cursorVisible = !this.cursorVisible;
      this.lastToggled = Game.time().now();
    }
    if (isSelected() && cursorVisible) {
      double textWidth = fm.stringWidth(this.getTextToRender(g));
      double textHeight = (double) fm.getAscent() + fm.getDescent();

      double xCoord =
        getTextAlign() != null ? getX() + getTextAlign().getLocation(getWidth(), textWidth)
          : getTextX();
      double yCoord =
        getTextValign() != null ? getY() + getTextValign().getLocation(getHeight(), textHeight)
          : getTextY();
      final Rectangle2D cursor = new Rectangle2D.Double(xCoord + fm.stringWidth(getTextToRender(g)),
        yCoord, getFont().getSize2D() * 1 / 5, getFont().getSize2D());
      g.setColor(getAppearance().getForeColor());
      g.fill(cursor);
    }
  }

  public void setFormat(final String format) {
    this.format = format;
  }

  public void setMaxLength(final int maxLength) {
    this.maxLength = maxLength;
  }

  public void setFlickerDelay(int flickerDelayMillis) {
    this.flickerDelay = flickerDelayMillis;
  }

  @Override
  public void setText(final String text) {
    this.fullText = text;
  }

  private void acceptInput() {
    toggleSelection();
    changeConfirmedConsumers.forEach(c -> c.accept(getText()));

    log.log(
      Level.INFO,
      "{0} typed into TextField with ComponentID {1}",
      new Object[]{getText(), getComponentId()});
  }

  private void handleBackSpace() {
    if (Input.keyboard().isPressed(KeyEvent.VK_SHIFT)) {
      while (getText().length() >= 1
        && getText().charAt(getText().length() - 1) == ' ') {
        setText(getText().substring(0, getText().length() - 1));
      }

      while (getText().length() >= 1
        && getText().charAt(getText().length() - 1) != ' ') {
        setText(getText().substring(0, getText().length() - 1));
      }
    } else if (getText().length() >= 1) {
      setText(getText().substring(0, getText().length() - 1));
    }

    if (isKnownNumericFormat() && (getText() == null || getText().isEmpty())) {
      setText("0");
    }
  }

  private void handleNormalTyping(KeyEvent event) {
    if (getMaxLength() > 0 && getText().length() >= getMaxLength()) {
      return;
    }

    final char text = event.getKeyChar();
    if (text == KeyEvent.CHAR_UNDEFINED) {
      return;
    }

    // regex check to ensure certain formats
    if (getFormat() != null && !getFormat().isEmpty()) {
      final Pattern pat = Pattern.compile(getFormat());
      final Matcher mat = pat.matcher(getText() + text);
      if (!mat.matches()) {
        return;
      }
    }

    if (isKnownNumericFormat() && getText().equals("0")) {
      setText("");
    }

    setText(getText() + text);
  }

  private boolean isKnownNumericFormat() {
    return getFormat() != null
      && (getFormat().equals(INTEGER_FORMAT) || getFormat().equals(DOUBLE_FORMAT));
  }
}
