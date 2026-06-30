package org.lwjgl;

import java.awt.Desktop;
import java.net.URI;

public final class Sys {

    private static final long START_TIME = System.nanoTime();
    private static final long TIMER_RESOLUTION = 1_000_000_000L;

    private Sys() {}

    public static long getTime() {
        return System.nanoTime() - START_TIME;
    }

    public static long getTimerResolution() {
        return TIMER_RESOLUTION;
    }

    public static String getVersion() {
        return "3.3.3";
    }

    public static void openURL(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception ignored) {
        }
    }

}
