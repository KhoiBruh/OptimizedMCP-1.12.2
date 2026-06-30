package org.lwjgl;

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

}
