package com.dumptruckman.minecraft.darkages.abilities.skills;

import com.dumptruckman.minecraft.darkages.Ability;
import com.dumptruckman.minecraft.darkages.AbilityInfo;
import com.dumptruckman.minecraft.darkages.DarkAgesPlugin;
import com.dumptruckman.minecraft.darkages.util.EntityTools;
import com.dumptruckman.minecraft.pluginbase.minecraft.Entity;
import com.dumptruckman.minecraft.pluginbase.minecraft.location.EntityCoordinates;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;

@AbilityInfo(
        name = "Ambush",
        magicColor = ChatColor.WHITE,
        description = "Teleports you to the other\nside of a nearby targeted enemy.",
        permission = "darkages.abilities.skills.ambush",
        material = Material.ARROW,
        usageComponents = Material.ARROW,
        coolDown = 10,
        inventoryLimit = 1,
        consumesAbilityItem = false,
        destroyedOnDeath = true
)
public class Ambush extends Ability {

    public Ambush(final DarkAgesPlugin plugin) {
        super(plugin);
    }

    @Override
    protected boolean canUseAbility(final PlayerInteractEvent event) {
        return true;
    }

    @Override
    protected boolean onAbilityUse(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        LivingEntity target = EntityTools.getTargetedLivingEntity(player, 15);
        if (target == null) {
            player.sendMessage("No target or not close enough!");
            return false;
        }
        return true;
    }
}
