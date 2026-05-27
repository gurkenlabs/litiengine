package de.gurkenlabs.litiengine.gui;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.input.Input;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A single-line text input GUI component. Supports input filtering through a regex {@linkplain #getFormat() format}, a maximum length, a flickering
 * caret while focused, and confirmation listeners that fire when the user presses {@code ENTER}/{@code ESC} or clicks outside the component.
 */
public class TextFieldComponent extends ImageComponent {

  /**
   * Regex matching signed decimal numbers including scientific notation.
   */
  public static final String DOUBLE_FORMAT = "[-+]?[0-9]*\\.?[0-9]*([eE][-+]?[0-9]*)?";
  /**
   * Regex matching positive integers up to 10 digits.
   */
  public static final String INTEGER_FORMAT = "[0-9]{1,10}";
  private static final Logger log = Logger.getLogger(TextFieldComponent.class.getName());
  private final List<Consumer<String>> changeConfirmedConsumers;
  private boolean cursorVisible;
  private int flickerDelay = 300;
  private String format;
  private String cursor = "_";
  private String fullText;
  private long lastToggled;
  private int maxLength = 0;

  /**
   * Constructs a new {@code TextFieldComponent}.
   *
   * @param x      the x coordinate of the component
   * @param y      the y coordinate of the component
   * @param width  the width of the component
   * @param height the height of the component
   * @param text   the initial text content
   */
  public TextFieldComponent(
      final double x, final double y, final double width, final double height, final String text) {
    super(x, y, width, height, text);
    setFocusable(true);
    this.changeConfirmedConsumers = new CopyOnWriteArrayList<>();
    setText(text);
    Input.keyboard().onKeyTyped(this::handleTypedKey);
    onClicked(
        e -> {
          if (!isSelected()) {
            setSelected(true);
          }
        });

    Input.mouse()
        .onClicked(
            e -> {
              if (isSelected() && !getBoundingBox().contains(Input.mouse().getLocation())) {
                acceptInput();
              }
            });

    setTextAlign(Align.LEFT);
    setAutomaticLineBreaks(true);
  }

  /**
   * Gets the regex pattern used to filter typed input.
   *
   * @return the format pattern, or {@code null} if no filter is set
   */
  public String getFormat() {
    return format;
  }

  /**
   * Sets the regex pattern used to filter typed input.
   *
   * @param format the format pattern, or {@code null} to disable filtering
   */
  public void setFormat(final String format) {
    this.format = format;
  }

  /**
   * Gets the string rendered as the caret while the field is focused.
   *
   * @return the caret string
   */
  public String getCursor() {
    return cursor;
  }

  /**
   * Sets the string rendered as the caret while the field is focused.
   *
   * @param newCursor the caret string
   */
  public void setCursor(String newCursor) {
    this.cursor = newCursor;
  }

  /**
   * Gets the maximum number of characters allowed in the field, or {@code 0} if unlimited.
   *
   * @return the maximum length
   */
  public int getMaxLength() {
    return maxLength;
  }

  /**
   * Sets the maximum number of characters allowed in the field.
   *
   * @param maxLength the maximum length, or {@code 0} for unlimited
   */
  public void setMaxLength(final int maxLength) {
    this.maxLength = maxLength;
  }

  @Override
  public String getText() {
    return fullText;
  }

  @Override
  public void setText(final String text) {
    this.fullText = text;
  }

  @Override
  public String getTextToRender(Graphics2D g) {
    return isSelected() && cursorVisible ? super.getTextToRender(g) + getCursor()
        : super.getTextToRender(g) + "  ";
  }

  /**
   * Handles a key typed event by dispatching to the relevant editing routine (backspace, accept, or normal typing).
   *
   * @param event the key event
   */
  public void handleTypedKey(final KeyEvent event) {
    if (!canHandleInput()) {
      return;
    }
    switch (event.getKeyCode()) {
      case KeyEvent.VK_BACK_SPACE -> handleBackSpace();
      case KeyEvent.VK_ESCAPE, KeyEvent.VK_ENTER -> acceptInput();
      default -> handleNormalTyping(event);
    }
  }


  /**
   * Returns whether this component is currently in a state in which it should accept user input.
   *
   * @return {@code true} if input can be handled
   */
  public boolean canHandleInput() {
    return !isSuspended() && isSelected() && isVisible() && isEnabled();
  }

  /**
   * Registers a callback that is invoked with the current text whenever the user confirms the input (by pressing {@code ENTER}/{@code ESC} or
   * clicking outside the component).
   *
   * @param cons the consumer to register
   */
  public void onChangeConfirmed(final Consumer<String> cons) {
    changeConfirmedConsumers.add(cons);
  }

  @Override
  public void render(final Graphics2D g) {
    super.render(g);
    if (isSelected() && Game.time().since(lastToggled) >= flickerDelay) {
      this.cursorVisible = !this.cursorVisible;
      this.lastToggled = Game.time().now();
    }
  }

  /**
   * Sets the delay (in milliseconds) between caret flicker toggles.
   *
   * @param flickerDelayMillis the flicker delay
   */
  public void setFlickerDelay(int flickerDelayMillis) {
    this.flickerDelay = flickerDelayMillis;
  }

  private void acceptInput() {
    setSelected(false);
    changeConfirmedConsumers.forEach(c -> c.accept(getText()));
    log.log(
        Level.INFO,
        "{0} typed into TextField with ComponentID {1}",
        new Object[] {getText(), getComponentId()});
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
