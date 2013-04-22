package com.dumptruckman.minecraft.darkages.abilities.spells;

import com.dumptruckman.minecraft.darkages.Ability;
import com.dumptruckman.minecraft.darkages.AbilityInfo;
import com.dumptruckman.minecraft.darkages.DarkAgesPlugin;
import com.dumptruckman.minecraft.pluginbase.minecraft.location.EntityCoordinates;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

@AbilityInfo(
        name = "Soul Stone",
        magicColor = ChatColor.DARK_GRAY,
        description = "Teleports you to the\nplace of your last death.\nThis item remains on death.",
        permission = "darkages.abilities.spells.soulstone",
        material = Material.GHAST_TEAR
)
public class SoulStone extends Ability {

    private final DarkAgesPlugin plugin;

    public SoulStone(final DarkAgesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void useAbility(final PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission(abilityInfo.permission())) {
            return;
        }
        if (!plugin.getDeathHandler().deathLocation.containsKey(player.getName())) {
            player.sendMessage(ChatColor.RED + "You do not have a recent death to return to!");
            return;
        }
        if (player.getItemInHand().isSimilar(plugin.soulStoneItem)) {
            if (player.getItemInHand().getAmount() > 1) {
                player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
            } else {
                event.getPlayer().setItemInHand(null);
            }
            EntityCoordinates l = plugin.getDeathHandler().deathLocation.get(player.getName());
            player.teleport(new Location(Bukkit.getWorld(l.getWorld()), l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch()));
            player.giveExp(plugin.getDeathHandler().expAtDeath.get(player.getName()));
            player.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + "You have returned to your death location...");

            plugin.getDeathHandler().deathLocation.remove(player.getName());
            plugin.getDeathHandler().expAtDeath.remove(player.getName());
        }
    }
}
