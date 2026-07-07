package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.util.ArrayUtilities;
import de.gurkenlabs.utiliti.controller.WrapLayout;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class TagPanel extends JPanel {
  private static final int MAX_TAG_LENGTH = 15;
  private static final int INLINE_INPUT_WIDTH = 120;
  private static final int INLINE_INPUT_COLLAPSED_WIDTH = 18;
  private final JTextField textFieldInput;

  private static final int PANEL_ARC = 8;

  public TagPanel() {
    setBackground(Style.COLOR_SURFACE2);
    setOpaque(false);
    setBorder(new RoundedBorder(Style.COLOR_BORDER, PANEL_ARC, 5));
    WrapLayout wrapLayout = new WrapLayout(FlowLayout.LEADING, 4, 0);
    this.addContainerListener(
        new ContainerListener() {

          @Override
          public void componentRemoved(ContainerEvent e) {
            updateTextFieldWidth();
            fireActionPerformed();
          }

          @Override
          public void componentAdded(ContainerEvent e) {
            updateTextFieldWidth();
            fireActionPerformed();
          }
        });

    this.setLayout(wrapLayout);

    this.textFieldInput = new JTextField();
    this.textFieldInput.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
    this.textFieldInput.setOpaque(false);
    this.textFieldInput.setForeground(Style.COLOR_TEXT);
    this.textFieldInput.setCaretColor(Style.COLOR_ACCENT_BLUE);
    this.textFieldInput.putClientProperty("JComponent.outline", "none");
    this.textFieldInput.setPreferredSize(new Dimension(INLINE_INPUT_WIDTH, Tag.CHIP_HEIGHT));
    this.textFieldInput.setMinimumSize(new Dimension(48, Tag.CHIP_HEIGHT));
    add(textFieldInput);
    this.textFieldInput.setColumns(7);
    this.textFieldInput.addActionListener(
        e -> {
          boolean isEmpty =
              this.textFieldInput.getText() == null
                  || this.textFieldInput.getText().trim().length() == 0;
          if (isEmpty) {
            this.textFieldInput.setText(null);
            return;
          }

          final String tag =
              this.textFieldInput.getText().trim().replaceAll("[^A-Za-z0-9\\-_]", "");
          if (this.containsTag(tag)) {
            this.textFieldInput.setText(null);
            return;
          }

          add(new Tag(tag), Math.max(0, getComponentCount() - 1));
          this.textFieldInput.setText(null);
          updateTextFieldWidth();
          this.revalidate();
        });

    this.textFieldInput.addKeyListener(
        new KeyAdapter() {
          @Override
          public void keyTyped(KeyEvent e) {
            // limit tags to MAX_TAG_LENGTH characters
            if (textFieldInput.getText() != null
                && textFieldInput.getText().length() >= MAX_TAG_LENGTH) {
              e.consume();
            }

            final char c = e.getKeyChar();

            if (!(Character.isAlphabetic(c)
                || Character.isDigit(c)
                || c == '_'
                || c == KeyEvent.VK_MINUS
                || c == KeyEvent.VK_BACK_SPACE
                || c == KeyEvent.VK_DELETE)) {
              e.consume();
            }

            // force lower case for tags
            if (Character.isAlphabetic(c)) {
              e.setKeyChar(Character.toLowerCase(e.getKeyChar()));
            }
          }

          @Override
          public void keyReleased(KeyEvent e) {
            String text = textFieldInput.getText();
            if (text != null) {
              textFieldInput.setText(text.toLowerCase());
            }

            final char c = e.getKeyChar();
            if (c == KeyEvent.VK_BACK_SPACE || c == KeyEvent.VK_DELETE) {
              return;
            }

            autoComplete();
          }
        });
  }

  @Override
  protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(getBackground());
    g2.fillRoundRect(0, 0, getWidth(), getHeight(), PANEL_ARC, PANEL_ARC);
    g2.dispose();
  }

  @Override
  public void revalidate() {
    if (this.getParent() != null) {
      this.getParent().revalidate();
    }
    super.revalidate();
    this.repaint();
  }

  public synchronized void addActionListener(ActionListener l) {
    this.listenerList.add(ActionListener.class, l);
  }

  public List<Tag> getTags() {
    List<Tag> tags = new ArrayList<>();

    for (Component comp : this.getComponents()) {
      if (comp instanceof Tag tag) {
        tags.add(tag);
      }
    }

    return tags;
  }

  public List<String> getTagStrings() {
    return this.getTags().stream().map(Tag::getTag).toList();
  }

  public String getTagsString() {
    return ArrayUtilities.join(this.getTags());
  }

  public void clear() {
    for (Tag tag : this.getTags()) {
      this.remove(tag);
    }

    this.textFieldInput.setText(null);
    updateTextFieldWidth();
    this.revalidate();
  }

  public void bind(String tagString) {
    if (tagString == null || tagString.trim().length() == 0) {
      this.clear();
      return;
    }

    this.textFieldInput.setText(null);
    String[] rawTags = tagString.split(",");
    List<String> tags = new ArrayList<>();
    for (String rawTag : rawTags) {
      final String tag = rawTag.trim().replaceAll("[^A-Za-z0-9\\-_]", "");
      tags.add(tag);
      if (this.containsTag(tag)) {
        continue;
      }

      this.add(new Tag(tag), Math.max(0, getComponentCount() - 1));
    }

    // remove all tags that are no longer present
    for (Tag currentTag : this.getTags()) {
      if (!tags.contains(currentTag.getTag())) {
        this.remove(currentTag);
      }
    }

    updateTextFieldWidth();
    this.revalidate();
  }

  private void updateTextFieldWidth() {
    if (this.textFieldInput == null) {
      return;
    }

    int width = this.getTags().isEmpty() ? INLINE_INPUT_WIDTH : INLINE_INPUT_COLLAPSED_WIDTH;
    Dimension size = new Dimension(width, Tag.CHIP_HEIGHT);
    this.textFieldInput.setPreferredSize(size);
    this.textFieldInput.setMinimumSize(size);
  }

  private boolean containsTag(String tag) {
    for (Tag t : this.getTags()) {
      if (t.getTag().equals(tag)) {
        return true;
      }
    }

    return false;
  }

  private void fireActionPerformed() {
    ActionEvent e = null;
    for (ActionListener listener : getListeners(ActionListener.class)) {
      if (e == null) {
        e =
            new ActionEvent(
                this, ActionEvent.ACTION_PERFORMED, null, System.currentTimeMillis(), 0);
      }

      listener.actionPerformed(e);
    }
  }

  private void autoComplete() {
    String autoCompletion = this.findAutoCompletion(this.textFieldInput.getText());
    if (autoCompletion == null) {
      return;
    }

    final int currentCaretPosition = this.textFieldInput.getCaretPosition();
    this.textFieldInput.setText(autoCompletion);
    this.validate();

    this.textFieldInput.setCaretPosition(this.textFieldInput.getText().length());
    this.textFieldInput.moveCaretPosition(currentCaretPosition);
  }

  private String findAutoCompletion(String currentText) {
    if (currentText == null || currentText.trim().length() == 0) {
      return null;
    }

    if (Game.world() == null || Game.world().environment() == null) {
      return null;
    }
    Optional<String> found =
        Game.world().environment().getUsedTags().stream()
            .filter(
                x -> x != null
                    && !this.getTagStrings().contains(x)
                    && x.startsWith(currentText.toLowerCase()))
            .findFirst();
    return found.orElse(null);
  }

}
