package com.example.pixelgame.lwjgl3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.example.pixelgame.Main;

public class Lwjgl3Launcher {

    public static void main(String[] args) {

        if (StartupHelper.startNewJvmIfRequired()) {
            return;
        }

        createApplication();
    }

    private static Lwjgl3Application createApplication() {

        return new Lwjgl3Application(
                new Main(),
                getDefaultConfiguration()
        );
    }

    private static Lwjgl3ApplicationConfiguration
    getDefaultConfiguration() {

        Lwjgl3ApplicationConfiguration configuration =
                new Lwjgl3ApplicationConfiguration();

        configuration.setTitle("Deadlight");
        configuration.setWindowListener(new Lwjgl3WindowAdapter() {
            @Override public void focusLost() {
                if (Gdx.app != null) Gdx.app.getApplicationListener().pause();
            }
        });
        configuration.useVsync(true);

        configuration.setForegroundFPS(
                Lwjgl3ApplicationConfiguration
                        .getDisplayMode()
                        .refreshRate
        );

        configuration.setWindowedMode(
                1280,
                720
        );

        configuration.setWindowIcon(
                "libgdx128.png",
                "libgdx64.png",
                "libgdx32.png",
                "libgdx16.png"
        );

        return configuration;
    }
}