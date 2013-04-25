package com.dumptruckman.minecraft.darkages.util;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import java.util.EnumSet;
import java.util.Set;

public class BlockSafety {

    public static Block getSafestBlock(final World world, final Vector vector) {
        Block block = world.getBlockAt(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
        if (isBlockSafe(block)) {
            return block;
        }
        block = block.getRelative(BlockFace.DOWN);
        if (isBlockSafe(block)) {
            return block;
        }
        block = block.getRelative(BlockFace.UP);
        if (isBlockSafe(block)) {
            return block;
        }
        return null;
    }

    private static Set<Material> TALL_MATERIALS = EnumSet.noneOf(Material.class);
    private static Set<Material> DANGEROUS_MATERIALS = EnumSet.noneOf(Material.class);

    static {
        TALL_MATERIALS.add(Material.FENCE);
        TALL_MATERIALS.add(Material.FENCE_GATE);
        TALL_MATERIALS.add(Material.NETHER_FENCE);
        TALL_MATERIALS.add(Material.COBBLE_WALL);
        DANGEROUS_MATERIALS.add(Material.LAVA);
        DANGEROUS_MATERIALS.add(Material.STATIONARY_LAVA);
        DANGEROUS_MATERIALS.add(Material.PORTAL);
        DANGEROUS_MATERIALS.add(Material.ENDER_PORTAL);
    }

    public static boolean isTallBlock(final Block block) {
        return TALL_MATERIALS.contains(block.getType());
    }

    public static boolean isDangerousBlock(final Block block) {
        return DANGEROUS_MATERIALS.contains(block.getType());
    }

    public static boolean isBlockSafe(final Block block) {
        Block blockAbove = block.getRelative(BlockFace.UP);
        Block blockBelow = block.getRelative(BlockFace.DOWN);
        return !block.getType().isSolid()
                && !block.getType().isOccluding()
                && !isDangerousBlock(block)
                && blockBelow.getType().isSolid()
                && !isTallBlock(blockBelow)
                && !blockAbove.getType().isSolid()
                && !blockAbove.getType().isOccluding()
                && !isDangerousBlock(blockAbove);
    }
}
