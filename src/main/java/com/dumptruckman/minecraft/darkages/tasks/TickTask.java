package com.dumptruckman.minecraft.darkages.tasks;

import com.dumptruckman.minecraft.darkages.DarkAgesPlugin;
import com.dumptruckman.minecraft.darkages.PlayerSession;
import org.bukkit.scheduler.BukkitRunnable;

public class TickTask extends BukkitRunnable {

    private final DarkAgesPlugin plugin;

    private long lastRegen;

    private static final long REGEN_PERIOD = 20000L;

    public TickTask(final DarkAgesPlugin plugin) {
        this.plugin = plugin;
        lastRegen = System.currentTimeMillis();
    }

    @Override
    public void run() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastRegen >= REGEN_PERIOD) {
            lastRegen = currentTime;
            for (PlayerSession session : plugin.getPlayerSessions()) {
                session.regenerateHealthAndMana();
            }
        }
    }
}
