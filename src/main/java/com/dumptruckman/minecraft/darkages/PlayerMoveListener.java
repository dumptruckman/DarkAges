package com.dumptruckman.minecraft.darkages;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {

    private final DarkAgesPlugin plugin;

    public PlayerMoveListener(final DarkAgesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void playerMove(final PlayerMoveEvent event) {
        final Location from = event.getFrom();
        final Location to = event.getTo();
        if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ() && from.getBlockY() == to.getBlockY()) {
            return;
        }


    }
}
