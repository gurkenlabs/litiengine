package de.gurkenlabs.utiliti.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.logging.Handler;
import java.util.logging.Logger;

public class ConsoleActionPanel extends JPanel {
    private static final Dimension BUTTON_SIZE = new Dimension(32, 32);

    public ConsoleActionPanel() {
        super();
        LayoutManager layout = new BoxLayout(this, BoxLayout.Y_AXIS);

        this.setLayout(layout);
        this.setVisible(true); // Could be used to toggle the visibility of the action panel
        this.setAlignmentY(Component.TOP_ALIGNMENT);

        JButton buttonClearConsole = createButton(Icons.CLEAR_CONSOLE, (actionEvent ->
                Arrays.stream(Logger.getLogger("").getHandlers())
                        .filter(handler -> handler instanceof LogHandler)
                        .findFirst().ifPresent(Handler::flush)));

        JButton buttonScrollConsole = createButton(Icons.SCROLL_DOWN, (actionEvent ->
                Arrays.stream(Logger.getLogger("").getHandlers())
                        .filter(handler -> handler instanceof LogHandler)
                        .findFirst().ifPresent(handler -> (((LogHandler) handler)).scrollToLast())));

        this.add(buttonClearConsole);
        this.add(buttonScrollConsole);
    }

    private JButton createButton(Icon icon, ActionListener actionListener) {
        JButton button = new JButton("");
        button.setPreferredSize(BUTTON_SIZE);
        button.setMinimumSize(BUTTON_SIZE);
        button.setMaximumSize(BUTTON_SIZE);
        button.setIcon(icon);
        button.addActionListener(actionListener);

        return button;
    }
}
