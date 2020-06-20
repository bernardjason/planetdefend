package org.bjason.game.planetdefend.desktop;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.utils.Logger;
import org.bjason.game.MainGame;

public class DesktopLauncher {
    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        Boolean small = true;
        if (small) {
            config.width = 1480;
            config.height = 800;
        }
        //config.fullscreen = true;
        config.title = "PlanetDefender";
        new LwjglApplication(new MainGame(), config);
        //Gdx.app.setLogLevel(Application.LOG_DEBUG);
        Gdx.app.setLogLevel(Application.LOG_NONE);
        if (!small) {
            Graphics.DisplayMode mode = Gdx.graphics.getDisplayMode();
            Gdx.graphics.setWindowedMode(mode.width, mode.height);
        }

    }
}
