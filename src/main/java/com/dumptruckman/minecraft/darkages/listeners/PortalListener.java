package com.dumptruckman.minecraft.darkages.listeners;

import com.dumptruckman.minecraft.darkages.DarkAgesPlugin;
import com.dumptruckman.minecraft.darkages.util.TownyLink;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.jetbrains.annotations.NotNull;

public class PortalListener implements Listener {

    @NotNull
    private final DarkAgesPlugin plugin;

    public PortalListener(@NotNull final DarkAgesPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void playerPortalEvent(PlayerPortalEvent event) {
        if (!plugin.getSession(event.getPlayer()).isAllowedToPortal()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void portalCreate(PortalCreateEvent event) {
        TownyLink link = plugin.getTownyLink();
        if (link != null) {
            for (Block block : event.getBlocks()) {
                if (!link.isOkayToBuildPortal(block)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
}
