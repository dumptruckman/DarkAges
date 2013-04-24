package com.dumptruckman.minecraft.darkages.ability;

import com.dumptruckman.minecraft.darkages.DarkAgesPlugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class PlayerSession {

    private final DarkAgesPlugin plugin;
    private final Player player;

    private LivingEntity target = null;
    private CastingTask castingTask = null;

    public PlayerSession(final DarkAgesPlugin plugin, final Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public LivingEntity getTarget() {
        return target;
    }

    public void setTarget(final LivingEntity target) {
        this.target = target;
    }

    public boolean isCasting() {
        return castingTask != null;
    }

    public CastingTask getCastingTask() {
        return castingTask;
    }

    public void beginCast(final Ability ability) {
        castingTask = new CastingTask(this, ability);
        if (ability.info.castTime() > 0) {
            try {
                //player.sendMessage(ChatColor.DARK_AQUA.toString() + ChatColor.ITALIC + "Casting in ");
                castingTask.runTaskLater(plugin, ability.info.castTime() * 20L);
            } catch (IllegalStateException e) {
                player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "Something went wrong!!!");
            }
        } else {
            castingTask.run();
            castingTask = null;
        }
    }

    public void endSession() {
        cancelCast();
    }

    public void cancelCast() {
        if (castingTask != null) {
            castingTask.cancel();
            castingTask = null;
        }
    }

    void clearCastTask() {
        castingTask = null;
    }
}
