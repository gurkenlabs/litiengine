package de.gurkenlabs.utiliti.swing;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.logging.Handler;
import java.util.logging.Logger;

public class ConsoleActionPanel extends JPanel {
    private static final Dimension BUTTON_SIZE = new Dimension(32, 32);

    private final JButton buttonClearConsole;
    private final JButton buttonScrollConsole;

    public ConsoleActionPanel(ConsoleComponent consoleComponent) {
        super();
        LayoutManager layout = new BoxLayout(this, BoxLayout.Y_AXIS);

        this.setLayout(layout);
        this.setVisible(true);
        this.setAlignmentY(Component.TOP_ALIGNMENT);

        this.buttonClearConsole = createButton(Icons.CLEAR_CONSOLE);
        buttonClearConsole.addActionListener((actionEvent -> {
            Arrays.stream(Logger.getLogger("").getHandlers())
                    .filter(handler -> handler instanceof LogHandler)
                    .findFirst().ifPresent(Handler::flush);
        }));

        this.buttonScrollConsole = createButton(Icons.SCROLL_DOWN);
        buttonScrollConsole.addActionListener((actionEvent -> {
            Arrays.stream(Logger.getLogger("").getHandlers())
                    .filter(handler -> handler instanceof LogHandler)
                    .findFirst().ifPresent(handler -> (((LogHandler) handler)).scrollToLast());
        }));

        this.add(buttonClearConsole);
        this.add(buttonScrollConsole);
    }

    private JButton createButton(Icon icon) {
        JButton button = new JButton("");
        button.setPreferredSize(BUTTON_SIZE);
        button.setMinimumSize(BUTTON_SIZE);
        button.setMaximumSize(BUTTON_SIZE);
        button.setIcon(icon);

        return button;
    }
}
