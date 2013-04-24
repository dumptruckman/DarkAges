package com.dumptruckman.minecraft.darkages.ability.special;

import com.dumptruckman.minecraft.darkages.DarkAgesPlugin;
import com.dumptruckman.minecraft.darkages.ability.Ability;
import com.dumptruckman.minecraft.darkages.ability.AbilityDetails;
import com.dumptruckman.minecraft.darkages.ability.AbilityInfo;
import com.dumptruckman.minecraft.pluginbase.minecraft.location.EntityCoordinates;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

@AbilityInfo(
        details = AbilityDetails.SOUL_STONE,
        description = "Teleports you to the\nplace of your last death.\nThis item remains on death.",
        retainOnDeath = true,
        allowDrop = true
)
public class SoulStone extends Ability {

    public SoulStone(final DarkAgesPlugin plugin) {
        super(plugin);
    }

    @Override
    protected int getLevel() {
        return 1;
    }

    @Override
    protected boolean canUseAbility(final Player player) {
        if (!plugin.getDeathHandler().deathLocation.containsKey(player.getName())) {
            player.sendMessage(ChatColor.RED + "You do not have a recent death to return to!");
            return false;
        }
        return true;
    }

    @Override
    protected boolean onAbilityUse(final Player player) {
        EntityCoordinates l = plugin.getDeathHandler().deathLocation.get(player.getName());
        Location loc = new Location(Bukkit.getWorld(l.getWorld()), l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());
        if (loc.getY() >= 0) {
            Block block = loc.getBlock();
            if (block.getType() == Material.LAVA || block.getType() == Material.STATIONARY_LAVA) {
                player.sendMessage(ChatColor.RED + "You might burn to death if you teleported there!");
                return false;
            }
        } else {
            player.sendMessage(ChatColor.RED + "You would die again if you teleported there!");
            return false;
        }

        if (player.getItemInHand().getAmount() > 1) {
            player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
        } else {
            player.setItemInHand(null);
        }

        player.teleport(loc);
        player.giveExp(plugin.getDeathHandler().expAtDeath.get(player.getName()));
        player.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + "You have returned to your death location...");

        plugin.getDeathHandler().deathLocation.remove(player.getName());
        plugin.getDeathHandler().expAtDeath.remove(player.getName());
        return true;
    }
}
