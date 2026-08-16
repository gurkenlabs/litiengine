package de.gurkenlabs.utiliti.view.dialogs;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import de.gurkenlabs.utiliti.view.components.GameScriptInspectorPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Project-level inspector for scripts attached to the game lifecycle. */
public final class GameScriptsDialog extends JDialog {
  private static GameScriptsDialog instance;
  private final GameScriptInspectorPanel scripts = new GameScriptInspectorPanel();

  public static void showDialog() {
    if (instance == null) instance = new GameScriptsDialog();
    instance.scripts.bindGame();
    instance.setLocationRelativeTo(owner());
    instance.setVisible(true);
    instance.toFront();
  }

  private GameScriptsDialog() {
    super(owner(), "Game Scripts", ModalityType.MODELESS);
    this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    this.setMinimumSize(new Dimension(560, 430));
    this.setSize(new Dimension(680, 540));
    this.setLocationRelativeTo(owner());
    this.getContentPane().setLayout(new BorderLayout());
    this.getContentPane().setBackground(Style.background());

    JPanel header = new JPanel(new BorderLayout(10, 0));
    header.setBackground(Style.surface());
    header.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createMatteBorder(0, 0, 1, 0, Style.border()),
      BorderFactory.createEmptyBorder(10, 14, 10, 14)));
    JLabel icon = new JLabel(Icons.PLAY_16);
    header.add(icon, BorderLayout.WEST);
    JPanel text = new JPanel(new java.awt.GridLayout(0, 1, 0, 2));
    text.setOpaque(false);
    JLabel title = new JLabel("Game Scripts");
    title.setFont(title.getFont().deriveFont(Font.BOLD, 13f));
    JLabel description = new JLabel("Scripts assigned here run for the global game lifecycle.");
    description.setForeground(Style.mutedText());
    text.add(title);
    text.add(description);
    header.add(text, BorderLayout.CENTER);
    this.add(header, BorderLayout.NORTH);

    JPanel content = new JPanel(new BorderLayout());
    content.setBackground(Style.background());
    content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    content.add(this.scripts, BorderLayout.CENTER);
    this.add(content, BorderLayout.CENTER);

    JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
    footer.setBackground(Style.surface());
    footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Style.border()));
    JButton close = new JButton("Close");
    close.addActionListener(event -> this.dispose());
    footer.add(close);
    this.add(footer, BorderLayout.SOUTH);
  }

  private static java.awt.Window owner() {
    return Game.window() == null || !(Game.window().getHostControl() instanceof Component component)
      ? null : javax.swing.SwingUtilities.getWindowAncestor(component);
  }
}
