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
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

@AbilityInfo(
        name = "Ambush",
        magicColor = ChatColor.WHITE,
        description = "Teleports you to the other\nside of a nearby targeted enemy.",
        permission = "darkages.abilities.skills.ambush",
        material = Material.ARROW,
        usageComponents = Material.ARROW,
        coolDown = 8,
        inventoryLimit = 1,
        consumesAbilityItem = false,
        destroyedOnDeath = true
)
public class Ambush extends Ability {

    private static final int RANGE = 9;

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
        LivingEntity target = EntityTools.getTargetedLivingEntity(player, RANGE);
        if (target == null) {
            player.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + "No target or not close enough!");
            return false;
        }
        Location tpLoc = null;
        Vector tVec = target.getLocation().toVector();
        Vector dir = player.getLocation().getDirection();
        dir = dir.setY(0).normalize();
        final World world = target.getWorld();
        Vector newVector = tVec.clone().add(dir);
        Block block = getSafestBlock(world, newVector);
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
            block = getSafestBlock(world, newVector);
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
                block = getSafestBlock(world, newVector);
                if (block != null) {
                    z = newDir.getZ();
                    newDir.setZ(-newDir.getX());
                    newDir.setX(z);
                    tpLoc = getTeleportLocation(block, tVec, newVector, newDir);
                } else {
                    newVector = tVec.clone().subtract(dir);
                    block = getSafestBlock(world, newVector);
                    if (block != null) {
                        newDir.setZ(-newDir.getZ());
                        newDir.setX(-newDir.getX());
                        tpLoc = getTeleportLocation(block, tVec, newVector, newDir);
                    }
                }
            }
        }
        System.out.println(tpLoc);
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
