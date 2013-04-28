package com.dumptruckman.minecraft.darkages.arena;

import com.dumptruckman.minecraft.darkages.DarkAgesPlugin;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.potion.PotionEffect;
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

        Location l = player.getLocation();
        RegionManager regionManager = plugin.getWorldGuard().getRegionManager(player.getWorld());
        List<String> regionNames = regionManager.getApplicableRegionsIDs(new Vector(l.getX(), l.getY(), l.getZ()));
        Arena arena = null;
        for (String regionName : regionNames) {
            arena = plugin.getArena(regionName);
            if (arena != null) {
                break;
            }
        }

        if (arena != null) {
            String killerName = null;
            if (event instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent edbe = (EntityDamageByEntityEvent) event;
                LivingEntity killer = null;
                if (edbe.getDamager() instanceof Projectile) {
                    Projectile projectile = (Projectile) edbe.getDamager();
                    killer = projectile.getShooter();
                }
                if (killer == null) {
                    if (edbe.getDamager() instanceof LivingEntity) {
                        killer = (LivingEntity) edbe.getDamager();
                    }
                }
                if (killer != null) {
                    if (killer instanceof Player) {
                        killerName = ((Player) killer).getDisplayName();
                    } else {
                        killerName = killer.getType().getName();
                    }
                }
            } else if (event instanceof EntityDamageByBlockEvent) {
                EntityDamageByBlockEvent edbb = (EntityDamageByBlockEvent) event;
                killerName = edbb.getDamager().getType().name().toLowerCase().replaceAll("_", " ");
            }

            arenaDeathMessage(player.getWorld(), regionManager.getRegion(arena.getRegionId()), player.getDisplayName(), killerName);

            player.setHealth(1);
            l = arena.getRespawnLocation();
            if (l == null) {
                l = Bukkit.getWorlds().get(0).getSpawnLocation();
            }
            player.teleport(l);
            player.setFireTicks(0);
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
            event.setCancelled(true);
        }
    }

    private void arenaDeathMessage(World world, ProtectedRegion arenaRegion, String deadGuy, String killer) {
        for (Player player : world.getPlayers()) {
            Location l = player.getLocation();
            if (arenaRegion.contains(new Vector(l.getX(), l.getY(), l.getZ()))) {
                if (killer != null) {
                    player.sendMessage(ChatColor.DARK_RED + deadGuy + " was killed by " + killer + "!");
                } else {
                    player.sendMessage(ChatColor.DARK_RED + deadGuy + " was killed!");
                }
            }
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
                player.setSaturation(20F);
                return;
            }
        }
    }

    @EventHandler
    public void interact(final NPCRightClickEvent event) {
        ArenaMasterTrait trait = event.getNPC().getTrait(ArenaMasterTrait.class);
        if (trait != null) {
            trait.onRightClick(event);
        }
    }
}
