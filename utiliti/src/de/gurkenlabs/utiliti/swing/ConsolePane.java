package de.gurkenlabs.utiliti.swing;

import javax.swing.*;
import java.awt.*;

public class ConsolePane extends JPanel {

    public ConsolePane() {
        super();
        LayoutManager layout = new BoxLayout(this, BoxLayout.X_AXIS);
        this.setLayout(layout);
        this.setVisible(true);

        ConsoleComponent consoleComponent = new ConsoleComponent();

        this.add(new ConsoleActionPanel(consoleComponent));
        this.add(consoleComponent);
    }
}
