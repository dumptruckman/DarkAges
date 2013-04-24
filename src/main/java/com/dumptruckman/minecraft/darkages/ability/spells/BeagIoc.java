package com.dumptruckman.minecraft.darkages.ability.spells;

import com.dumptruckman.minecraft.darkages.DarkAgesPlugin;
import com.dumptruckman.minecraft.darkages.ability.Ability;
import com.dumptruckman.minecraft.darkages.ability.AbilityDetails;
import com.dumptruckman.minecraft.darkages.ability.AbilityInfo;
import com.dumptruckman.minecraft.darkages.util.EntityTools;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

@AbilityInfo(
        details = AbilityDetails.BEAG_IOC,
        description = "Heals you or another player\nfor a small amount.",
        range = 51,
        castTime = 1,
        consumesAbilityItem = true,
        allowDrop = true,
        requiresTarget = true
)
public class BeagIoc extends Ability {

    private static final int HEAL_AMOUNT = 2;

    public BeagIoc(final DarkAgesPlugin plugin) {
        super(plugin);
    }

    @Override
    protected int getLevel() {
        return 1;
    }

    @Override
    protected boolean canUseAbility(final Player player) {
        LivingEntity target = EntityTools.getTargetedLivingEntity(player, info.range());
        if (target == null) {
            target = player;
        } else {
            if (!(target instanceof Player)) {
                target = player;
                //player.sendMessage(ChatColor.RED + "Can only target players!");
                //return false;
            }
        }
        if (target == player) {
            player.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + "Targeting self...");
        } else {
            player.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + "Targeting " + ((Player) target).getName() + "...");
        }
        plugin.getSession(player).setTarget(target);
        return true;
    }

    @Override
    protected boolean onAbilityUse(final Player player) {
        LivingEntity target = plugin.getSession(player).getTarget();
        int newHealth = target.getHealth();
        newHealth += HEAL_AMOUNT;
        if (newHealth > 20) {
            newHealth = 20;
        }
        target.setHealth(newHealth);
        ((Player) target).playSound(target.getLocation(), Sound.ORB_PICKUP, 0.7F, 0.5F);
        return true;
    }

}
