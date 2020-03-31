package de.gurkenlabs.litiengine.gui;

import de.gurkenlabs.litiengine.Game;

import java.awt.*;

public class GameRenderComponent extends GuiComponent {
    private int cameraIndex;

    public GameRenderComponent(double x, double y, double width, double height) {
        this(x, y, width, height, 0);

    }
    public GameRenderComponent(double x, double y, double width, double height, int cameraIndex) {
        super(x, y, width, height);
        this.cameraIndex = cameraIndex;
    }
    @Override
    public void render(final Graphics2D g) {

        Game.world().setActiveCameraIndex(cameraIndex);
        if (Game.world().environment() != null) {
            Game.world().environment().render(g);
        }

        super.render(g);
    }

    public int getCameraIndex() {
        return cameraIndex;
    }
    public void setCameraIndex(int cameraIndex) {
        this.cameraIndex = cameraIndex;
    }
}
