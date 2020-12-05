package de.gurkenlabs.utiliti.swing;

import javax.swing.JPanel;
import javax.swing.BoxLayout;
import java.awt.LayoutManager;

public class ConsoleComponent extends JPanel {

    public ConsoleComponent() {
        super();
        LayoutManager layout = new BoxLayout(this, BoxLayout.X_AXIS);
        this.setLayout(layout);

        ConsolePanel consolePanel = new ConsolePanel();

        this.add(new ConsoleActionPanel());
        this.add(consolePanel);
    }
}
