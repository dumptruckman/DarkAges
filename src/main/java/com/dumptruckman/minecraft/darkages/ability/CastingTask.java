package com.dumptruckman.minecraft.darkages.ability;

import com.dumptruckman.minecraft.darkages.PlayerSession;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class CastingTask extends BukkitRunnable {

    private static final PotionEffectType CAST_EFFECT = PotionEffectType.CONFUSION;

    private final PlayerSession playerSession;
    private final Ability ability;
    private final boolean confusedBeforeCasting;
    private final boolean hasCastTime;

    public CastingTask(final PlayerSession playerSession, final Ability ability) {
        this.playerSession = playerSession;
        this.ability = ability;
        this.confusedBeforeCasting = getPlayer().hasPotionEffect(CAST_EFFECT);
        this.hasCastTime = ability.info.castTime() > 0;
        if (hasCastTime) {
            getPlayer().playSound(getPlayer().getLocation(), Sound.PORTAL, 0.7F, 0.5F);
            getPlayer().addPotionEffect(new PotionEffect(CAST_EFFECT, ability.info.castTime() * 20, 1, true));
        }
    }

    public Ability getCastingAbility() {
        return ability;
    }

    private Player getPlayer() {
        return playerSession.getPlayer();
    }

    @Override
    public synchronized void cancel() {
        try {
            super.cancel();
            endCasting();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            getPlayer().sendMessage(ChatColor.RED + "An error occurred when canceling your cast.  Please tell dumptruckman!");
        }
    }

    @Override
    public void run() {
        if (hasCastTime && ability.info.requiresTarget() && playerSession.getTarget() == null) {
            getPlayer().sendMessage(ChatColor.RED + "Your target was lost!");
            endCasting();
            return;
        }
        if (ability.onAbilityUse(getPlayer())) {
            ability.abilityUsed(getPlayer());
        }
        endCasting();
    }

    private void endCasting() {
        playerSession.setTarget(null);
        if (hasCastTime && !confusedBeforeCasting) {
            getPlayer().removePotionEffect(CAST_EFFECT);
        }
        playerSession.clearCastTask();
    }
}