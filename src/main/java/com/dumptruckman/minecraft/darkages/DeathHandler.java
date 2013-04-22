package com.dumptruckman.minecraft.darkages;

import com.dumptruckman.minecraft.pluginbase.minecraft.location.EntityCoordinates;
import com.dumptruckman.minecraft.pluginbase.minecraft.location.Locations;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DeathHandler implements Listener {

    @NotNull
    private final DarkAgesPlugin plugin;
    public final Map<String, Integer> expAtDeath = new HashMap<String, Integer>(Bukkit.getMaxPlayers());
    private final Map<String, ItemStack> soulStonesAtDeath = new HashMap<String, ItemStack>(Bukkit.getMaxPlayers());
    public final Map<String, EntityCoordinates> deathLocation = new HashMap<String, EntityCoordinates>(Bukkit.getMaxPlayers());

    public DeathHandler(@NotNull final DarkAgesPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void playerDeath(PlayerDeathEvent event) {
        int savedExp = (event.getEntity().getTotalExperience() / 2) - event.getDroppedExp();
        if (savedExp < 0) {
            savedExp = 0;
        }
        expAtDeath.put(event.getEntity().getName(), savedExp);
        int soulStonesAtDeath = 0;
        for (ItemStack item : event.getEntity().getInventory().getContents()) {
            if (item != null && item.isSimilar(plugin.soulStoneItem)) {
                soulStonesAtDeath += item.getAmount();
            }
        }
        Iterator<ItemStack> drops = event.getDrops().iterator();
        if (drops.hasNext()) {
            for (ItemStack item = drops.next(); drops.hasNext(); item = drops.next()) {
                if (item.isSimilar(plugin.soulStoneItem)) {
                    System.out.println("removing soulstone from drop.");
                    drops.remove();
                }
            }
        }
        if (soulStonesAtDeath > 0) {
            ItemStack soulStones = new ItemStack(plugin.soulStoneItem);
            soulStones.setAmount(soulStonesAtDeath);
            this.soulStonesAtDeath.put(event.getEntity().getName(), soulStones);
        }
        Location l = event.getEntity().getLocation();
        deathLocation.put(event.getEntity().getName(), Locations.getEntityCoordinates(l.getWorld().getName(), l.getX(), l.getY(), l.getZ(), l.getPitch(), l.getYaw()));
    }

    @EventHandler
    public void playerRespawn(PlayerRespawnEvent event) {
        if (soulStonesAtDeath.containsKey(event.getPlayer().getName())) {
            event.getPlayer().getInventory().addItem(soulStonesAtDeath.get(event.getPlayer().getName()));
            soulStonesAtDeath.remove(event.getPlayer().getName());
        }
    }
}
