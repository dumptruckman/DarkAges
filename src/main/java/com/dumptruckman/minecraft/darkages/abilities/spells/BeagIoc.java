package com.dumptruckman.minecraft.darkages.abilities.spells;

import com.dumptruckman.minecraft.darkages.Ability;
import com.dumptruckman.minecraft.darkages.AbilityInfo;
import com.dumptruckman.minecraft.darkages.DarkAgesPlugin;
import com.dumptruckman.minecraft.darkages.abilities.AbilityType;
import com.dumptruckman.minecraft.darkages.util.EntityTools;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@AbilityInfo(
        name = "Beag Ioc",
        magicColor = ChatColor.BLUE,
        type = AbilityType.SPELL,
        description = "Heals you or your target\nfor a small amount.",
        permission = "darkages.abilities.spells.beagioc",
        material = Material.POTION,
        levelCost = 5,
        usageComponents = Material.POTION,
        castTime = 1,
        consumesAbilityItem = true,
        allowDrop = true
)
public class BeagIoc extends Ability {

    private static final int RANGE = 50;
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
        LivingEntity target = EntityTools.getTargetedLivingEntity(player, RANGE);
        if (target == null) {
            player.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + "Targetting self...");
            target = player;
        } else {
            if (!(target instanceof Player)) {
                player.sendMessage(ChatColor.RED + "Can only target players!");
                return false;
            }
        }
        plugin.castingTargets.put(player.getName(), target);
        return true;
    }

    @Override
    protected boolean onAbilityUse(final Player player) {
        LivingEntity target = plugin.castingTargets.get(player.getName());
        if (target == null || !(target instanceof Player)) {
            player.sendMessage(ChatColor.RED + "Your target was lost!");
            return false;
        }
        int newHealth = target.getHealth();
        newHealth += HEAL_AMOUNT;
        if (newHealth > 20) {
            newHealth = 20;
        }
        target.setHealth(newHealth);
        ((Player) target).playEffect(target.getLocation(), Effect.GHAST_SHRIEK, 0);
        return true;
    }

    private Location getTeleportLocation(final Block block, final Vector targetVector, final Vector vector, Vector dir) {
        float pitch = 0F;
        if (block.getY() > targetVector.getY()) {
            pitch = 45F;
        } else if (block.getY() < targetVector.getY()) {
            pitch = -45F;
        }
        //float pitch = Double.valueOf(Math.asin(dir.getY()/Math.sqrt(dir.getZ() * dir.getZ() + dir.getX() * dir.getX()))).floatValue();
        //float yaw = Double.valueOf(Math.atan2(dir.getZ(), dir.getX())).floatValue();
        float yaw = getLookAtYaw(dir);
        return new Location(block.getWorld(), vector.getX(), block.getY(), vector.getZ(), yaw, pitch);
    }

    private static float getLookAtYaw(Vector motion) {
        double dx = motion.getX();
        double dz = motion.getZ();
        double yaw = 0;
        // Set yaw
        if (dx != 0) {
            // Set yaw start value based on dx
            if (dx < 0) {
                yaw = 1.5 * Math.PI;
            } else {
                yaw = 0.5 * Math.PI;
            }
            yaw -= Math.atan(dz / dx);
        } else if (dz < 0) {
            yaw = Math.PI;
        }
        return (float) (-yaw * 180 / Math.PI - 90);
    }

    private Block getSafestBlock(final World world, final Vector vector) {
        Block block = world.getBlockAt(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
        if (isBlockSafe(block)) {
            return block;
        }
        block = world.getBlockAt(vector.getBlockX(), vector.getBlockY() - 1, vector.getBlockZ());
        if (isBlockSafe(block)) {
            return block;
        }
        block = world.getBlockAt(vector.getBlockX(), vector.getBlockY() + 1, vector.getBlockZ());
        if (isBlockSafe(block)) {
            return block;
        }
        return null;
    }

    private boolean isBlockSafe(final Block block) {
        return !block.getType().isSolid()
                && !block.getType().isOccluding()
                && block.getType() != Material.LAVA
                && block.getType() != Material.STATIONARY_LAVA
                && block.getType() != Material.PORTAL
                && block.getType() != Material.ENDER_PORTAL
                && block.getRelative(BlockFace.DOWN).getType().isSolid();
    }

    //formula for pitch = Math.asin(deltaY/Math.sqrt(deltaZ * deltaZ + deltaX * deltaX));
    //formula for yaw = Math.atan2(deltaX, deltaZ);
}
