package com.dumptruckman.minecraft.darkages;

import com.dumptruckman.minecraft.darkages.ability.Ability;
import com.dumptruckman.minecraft.darkages.ability.CastingTask;
import com.dumptruckman.minecraft.darkages.character.CharacterData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class PlayerSession {

    private class CancelPortalLimitation implements Runnable {
        @Override
        public void run() {
            castingBlock = null;
        }
    }

    private final DarkAgesPlugin plugin;
    private final Player player;

    private final CharacterData data;

    private LivingEntity target = null;
    private CastingTask castingTask = null;
    private BlockState preCastingBlockState = null;
    private Block castingBlock = null;
    private final CancelPortalLimitation portalLimitationCanceller = new CancelPortalLimitation();

    public PlayerSession(final DarkAgesPlugin plugin, final Player player, final CharacterData data) {
        this.plugin = plugin;
        this.player = player;
        this.data = data;
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
        if (ability.getInfo().castTime() > 0) {
            try {
                castingBlock = player.getLocation().getBlock();
                preCastingBlockState = castingBlock.getState();
                castingBlock.setTypeId(Material.PORTAL.getId(), false);
                castingBlock.getState().update(true);
                castingTask.runTaskLater(plugin, ability.getInfo().castTime() * 20L);
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
            clearCastTask();
        }
    }

    public void clearCastTask() {
        castingTask = null;
        if (preCastingBlockState != null) {
            preCastingBlockState.update(true);
        }
        preCastingBlockState = null;
        Bukkit.getScheduler().runTaskLater(plugin, portalLimitationCanceller, 1L);
    }

    public boolean isAllowedToPortal() {
        return !isCasting() && castingBlock == null;
    }

    public Block getCastingBlock() {
        return castingBlock;
    }

    public void regenerateHealthAndMana() {
        regenerateHealth();
        regenerateMana();
    }

    private void regenerateHealth() {
        int diff = data.getConstitution() - data.getLevel();
        if (diff > 10) {
            diff = 10;
        } else if (diff < 0) {
            diff = 0;
        }
        int health = (int) (data.getHealth() + (data.getMaxHealth() * (diff / 100D + .1D)));
        if (health > data.getMaxHealth()) {
            health = data.getMaxHealth();
        }
        data.setHealth(health);
    }

    private void regenerateMana() {
        int diff = data.getWisdom() - data.getLevel();
        if (diff > 10) {
            diff = 10;
        } else if (diff < 0) {
            diff = 0;
        }
        int mana = (int) (data.getMana() + (data.getMaxMana() * (diff / 100D + .1D)));
        if (mana > data.getMaxMana()) {
            mana = data.getMaxMana();
        }
        data.setMana(mana);
    }
}
