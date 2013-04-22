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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class DeathHandler implements Listener {

    @NotNull
    private final DarkAgesPlugin plugin;
    public final Map<String, Integer> expAtDeath = new HashMap<String, Integer>(Bukkit.getMaxPlayers() * 3);
    private final Map<String, Set<ItemStack>> retainedItemsAtDeath = new HashMap<String, Set<ItemStack>>(Bukkit.getMaxPlayers() * 3);
    public final Map<String, EntityCoordinates> deathLocation = new HashMap<String, EntityCoordinates>(Bukkit.getMaxPlayers() * 3);

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
        Set<ItemStack> retainedItems = new HashSet<ItemStack>(Ability.ABILITY_ITEMS.size());
        for (Map.Entry<ItemStack, Ability> ability : Ability.ABILITY_ITEMS.entrySet()) {
            if (ability.getValue().isRetainedOnDeath()) {
                ItemStack retainedItem = null;
                for (ItemStack item : event.getEntity().getInventory().getContents()) {
                    if (item != null && item.isSimilar(ability.getKey())) {
                        if (retainedItem == null) {
                            retainedItem = new ItemStack(ability.getKey());
                            retainedItem.setAmount(item.getAmount());
                        } else {
                            retainedItem.setAmount(retainedItem.getAmount() + item.getAmount());
                        }
                    }
                }
                if (retainedItem != null) {
                    retainedItems.add(retainedItem);
                }
            }
        }
        if (!retainedItems.isEmpty()) {
            retainedItemsAtDeath.put(event.getEntityType().getName(), retainedItems);
        }
        Iterator<ItemStack> drops = event.getDrops().iterator();
        if (drops.hasNext()) {
            for (ItemStack item = drops.next(); drops.hasNext(); item = drops.next()) {
                for (Map.Entry<ItemStack, Ability> ability : Ability.ABILITY_ITEMS.entrySet()) {
                    if ((ability.getValue().isDestroyedOnDeath() || ability.getValue().isRetainedOnDeath()) && item.isSimilar(ability.getKey())) {
                        drops.remove();
                    }
                }
            }
        }
        Location l = event.getEntity().getLocation();
        deathLocation.put(event.getEntity().getName(), Locations.getEntityCoordinates(l.getWorld().getName(), l.getX(), l.getY(), l.getZ(), l.getPitch(), l.getYaw()));
    }

    @EventHandler
    public void playerRespawn(PlayerRespawnEvent event) {
        if (retainedItemsAtDeath.containsKey(event.getPlayer().getName())) {
            for (ItemStack item : retainedItemsAtDeath.get(event.getPlayer().getName())) {
                event.getPlayer().getInventory().addItem(item);
            }
            retainedItemsAtDeath.remove(event.getPlayer().getName());
        }
    }
}
