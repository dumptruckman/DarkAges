package com.dumptruckman.minecraft.darkages.ability.spells;

import com.dumptruckman.minecraft.darkages.DarkAgesPlugin;
import com.dumptruckman.minecraft.darkages.ability.Ability;
import com.dumptruckman.minecraft.darkages.ability.AbilityDetails;
import com.dumptruckman.minecraft.darkages.ability.AbilityInfo;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

@AbilityInfo(
        details = AbilityDetails.DACHAIDH,
        description = "Teleports you home",
        castTime = 2,
        consumesAbilityItem = true,
        allowDrop = true
)
public class Dachaidh extends Ability {

    public Dachaidh(final DarkAgesPlugin plugin) {
        super(plugin);
    }

    @Override
    protected int getLevel() {
        return 1;
    }

    @Override
    protected boolean canUseAbility(final Player player) {
        return true;
    }

    @Override
    protected boolean onAbilityUse(final Player player) {
        Location loc = player.getBedSpawnLocation();
        if (loc == null && plugin.getTownyLink() != null) {
            loc = plugin.getTownyLink().getTownSpawn(player);
        }
        if (loc == null) {
            loc = Bukkit.getWorlds().get(0).getSpawnLocation();
        }
        player.playSound(player.getLocation(), Sound.PORTAL_TRAVEL, 0.5F, 0.5F);
        player.teleport(loc);
        player.playSound(loc, Sound.PORTAL_TRAVEL, 0.5F, 0.5F);
        return true;
    }

}
