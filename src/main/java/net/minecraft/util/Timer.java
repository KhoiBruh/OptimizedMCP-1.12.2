package net.minecraft.util;

import net.minecraft.client.Minecraft;

public class Timer
{
    /**
     * How many full ticks have turned over since the last call to updateTimer(), capped at 10.
     */
    public int elapsedTicks;

    /**
     * How much time has elapsed since the last tick, in ticks, for use by display rendering routines (range: 0.0 -
     * 1.0).
     */
    public float renderPartialTicks;

    /**
     * How much time has elapsed since the last tick, in ticks (range: 0.0 - 1.0).
     */
    public float elapsedPartialTicks;

    /**
     * The time reported by the system clock at the last sync, in milliseconds
     */
    private long lastSyncSysClock;

    /**
     * The Length of a single tick in milliseconds. Calculated as 1000/tps. At a default 20 TPS, tickLength is 50 ms
     */
    private float tickLength;

    public Timer(float tps)
    {
        tickLength = 1000.0F / tps;
        lastSyncSysClock = Minecraft.getSystemTime();
    }

    /**
     * Updates all fields of the Timer using the current time
     */
    public void updateTimer()
    {
        long i = Minecraft.getSystemTime();
        elapsedPartialTicks = (float)(i - lastSyncSysClock) / tickLength;
        lastSyncSysClock = i;
        renderPartialTicks += elapsedPartialTicks;
        elapsedTicks = (int) renderPartialTicks;
        renderPartialTicks -= (float) elapsedTicks;
    }
}
