package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.util.ArrayUtilities;
import de.gurkenlabs.utiliti.controller.WrapLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class TagPanel extends JPanel {
  private static final int MAX_TAG_LENGTH = 15;
  private final JTextField textFieldInput;

  public TagPanel() {
    setBorder(null);
    WrapLayout wrapLayout = new WrapLayout(FlowLayout.LEADING, 0, 0);
    this.addContainerListener(
        new ContainerListener() {

          @Override
          public void componentRemoved(ContainerEvent e) {
            fireActionPerformed();
          }

          @Override
          public void componentAdded(ContainerEvent e) {
            fireActionPerformed();
          }
        });

    this.setLayout(wrapLayout);

    this.textFieldInput = new JTextField();
    this.textFieldInput.setPreferredSize(new Dimension(6, PropertyPanel.CONTROL_HEIGHT));
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

          add(new Tag(tag));
          this.textFieldInput.setText(null);
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
            textFieldInput.setText(textFieldInput.getText().toLowerCase());

            final char c = e.getKeyChar();
            if (c == KeyEvent.VK_BACK_SPACE || c == KeyEvent.VK_DELETE) {
              return;
            }

            autoComplete();
          }
        });
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

      this.add(new Tag(tag));
    }

    // remove all tags that are no longer present
    for (Tag currentTag : this.getTags()) {
      if (!tags.contains(currentTag.getTag())) {
        this.remove(currentTag);
      }
    }

    this.revalidate();
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
