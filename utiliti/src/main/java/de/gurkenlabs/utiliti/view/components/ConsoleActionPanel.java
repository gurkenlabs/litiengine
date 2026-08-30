package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.UriUtilities;
import de.gurkenlabs.utiliti.controller.LogHandler;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.net.URI;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class ConsoleActionPanel extends JPanel {
  private static final Dimension BUTTON_SIZE = new Dimension(32, 32);

  public ConsoleActionPanel(LogHandler logHandler) {
    super();
    LayoutManager layout = new BoxLayout(this, BoxLayout.Y_AXIS);

    this.setLayout(layout);
    this.setBackground(Style.assetExplorerBackground());
    this.setVisible(true); // Could be used to toggle the visibility of the action panel
    this.setAlignmentY(Component.TOP_ALIGNMENT);

    JButton buttonClearConsole =
        createButton(
            Icons.CLEAR_CONSOLE_24,
            Resources.strings().get("console_clear"),
            actionEvent -> logHandler.flush());

    JButton buttonScrollConsole =
        createButton(
            Icons.SCROLL_DOWN_24,
            Resources.strings().get("console_scroll_to_end"),
            actionEvent -> logHandler.scrollToLast());

    JButton buttonCreateBug =
        createButton(
            Icons.BUG_24,
            Resources.strings().get("console_create_bug"),
            actionEvent ->
                UriUtilities.openWebpage(
                    URI.create(Resources.strings().getFrom("links", "link_LITIengine_bug"))));
    JButton buttonCopyStack =
        createButton(
            Icons.COPY_24,
            Resources.strings().get("console_copy_error_stack"),
            actionEvent -> {
              String stack = logHandler.getLatestErrorStack();
              if (stack != null) {
                Toolkit.getDefaultToolkit()
                    .getSystemClipboard()
                    .setContents(new StringSelection(stack), null);
              }
            });

    Runnable updateErrorActions =
        () -> {
          Runnable update =
              () -> {
                boolean hasErrorStack = logHandler.getLatestErrorStack() != null;
                buttonCreateBug.setEnabled(hasErrorStack);
                buttonCopyStack.setEnabled(hasErrorStack);
              };
          if (SwingUtilities.isEventDispatchThread()) {
            update.run();
          } else {
            SwingUtilities.invokeLater(update);
          }
        };
    logHandler.addChangeListener(updateErrorActions);
    updateErrorActions.run();

    this.add(buttonClearConsole);
    this.add(buttonScrollConsole);
    this.add(buttonCreateBug);
    this.add(buttonCopyStack);
  }

  private JButton createButton(Icon icon, String tooltip, ActionListener actionListener) {
    JButton button = Style.iconButton(icon);
    button.setToolTipText(tooltip);
    button.addActionListener(actionListener);
    button.setPreferredSize(BUTTON_SIZE);
    button.setMinimumSize(BUTTON_SIZE);
    button.setMaximumSize(BUTTON_SIZE);

    return button;
  }
}
