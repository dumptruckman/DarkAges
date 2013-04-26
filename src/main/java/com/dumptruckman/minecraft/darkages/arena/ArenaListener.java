package com.dumptruckman.minecraft.darkages.arena;

import com.dumptruckman.minecraft.darkages.DarkAgesPlugin;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.managers.RegionManager;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ArenaListener implements Listener {

    @NotNull
    private final DarkAgesPlugin plugin;

    public ArenaListener(@NotNull final DarkAgesPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        final Player player = (Player) event.getEntity();
        if (event.getDamage() < player.getHealth()) {
            return;
        }

        if (handleDeath(player)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.DARK_RED + "You were killed!");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        final Player player = (Player) event.getEntity();
        if (event.getDamage() < player.getHealth()) {
            return;
        }

        if (handleDeath(player)) {
            event.setCancelled(true);
            LivingEntity killer = null;
            if (event.getDamager() instanceof Projectile) {
                Projectile projectile = (Projectile) event.getDamager();
                killer = projectile.getShooter();
            }
            if (killer == null) {
                if (event.getDamager() instanceof LivingEntity) {
                    killer = (LivingEntity) event.getDamager();
                }
            }
            String killerName = "unknown";
            if (killer != null) {
                if (killer instanceof Player) {
                    killerName = ((Player) killer).getName();
                } else {
                    killerName = killer.getType().getName();
                }
            }
            player.sendMessage(ChatColor.DARK_RED + "You were killed by " + ChatColor.ITALIC + killerName + ChatColor.DARK_RED +  "!");
        }
    }

    @EventHandler
    public void hungerChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        final Player player = (Player) event.getEntity();
        Location l = player.getLocation();
        RegionManager regionManager = plugin.getWorldGuard().getRegionManager(player.getWorld());
        List<String> regionNames = regionManager.getApplicableRegionsIDs(new Vector(l.getX(), l.getY(), l.getZ()));
        for (String regionName : regionNames) {
            Arena arena = plugin.getArena(regionName);
            if (arena != null) {
                event.setCancelled(true);
                return;
            }
        }
    }

    public boolean handleDeath(final Player player) {
        Location l = player.getLocation();
        RegionManager regionManager = plugin.getWorldGuard().getRegionManager(player.getWorld());
        List<String> regionNames = regionManager.getApplicableRegionsIDs(new Vector(l.getX(), l.getY(), l.getZ()));
        for (String regionName : regionNames) {
            Arena arena = plugin.getArena(regionName);
            if (arena != null) {
                player.setHealth(1);
                l = arena.getRespawnLocation();
                if (l == null) {
                    l = Bukkit.getWorlds().get(0).getSpawnLocation();
                }
                player.teleport(l);
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void interact(final NPCRightClickEvent event) {
        ArenaMasterTrait trait = event.getNPC().getTrait(ArenaMasterTrait.class);
        if (trait != null) {
            trait.onRightClick(event);
        }
    }
}
