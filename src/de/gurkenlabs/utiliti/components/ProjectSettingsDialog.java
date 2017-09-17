package de.gurkenlabs.utiliti.components;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;

public class ProjectSettingsDialog extends JDialog {

  private final JPanel contentPanel = new JPanel();
  private JTextPane textPane;

  /**
   * Create the dialog.
   */
  public ProjectSettingsDialog() {
    setResizable(false);
    setTitle("Project properties");
    setBounds(100, 100, 490, 236);
    getContentPane().setLayout(new BorderLayout());
    contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    getContentPane().add(contentPanel, BorderLayout.CENTER);
    contentPanel.setLayout(null);

    JLabel lblSpriteFiles = new JLabel("Spritefile names");
    lblSpriteFiles.setBounds(10, 11, 98, 14);
    contentPanel.add(lblSpriteFiles);

    textPane = new JTextPane();
    textPane.setBounds(111, 11, 363, 113);
    contentPanel.add(textPane);
    JPanel buttonPane = new JPanel();
    buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
    getContentPane().add(buttonPane, BorderLayout.SOUTH);

    JButton okButton = new JButton("OK");
    okButton.addActionListener(a -> dispose());
    okButton.setActionCommand("OK");
    buttonPane.add(okButton);
    getRootPane().setDefaultButton(okButton);
  }

  public void set(List<String> spriteFiles) {
    StringBuilder sb = new StringBuilder();
    for (String sprite : spriteFiles) {
      sb.append(sprite + "\n");
    }

    this.textPane.setText(sb.toString());
  }

  public String[] getSpritefileNames() {
    String text = this.textPane.getText();
    if (text == null || text.isEmpty()) {
      return new String[] {};
    }

    return text.split("\\r?\\n");
  }
}
