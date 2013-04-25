package com.dumptruckman.minecraft.darkages.ability.skills;

import com.dumptruckman.minecraft.darkages.DarkAgesPlugin;
import com.dumptruckman.minecraft.darkages.ability.Ability;
import com.dumptruckman.minecraft.darkages.ability.AbilityDetails;
import com.dumptruckman.minecraft.darkages.ability.AbilityInfo;
import com.dumptruckman.minecraft.darkages.util.BlockSafety;
import com.dumptruckman.minecraft.darkages.util.EntityTools;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@AbilityInfo(
        details = AbilityDetails.SHADOW_FIGURE,
        description = "Teleports you to the other\nside of a very close enemy.\nDeals light damage.",
        range = 2,
        inventoryLimit = 1,
        consumesAbilityItem = false,
        destroyedOnDeath = true,
        requiresTarget = true
)
public class ShadowFigure extends Ambush {

    public ShadowFigure(final DarkAgesPlugin plugin) {
        super(plugin);
    }

    @Override
    protected int getLevel() {
        return 5;
    }

    @Override
    protected boolean onAbilityUse(final Player player) {
        if (super.onAbilityUse(player)) {
            LivingEntity target = plugin.getSession(player).getTarget();
            target.damage(1, player);
            return true;
        }
        return false;
    }
}
