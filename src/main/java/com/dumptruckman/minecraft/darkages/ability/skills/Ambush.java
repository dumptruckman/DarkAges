package com.dumptruckman.minecraft.darkages.ability.skills;

import com.dumptruckman.minecraft.darkages.DarkAgesPlugin;
import com.dumptruckman.minecraft.darkages.ability.Ability;
import com.dumptruckman.minecraft.darkages.ability.AbilityDetails;
import com.dumptruckman.minecraft.darkages.ability.AbilityInfo;
import com.dumptruckman.minecraft.darkages.util.BlockSafety;
import com.dumptruckman.minecraft.darkages.util.EntityTools;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@AbilityInfo(
        details = AbilityDetails.AMBUSH,
        description = "Teleports you to the other\nside of a nearby targeted enemy.",
        cooldown = 7,
        range = 7,
        inventoryLimit = 1,
        consumesAbilityItem = false,
        destroyedOnDeath = true,
        requiresTarget = true
)
public class Ambush extends Ability {

    public Ambush(final DarkAgesPlugin plugin) {
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
            player.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + "No target or not close enough!");
            return false;
        }
        plugin.getSession(player).setTarget(target);
        return true;
    }

    @Override
    protected boolean onAbilityUse(final Player player) {
        LivingEntity target = plugin.getSession(player).getTarget();
        Location tpLoc = null;
        Vector tVec = target.getLocation().toVector();
        Vector dir = player.getLocation().getDirection();
        dir = dir.setY(0).normalize();
        final World world = target.getWorld();
        Vector newVector = tVec.clone().add(dir);
        Block block = BlockSafety.getSafestBlock(world, newVector);
        if (block != null) {
            Vector newDir = dir.clone();
            double z = newDir.getZ();
            newDir.setZ(-newDir.getX());
            newDir.setX(z);
            tpLoc = getTeleportLocation(block, tVec, newVector, newDir);
        } else {
            Vector newDir = dir.clone();
            double z = newDir.getZ();
            newDir.setZ(newDir.getX());
            newDir.setX(-z);
            newVector = tVec.clone().add(newDir);
            block = BlockSafety.getSafestBlock(world, newVector);
            if (block != null) {
                z = newDir.getZ();
                newDir.setZ(-newDir.getX());
                newDir.setX(z);
                tpLoc = getTeleportLocation(block, tVec, newVector, newDir);
            } else {
                newDir = dir.clone();
                z = newDir.getZ();
                newDir.setZ(-newDir.getX());
                newDir.setX(z);
                newVector = tVec.clone().add(newDir);
                block = BlockSafety.getSafestBlock(world, newVector);
                if (block != null) {
                    z = newDir.getZ();
                    newDir.setZ(-newDir.getX());
                    newDir.setX(z);
                    tpLoc = getTeleportLocation(block, tVec, newVector, newDir);
                } else {
                    newVector = tVec.clone().subtract(dir);
                    block = BlockSafety.getSafestBlock(world, newVector);
                    if (block != null) {
                        newDir.setZ(-newDir.getZ());
                        newDir.setX(-newDir.getX());
                        tpLoc = getTeleportLocation(block, tVec, newVector, newDir);
                    }
                }
            }
        }
        if (tpLoc != null) {
            player.teleport(tpLoc);
        } else {
            player.sendMessage(ChatColor.RED + "No safe place to move to!");
            return false;
        }

        return true;
    }

    private Location getTeleportLocation(final Block block, final Vector targetVector, final Vector vector, Vector dir) {
        float pitch = 0F;
        if (block.getY() > targetVector.getY()) {
            pitch = 30F;
        } else if (block.getY() < targetVector.getY()) {
            pitch = -30F;
        }
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


}
