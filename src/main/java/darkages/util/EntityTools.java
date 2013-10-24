package darkages.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.HashSet;

public class EntityTools {

    private static final HashSet<Byte> TRANSPARENT_BLOCKS = new HashSet<Byte>();

    static {
        TRANSPARENT_BLOCKS.add((byte) Material.AIR.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.LAVA.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.STATIONARY_LAVA.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.WATER.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.STATIONARY_WATER.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.LEAVES.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.DETECTOR_RAIL.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.POWERED_RAIL.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.WEB.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.LONG_GRASS.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.DEAD_BUSH.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.YELLOW_FLOWER.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.RED_ROSE.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.FIRE.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.REDSTONE_WIRE.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.CROPS.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.SIGN_POST.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.LADDER.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.RAILS.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.WALL_SIGN.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.LEVER.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.STONE_PLATE.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.WOOD_PLATE.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.REDSTONE_TORCH_OFF.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.REDSTONE_TORCH_ON.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.STONE_BUTTON.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.SUGAR_CANE_BLOCK.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.REDSTONE_TORCH_ON.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.PORTAL.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.DIODE_BLOCK_OFF.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.DIODE_BLOCK_ON.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.PUMPKIN_STEM.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.MELON_STEM.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.VINE.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.WATER_LILY.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.NETHER_WARTS.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.ENDER_PORTAL.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.TRIPWIRE.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.TRIPWIRE_HOOK.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.CARROT.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.POTATO.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.REDSTONE_TORCH_ON.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.WOOD_BUTTON.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.GOLD_PLATE.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.IRON_PLATE.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.REDSTONE_COMPARATOR_OFF.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.REDSTONE_COMPARATOR_ON.getId());
        TRANSPARENT_BLOCKS.add((byte) Material.ACTIVATOR_RAIL.getId());
    }

    public static LivingEntity getTargetedLivingEntity(final LivingEntity entity, final int maxDistance) {
        // Shitty hack to get the looked at entity.
        Location loc = entity.getLocation();
        Vector n1 = loc.toVector();
        Vector dir = loc.getDirection();
        LivingEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Entity e : entity.getNearbyEntities(maxDistance, maxDistance, maxDistance)) {
            if (e instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) e;
                Vector n0 = livingEntity.getLocation().toVector();
                //n0.setY(n0.getY() + livingEntity.getEyeHeight());
                double distance = getDistance(dir, n1, n0);
                if (distance > 0 && distance <= maxDistance) { // In front of the player, not behind.
                    double lineDist = Math.sqrt(n1.distanceSquared(n0) - distance * distance);
                    if (lineDist < 1) { // Tolerance value of within 1 blocks of the line.
                        if (distance < nearestDistance) {
                            // Make sure the targeted block is not closer
                            Block block = entity.getTargetBlock(TRANSPARENT_BLOCKS, maxDistance);
                            if (block != null) {
                                Vector b1 = block.getLocation().toVector();
                                double dist2 = -n1.clone().subtract(b1).dot(dir) / dir.lengthSquared();
                                if (dist2 > 0 && dist2 < distance) {
                                    continue; // targeted block is closer than entity
                                }
                            }
                            //if (entity.hasLineOfSight(e)) {  // Doesn't work.. can target through dirt
                            nearestDistance = distance;
                            nearest = livingEntity;
                            //}
                        }
                    }
                }
            }
        }
        return nearest;
    }

    private static double getDistance(final Vector dir, final Vector n1, final Vector n0) {
        return -n1.clone().subtract(n0).dot(dir) / dir.clone().lengthSquared();
    }

    public static double getDistance(final LivingEntity entity1, final LivingEntity entity2) {
        Location loc = entity1.getLocation();
        return getDistance(loc.getDirection(), loc.toVector(), entity2.getLocation().toVector());
    }
}
